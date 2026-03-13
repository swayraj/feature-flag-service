package com.flagservice.feature_flag_service.repository;

import com.flagservice.feature_flag_service.model.WebhookDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, Long> {
    List<WebhookDelivery> findByWebhookIdOrderByDeliveredAtDesc(Long webhookId);
}