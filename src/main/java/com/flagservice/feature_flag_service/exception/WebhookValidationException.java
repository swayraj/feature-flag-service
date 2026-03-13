package com.flagservice.feature_flag_service.exception;

public class WebhookValidationException extends RuntimeException {

    public WebhookValidationException(String message) {
        super(message);
    }
}