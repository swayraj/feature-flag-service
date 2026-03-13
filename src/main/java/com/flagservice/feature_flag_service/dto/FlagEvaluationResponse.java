package com.flagservice.feature_flag_service.dto;

import java.io.Serializable;

public class FlagEvaluationResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String flagName;
    private boolean enabled;
    private String userId;
    private String reason;

    public FlagEvaluationResponse() {
    }

    public FlagEvaluationResponse(String flagName, boolean enabled, String userId, String reason) {
        this.flagName = flagName;
        this.enabled = enabled;
        this.userId = userId;
        this.reason = reason;
    }

    public String getFlagName() { return flagName; }
    public void setFlagName(String flagName) { this.flagName = flagName; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

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