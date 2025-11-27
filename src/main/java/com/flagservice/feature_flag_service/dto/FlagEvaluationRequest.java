package com.flagservice.feature_flag_service.dto;

public class FlagEvaluationRequest {

    private Long userId;
    private String flagName;


    public FlagEvaluationRequest() {
    }

    public FlagEvaluationRequest(Long userId, String flagName) {
        this.userId = userId;
        this.flagName = flagName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
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
                "userId=" + userId +
                ", flagName='" + flagName + '\'' +
                '}';
    }


}
