package com.flagservice.feature_flag_service.exception;

public class FlagValidationException extends RuntimeException{

    public FlagValidationException(String message) {
        super(message);
    }
}
