package com.flagservice.feature_flag_service.repository;

import com.flagservice.feature_flag_service.model.Webhook;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WebhookRepository extends JpaRepository<Webhook, Long> {
    List<Webhook> findByActiveTrue();
}