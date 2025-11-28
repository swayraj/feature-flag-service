package com.flagservice.feature_flag_service.dto;

import java.util.List;
import java.util.Map;

public class BatchEvaluationResponse {

    private String flagName;
    private int totalUsers;
    private int usersEnabled;
    private int usersDisabled;
    private double enabledPercentage;
    private List<FlagEvaluationResponse> results;
    private Map<String, Integer> reasonCounts;  // Count by reason

    // Constructor
    public BatchEvaluationResponse() {
    }

    public BatchEvaluationResponse(String flagName, List<FlagEvaluationResponse> results) {
        this.flagName = flagName;
        this.results = results;
        this.totalUsers = results.size();
        this.usersEnabled = (int) results.stream().filter(FlagEvaluationResponse::isEnabled).count();
        this.usersDisabled = totalUsers - usersEnabled;
        this.enabledPercentage = totalUsers > 0 ? (usersEnabled * 100.0 / totalUsers) : 0.0;

        // Count reasons
        this.reasonCounts = new java.util.HashMap<>();
        for (FlagEvaluationResponse result : results) {
            String reason = result.getReason();
            reasonCounts.put(reason, reasonCounts.getOrDefault(reason, 0) + 1);
        }
    }

    // Getters and Setters
    public String getFlagName() {
        return flagName;
    }

    public void setFlagName(String flagName) {
        this.flagName = flagName;
    }

    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    public int getUsersEnabled() {
        return usersEnabled;
    }

    public void setUsersEnabled(int usersEnabled) {
        this.usersEnabled = usersEnabled;
    }

    public int getUsersDisabled() {
        return usersDisabled;
    }

    public void setUsersDisabled(int usersDisabled) {
        this.usersDisabled = usersDisabled;
    }

    public double getEnabledPercentage() {
        return enabledPercentage;
    }

    public void setEnabledPercentage(double enabledPercentage) {
        this.enabledPercentage = enabledPercentage;
    }

    public List<FlagEvaluationResponse> getResults() {
        return results;
    }

    public void setResults(List<FlagEvaluationResponse> results) {
        this.results = results;
    }

    public Map<String, Integer> getReasonCounts() {
        return reasonCounts;
    }

    public void setReasonCounts(Map<String, Integer> reasonCounts) {
        this.reasonCounts = reasonCounts;
    }

    @Override
    public String toString() {
        return "BatchEvaluationResponse{" +
                "flagName='" + flagName + '\'' +
                ", totalUsers=" + totalUsers +
                ", usersEnabled=" + usersEnabled +
                ", usersDisabled=" + usersDisabled +
                ", enabledPercentage=" + String.format("%.2f", enabledPercentage) +
                '}';
    }
}