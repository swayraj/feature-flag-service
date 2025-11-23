package com.flagservice.feature_flag_service.exception;

public class FlagNotFoundException extends RuntimeException{

    public FlagNotFoundException(Long id) {
        super("Flag with ID " + id + " not found");
    }

    public FlagNotFoundException(String message) {
        super(message);
    }
}
