package com.flagservice.feature_flag_service.dto;

public class FlagEvaluationResponse {

    private String flagName;
    private boolean enabled;
    private String userId;
    private String reason;  // Why user got/didn't get the feature

    // Constructors
    public FlagEvaluationResponse() {
    }

    public FlagEvaluationResponse(String flagName, boolean enabled, String userId, String reason) {
        this.flagName = flagName;
        this.enabled = enabled;
        this.userId = userId;
        this.reason = reason;
    }

    // Getters and Setters
    public String getFlagName() {
        return flagName;
    }

    public void setFlagName(String flagName) {
        this.flagName = flagName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "FlagEvaluationResponse{" +
                "flagName='" + flagName + '\'' +
                ", enabled=" + enabled +
                ", userId='" + userId + '\'' +
                ", reason='" + reason + '\'' +
                '}';
    }
}