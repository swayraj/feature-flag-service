package com.flagservice.feature_flag_service.controller;

import com.flagservice.feature_flag_service.model.Webhook;
import com.flagservice.feature_flag_service.model.WebhookDelivery;
import com.flagservice.feature_flag_service.service.WebhookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    private final WebhookService webhookService;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    /**
     * Register a new webhook
     * POST /api/webhooks
     * Body: {"name": "My Slack Hook", "url": "https://...", "events": "ALL"}
     */
    @PostMapping
    public ResponseEntity<Webhook> registerWebhook(@RequestBody Webhook webhook) {
        Webhook created = webhookService.registerWebhook(webhook);
        return ResponseEntity.status(201).body(created);
    }

    /**
     * List all webhooks
     * GET /api/webhooks
     */
    @GetMapping
    public ResponseEntity<List<Webhook>> getAllWebhooks() {
        return ResponseEntity.ok(webhookService.getAllWebhooks());
    }

    /**
     * Get a single webhook
     * GET /api/webhooks/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Webhook> getWebhook(@PathVariable Long id) {
        return ResponseEntity.ok(webhookService.getWebhookById(id));
    }

    /**
     * Update a webhook (name, url, or events)
     * PUT /api/webhooks/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Webhook> updateWebhook(@PathVariable Long id,
                                                 @RequestBody Webhook webhook) {
        return ResponseEntity.ok(webhookService.updateWebhook(id, webhook));
    }

    /**
     * Delete a webhook and its delivery history
     * DELETE /api/webhooks/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWebhook(@PathVariable Long id) {
        webhookService.deleteWebhook(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Toggle a webhook active/inactive
     * POST /api/webhooks/{id}/toggle
     */
    @PostMapping("/{id}/toggle")
    public ResponseEntity<Webhook> toggleWebhook(@PathVariable Long id) {
        return ResponseEntity.ok(webhookService.toggleWebhook(id));
    }

    /**
     * Send a test ping to verify the webhook URL is reachable
     * POST /api/webhooks/{id}/test
     */
    @PostMapping("/{id}/test")
    public ResponseEntity<Map<String, Object>> testWebhook(@PathVariable Long id) {
        Map<String, Object> result = webhookService.sendTestPing(id);
        return ResponseEntity.ok(result);
    }

    /**
     * View delivery history for a webhook (most recent first)
     * GET /api/webhooks/{id}/deliveries
     */
    @GetMapping("/{id}/deliveries")
    public ResponseEntity<List<WebhookDelivery>> getDeliveries(@PathVariable Long id) {
        return ResponseEntity.ok(webhookService.getDeliveries(id));
    }
}