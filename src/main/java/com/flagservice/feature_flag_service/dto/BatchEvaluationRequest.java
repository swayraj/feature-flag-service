package com.flagservice.feature_flag_service.dto;

import java.util.List;

public class BatchEvaluationRequest {

    private List<String> userIds;
    private String flagName;

    // Constructors
    public BatchEvaluationRequest() {
    }

    public BatchEvaluationRequest(List<String> userIds, String flagName) {
        this.userIds = userIds;
        this.flagName = flagName;
    }

    // Getters and Setters
    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public String getFlagName() {
        return flagName;
    }

    public void setFlagName(String flagName) {
        this.flagName = flagName;
    }

    @Override
    public String toString() {
        return "BatchEvaluationRequest{" +
                "userIds=" + userIds +
                ", flagName='" + flagName + '\'' +
                '}';
    }
}