package com.flagservice.feature_flag_service.dto;

public class FlagEvaluationRequest {

    private String userId;  // Changed from Long to String
    private String flagName;

    // Constructors
    public FlagEvaluationRequest() {
    }

    public FlagEvaluationRequest(String userId, String flagName) {
        this.userId = userId;
        this.flagName = flagName;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFlagName() {
        return flagName;
    }

    public void setFlagName(String flagName) {
        this.flagName = flagName;
    }

    @Override
    public String toString() {
        return "FlagEvaluationRequest{" +
                "userId='" + userId + '\'' +
                ", flagName='" + flagName + '\'' +
                '}';
    }
}