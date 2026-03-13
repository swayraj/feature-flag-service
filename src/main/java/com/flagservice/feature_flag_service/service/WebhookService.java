package com.flagservice.feature_flag_service.service;

import tools.jackson.databind.ObjectMapper;
import com.flagservice.feature_flag_service.exception.WebhookNotFoundException;
import com.flagservice.feature_flag_service.exception.WebhookValidationException;
import com.flagservice.feature_flag_service.model.DeliveryStatus;
import com.flagservice.feature_flag_service.model.Webhook;
import com.flagservice.feature_flag_service.model.WebhookDelivery;
import com.flagservice.feature_flag_service.repository.WebhookDeliveryRepository;
import com.flagservice.feature_flag_service.repository.WebhookRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class WebhookService {

    private static final int MAX_ATTEMPTS = 3;
    private static final int[] RETRY_DELAYS_MS = {1000, 2000};

    private static final List<String> VALID_EVENTS = Arrays.asList(
            "FLAG_CREATED", "FLAG_UPDATED", "FLAG_DELETED", "FLAG_TOGGLED"
    );

    private final WebhookRepository webhookRepository;
    private final WebhookDeliveryRepository webhookDeliveryRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public WebhookService(WebhookRepository webhookRepository,
                          WebhookDeliveryRepository webhookDeliveryRepository,
                          RestTemplate restTemplate,
                          ObjectMapper objectMapper) {
        this.webhookRepository = webhookRepository;
        this.webhookDeliveryRepository = webhookDeliveryRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    // ================= CRUD =================

    public Webhook registerWebhook(Webhook webhook) {
        if (webhook.getName() == null || webhook.getName().trim().isEmpty()) {
            throw new WebhookValidationException("Webhook name is required");
        }

        validateUrl(webhook.getUrl());
        validateEvents(webhook.getEvents());

        webhook.setSecret(UUID.randomUUID().toString().replace("-", ""));
        webhook.setActive(true);

        return webhookRepository.save(webhook);
    }

    public List<Webhook> getAllWebhooks() {
        return webhookRepository.findAll();
    }

    public Webhook getWebhookById(Long id) {
        return webhookRepository.findById(id)
                .orElseThrow(() -> new WebhookNotFoundException(id));
    }

    public Webhook updateWebhook(Long id, Webhook updated) {
        Webhook existing = webhookRepository.findById(id)
                .orElseThrow(() -> new WebhookNotFoundException(id));

        if (updated.getName() != null && !updated.getName().trim().isEmpty()) {
            existing.setName(updated.getName());
        }

        if (updated.getUrl() != null && !updated.getUrl().trim().isEmpty()) {
            validateUrl(updated.getUrl());
            existing.setUrl(updated.getUrl());
        }

        if (updated.getEvents() != null && !updated.getEvents().trim().isEmpty()) {
            validateEvents(updated.getEvents());
            existing.setEvents(updated.getEvents());
        }

        return webhookRepository.save(existing);
    }

    public void deleteWebhook(Long id) {
        if (!webhookRepository.existsById(id)) {
            throw new WebhookNotFoundException(id);
        }

        List<WebhookDelivery> deliveries =
                webhookDeliveryRepository.findByWebhookIdOrderByDeliveredAtDesc(id);

        webhookDeliveryRepository.deleteAll(deliveries);
        webhookRepository.deleteById(id);
    }

    public Webhook toggleWebhook(Long id) {
        Webhook webhook = webhookRepository.findById(id)
                .orElseThrow(() -> new WebhookNotFoundException(id));

        webhook.setActive(!webhook.isActive());
        return webhookRepository.save(webhook);
    }

    public List<WebhookDelivery> getDeliveries(Long webhookId) {
        if (!webhookRepository.existsById(webhookId)) {
            throw new WebhookNotFoundException(webhookId);
        }
        return webhookDeliveryRepository
                .findByWebhookIdOrderByDeliveredAtDesc(webhookId);
    }

    // ================= TEST PING =================

    public Map<String, Object> sendTestPing(Long webhookId) {
        Webhook webhook = webhookRepository.findById(webhookId)
                .orElseThrow(() -> new WebhookNotFoundException(webhookId));

        Map<String, Object> testPayload = new HashMap<>();
        testPayload.put("eventType", "TEST_PING");
        testPayload.put("message", "Hello from FlagService! Your webhook is working.");
        testPayload.put("webhookId", webhookId);
        testPayload.put("timestamp", LocalDateTime.now().toString());

        WebhookDelivery delivery =
                deliverToWebhook(webhook, "TEST_PING", testPayload);

        Map<String, Object> result = new HashMap<>();
        result.put("status", delivery.getStatus().toString());
        result.put("responseCode", delivery.getResponseCode());
        result.put("attemptCount", delivery.getAttemptCount());
        result.put("webhookUrl", webhook.getUrl());
        result.put("message",
                delivery.getStatus() == DeliveryStatus.SUCCESS
                        ? "Test ping delivered successfully!"
                        : "Test ping failed after " + delivery.getAttemptCount() + " attempt(s). Check the URL.");

        return result;
    }

    // ================= ASYNC DELIVERY =================

    @Async
    public void deliverEvent(String eventType, Map<String, Object> payload) {
        List<Webhook> activeWebhooks = webhookRepository.findByActiveTrue();

        for (Webhook webhook : activeWebhooks) {
            if (isSubscribed(webhook, eventType)) {
                deliverToWebhook(webhook, eventType, payload);
            }
        }
    }

    // ================= CORE DELIVERY =================

    private WebhookDelivery deliverToWebhook(Webhook webhook,
                                             String eventType,
                                             Map<String, Object> payload) {

        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            return saveDelivery(webhook.getId(), eventType,
                    DeliveryStatus.FAILED, null,
                    "Serialization error: " + e.getMessage(), 1);
        }

        String signature;
        try {
            signature = signPayload(webhook.getSecret(), jsonBody);
        } catch (Exception e) {
            return saveDelivery(webhook.getId(), eventType,
                    DeliveryStatus.FAILED, null,
                    "Signing error: " + e.getMessage(), 1);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("X-Event-Type", eventType);
        headers.set("X-Signature", signature);
        headers.set("User-Agent", "FlagService/1.0");

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {

            try {
                ResponseEntity<String> response =
                        restTemplate.postForEntity(
                                webhook.getUrl(), entity, String.class);

                return saveDelivery(webhook.getId(), eventType,
                        DeliveryStatus.SUCCESS,
                        response.getStatusCode().value(),
                        truncate(response.getBody()),
                        attempt);
            }

            // 🔴 4xx → do NOT retry
            catch (HttpClientErrorException e) {
                return saveDelivery(webhook.getId(), eventType,
                        DeliveryStatus.FAILED,
                        e.getStatusCode().value(),
                        truncate(e.getResponseBodyAsString()),
                        attempt);
            }

            // 🟡 5xx → retry
            catch (HttpServerErrorException e) {
                if (attempt == MAX_ATTEMPTS) {
                    return saveDelivery(webhook.getId(), eventType,
                            DeliveryStatus.FAILED,
                            e.getStatusCode().value(),
                            truncate(e.getResponseBodyAsString()),
                            attempt);
                }
            }

            // 🟡 Network errors → retry
            catch (RestClientException e) {
                if (attempt == MAX_ATTEMPTS) {
                    return saveDelivery(webhook.getId(), eventType,
                            DeliveryStatus.FAILED,
                            null,
                            truncate(e.getMessage()),
                            attempt);
                }
            }

            sleep(RETRY_DELAYS_MS[attempt - 1]);
        }

        return saveDelivery(webhook.getId(), eventType,
                DeliveryStatus.FAILED,
                null,
                "Max attempts exhausted",
                MAX_ATTEMPTS);
    }

    // ================= HELPERS =================

    private String signPayload(String secret, String jsonBody) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(keySpec);

        byte[] hash = mac.doFinal(jsonBody.getBytes(StandardCharsets.UTF_8));

        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }

        return "sha256=" + hex;
    }

    private boolean isSubscribed(Webhook webhook, String eventType) {
        String events = webhook.getEvents();
        if (events == null || events.trim().equalsIgnoreCase("ALL")) {
            return true;
        }

        return Arrays.stream(events.split(","))
                .map(String::trim)
                .anyMatch(e -> e.equalsIgnoreCase(eventType));
    }

    private WebhookDelivery saveDelivery(Long webhookId,
                                         String eventType,
                                         DeliveryStatus status,
                                         Integer responseCode,
                                         String responseBody,
                                         int attemptCount) {

        WebhookDelivery delivery = new WebhookDelivery();
        delivery.setWebhookId(webhookId);
        delivery.setEventType(eventType);
        delivery.setStatus(status);
        delivery.setResponseCode(responseCode);
        delivery.setResponseBody(responseBody);
        delivery.setAttemptCount(attemptCount);

        return webhookDeliveryRepository.save(delivery);
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String truncate(String s) {
        if (s == null) return null;
        return s.length() > 500 ? s.substring(0, 500) : s;
    }

    private void validateUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new WebhookValidationException("Webhook URL is required");
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new WebhookValidationException(
                    "Webhook URL must start with http:// or https://");
        }
    }

    private void validateEvents(String events) {
        if (events == null || events.trim().isEmpty()) {
            throw new WebhookValidationException(
                    "Events field is required. Use 'ALL' or comma-separated types: " +
                            "FLAG_CREATED, FLAG_UPDATED, FLAG_DELETED, FLAG_TOGGLED");
        }

        if (events.trim().equalsIgnoreCase("ALL")) return;

        for (String event : events.split(",")) {
            String trimmed = event.trim().toUpperCase();
            if (!VALID_EVENTS.contains(trimmed)) {
                throw new WebhookValidationException(
                        "Invalid event type: '" + event.trim() +
                                "'. Valid types: " + VALID_EVENTS + " or ALL");
            }
        }
    }
}