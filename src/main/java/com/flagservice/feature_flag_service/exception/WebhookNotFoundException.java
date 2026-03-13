package com.flagservice.feature_flag_service.exception;

public class WebhookNotFoundException extends RuntimeException {

    public WebhookNotFoundException(Long id) {
        super("Webhook with ID " + id + " not found");
    }

    public WebhookNotFoundException(String message) {
        super(message);
    }
}