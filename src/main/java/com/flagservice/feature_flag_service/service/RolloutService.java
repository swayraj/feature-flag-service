package com.flagservice.feature_flag_service.service;

import com.flagservice.feature_flag_service.dto.BatchEvaluationResponse;
import com.flagservice.feature_flag_service.dto.FlagEvaluationResponse;
import com.flagservice.feature_flag_service.exception.FlagNotFoundException;
import com.flagservice.feature_flag_service.model.Flag;
import com.flagservice.feature_flag_service.repository.FlagRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class RolloutService {

    private final FlagRepository flagRepository;

    public RolloutService(FlagRepository flagRepository) {
        this.flagRepository = flagRepository;
    }

    /**
     * Evaluate if a user should get a feature flag
     */
    public FlagEvaluationResponse evaluateFlag(String flagName, String userId) {
        // Find the flag
        Flag flag = flagRepository.findByNameIgnoreCase(flagName)
                .orElseThrow(() -> new FlagNotFoundException("Flag '" + flagName + "' not found"));

        // If flag is disabled globally, nobody gets it
        if (!flag.isEnabled()) {
            return new FlagEvaluationResponse(
                    flagName,
                    false,
                    userId,
                    "Flag is disabled globally"
            );
        }

        // Check if user is specifically targeted
        if (isUserTargeted(flag, userId)) {
            return new FlagEvaluationResponse(
                    flagName,
                    true,
                    userId,
                    "User is specifically targeted"
            );
        }

        // Check percentage rollout
        if (isUserInRolloutPercentage(flag, userId)) {
            return new FlagEvaluationResponse(
                    flagName,
                    true,
                    userId,
                    "User is in rollout percentage (" + flag.getRolloutPercentage() + "%)"
            );
        }

        // User doesn't get the feature
        return new FlagEvaluationResponse(
                flagName,
                false,
                userId,
                "User not in rollout percentage"
        );
    }

    /**
     * Evaluate multiple flags for a user at once
     */
    public List<FlagEvaluationResponse> evaluateAllFlags(String userId) {
        List<Flag> allFlags = flagRepository.findAll();

        return allFlags.stream()
                .map(flag -> evaluateFlag(flag.getName(), userId))
                .toList();
    }

    /**
     * Check if user is specifically targeted for this flag
     */
    private boolean isUserTargeted(Flag flag, String userId) {
        String targetUserIds = flag.getTargetUserIds();

        if (targetUserIds == null || targetUserIds.trim().isEmpty()) {
            return false;
        }

        // Split by comma and check if userId is in the list
        List<String> targetedUsers = Arrays.asList(targetUserIds.split(","));
        return targetedUsers.stream()
                .map(String::trim)
                .anyMatch(id -> id.equalsIgnoreCase(userId));
    }

    /**
     * Check if user falls within the rollout percentage
     * Uses consistent hashing so same user always gets same result
     */
    private boolean isUserInRolloutPercentage(Flag flag, String userId) {
        int rolloutPercentage = flag.getRolloutPercentage();

        // 0% rollout = nobody gets it
        if (rolloutPercentage <= 0) {
            return false;
        }

        // 100% rollout = everyone gets it
        if (rolloutPercentage >= 100) {
            return true;
        }

        // Calculate hash bucket (0-99) for this user + flag combination
        int bucket = getUserBucket(flag.getName(), userId);

        // User gets feature if their bucket is less than rollout percentage
        return bucket < rolloutPercentage;
    }

    /**
     * Calculate which bucket (0-99) a user falls into for a specific flag
     * This ensures:
     * - Same user always gets same bucket for same flag (consistent)
     * - Different flags give different buckets (independent rollouts)
     * - Users are distributed evenly across buckets
     */
    private int getUserBucket(String flagName, String userId) {
        try {
            // Create a unique key combining flag name and user ID
            String key = flagName + ":" + userId;

            // Use SHA-256 to hash the key
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(key.getBytes(StandardCharsets.UTF_8));

            // Take first 4 bytes and convert to integer
            int hash = Math.abs(
                    ((hashBytes[0] & 0xFF) << 24) |
                            ((hashBytes[1] & 0xFF) << 16) |
                            ((hashBytes[2] & 0xFF) << 8) |
                            (hashBytes[3] & 0xFF)
            );

            // Return bucket 0-99
            return hash % 100;

        } catch (NoSuchAlgorithmException e) {
            // Fallback to simple hash (should never happen)
            return Math.abs((flagName + userId).hashCode()) % 100;
        }
    }

    /**
     * Get statistics about how many users would get this flag
     * Simulates with sample user IDs
     */
    public RolloutStatistics getStatistics(String flagName, int sampleSize) {
        Flag flag = flagRepository.findByNameIgnoreCase(flagName)
                .orElseThrow(() -> new FlagNotFoundException("Flag '" + flagName + "' not found"));

        if (!flag.isEnabled()) {
            return new RolloutStatistics(flagName, 0, sampleSize, 0.0);
        }

        // Simulate with sample user IDs
        int usersWhoGetFeature = 0;
        for (int i = 0; i < sampleSize; i++) {
            String testUserId = "user-" + i;
            FlagEvaluationResponse result = evaluateFlag(flagName, testUserId);
            if (result.isEnabled()) {
                usersWhoGetFeature++;
            }
        }

        double actualPercentage = (usersWhoGetFeature * 100.0) / sampleSize;

        return new RolloutStatistics(flagName, usersWhoGetFeature, sampleSize, actualPercentage);
    }

    /**
     * Inner class for rollout statistics
     */
    public static class RolloutStatistics {
        private final String flagName;
        private final int usersEnabled;
        private final int totalUsers;
        private final double actualPercentage;

        public RolloutStatistics(String flagName, int usersEnabled, int totalUsers, double actualPercentage) {
            this.flagName = flagName;
            this.usersEnabled = usersEnabled;
            this.totalUsers = totalUsers;
            this.actualPercentage = actualPercentage;
        }

        public String getFlagName() {
            return flagName;
        }

        public int getUsersEnabled() {
            return usersEnabled;
        }

        public int getTotalUsers() {
            return totalUsers;
        }

        public double getActualPercentage() {
            return actualPercentage;
        }

        @Override
        public String toString() {
            return String.format("RolloutStatistics{flagName='%s', usersEnabled=%d, totalUsers=%d, actualPercentage=%.2f%%}",
                    flagName, usersEnabled, totalUsers, actualPercentage);
        }
    }

    // Evaluate a flag for multiple users at once
    public BatchEvaluationResponse evaluateFlagForUsers(String flagName, List<String> userIds) {
        // Evaluate each user
        List<FlagEvaluationResponse> results = userIds.stream()
                .map(userId -> evaluateFlag(flagName, userId))
                .toList();

        return new BatchEvaluationResponse(flagName, results);
    }

    // Simulate rollout with generated user IDs
    public BatchEvaluationResponse simulateRollout(String flagName, int numberOfUsers) {
        // Generate test user IDs
        List<String> userIds = new java.util.ArrayList<>();
        for (int i = 1; i <= numberOfUsers; i++) {
            userIds.add("user-" + i);
        }

        return evaluateFlagForUsers(flagName, userIds);
    }

    // Get distribution buckets (0-9, 10-19, 20-29, etc.)
    public Map<String, Integer> getDistributionBuckets(String flagName, int sampleSize) {
        Map<String, Integer> buckets = new java.util.LinkedHashMap<>();

        // Initialize buckets
        for (int i = 0; i < 10; i++) {
            int start = i * 10;
            int end = start + 9;
            buckets.put(start + "-" + end, 0);
        }

        // Count users in each bucket
        for (int i = 1; i <= sampleSize; i++) {
            String userId = "user-" + i;
            int bucket = getUserBucket(flagName, userId);
            int bucketGroup = (bucket / 10) * 10;
            String key = bucketGroup + "-" + (bucketGroup + 9);
            buckets.put(key, buckets.get(key) + 1);
        }

        return buckets;
    }

    // ========== NEW METHODS FOR USER SEGMENTATION ==========

    /**
     * Check if user matches segment criteria
     */
    private boolean matchesUserSegment(Flag flag, Map<String, String> userAttributes) {
        String segment = flag.getUserSegment();

        if (segment == null || segment.trim().isEmpty() || userAttributes == null) {
            return true;  // No segment restriction
        }

        try {
            // Simple key-value matching (in production, use JSON parser)
            // Format: {"country":"US","platform":"iOS"}

            // Remove braces and quotes for simple parsing
            segment = segment.replace("{", "").replace("}", "")
                    .replace("\"", "").replace(" ", "");

            String[] criteria = segment.split(",");

            for (String criterion : criteria) {
                String[] parts = criterion.split(":");
                if (parts.length == 2) {
                    String key = parts[0];
                    String requiredValue = parts[1];
                    String actualValue = userAttributes.get(key);

                    if (actualValue == null || !actualValue.equalsIgnoreCase(requiredValue)) {
                        return false;  // User doesn't match this criterion
                    }
                }
            }

            return true;  // User matches all criteria

        } catch (Exception e) {
            System.err.println("Error parsing user segment: " + e.getMessage());
            return true;  // Default to allowing access if parsing fails
        }
    }

    /**
     * Evaluate flag with user attributes (for segmentation)
     */
    public FlagEvaluationResponse evaluateFlagWithAttributes(String flagName, String userId,
                                                             Map<String, String> userAttributes) {
        Flag flag = flagRepository.findByNameIgnoreCase(flagName)
                .orElseThrow(() -> new FlagNotFoundException("Flag '" + flagName + "' not found"));

        // If flag is disabled globally, nobody gets it
        if (!flag.isEnabled()) {
            return new FlagEvaluationResponse(
                    flagName,
                    false,
                    userId,
                    "Flag is disabled globally"
            );
        }

        // Check user segment match
        if (!matchesUserSegment(flag, userAttributes)) {
            return new FlagEvaluationResponse(
                    flagName,
                    false,
                    userId,
                    "User does not match segment criteria"
            );
        }

        // Check if user is specifically targeted
        if (isUserTargeted(flag, userId)) {
            return new FlagEvaluationResponse(
                    flagName,
                    true,
                    userId,
                    "User is specifically targeted"
            );
        }

        // Check percentage rollout
        if (isUserInRolloutPercentage(flag, userId)) {
            return new FlagEvaluationResponse(
                    flagName,
                    true,
                    userId,
                    "User is in rollout percentage (" + flag.getRolloutPercentage() + "%)"
            );
        }

        // User doesn't get the feature
        return new FlagEvaluationResponse(
                flagName,
                false,
                userId,
                "User not in rollout percentage"
        );
    }
}