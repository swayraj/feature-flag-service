package com.flagservice.feature_flag_service.controller;

import com.flagservice.feature_flag_service.dto.FlagEvaluationRequest;
import com.flagservice.feature_flag_service.dto.FlagEvaluationResponse;
import com.flagservice.feature_flag_service.exception.FlagNotFoundException;
import com.flagservice.feature_flag_service.service.RolloutService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.flagservice.feature_flag_service.dto.BatchEvaluationRequest;
import com.flagservice.feature_flag_service.dto.BatchEvaluationResponse;
import java.util.Map;

import java.util.List;

@RestController
@RequestMapping("/api/evaluate")
public class FlagEvaluationController {

    private final RolloutService rolloutService;

    public FlagEvaluationController(RolloutService rolloutService) {
        this.rolloutService = rolloutService;
    }

    /**
     * Evaluate a single flag for a user
     * POST /api/evaluate
     */
    @PostMapping
    public ResponseEntity<?> evaluateFlag(@RequestBody FlagEvaluationRequest request) {
        if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("userId is required");
        }
        if (request.getFlagName() == null || request.getFlagName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("flagName is required");
        }

        FlagEvaluationResponse response = rolloutService.evaluateFlag(
                request.getFlagName(),
                request.getUserId()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Evaluate a single flag for a user (simpler GET endpoint)
     * GET /api/evaluate/{flagName}?userId=xxx
     */
    @GetMapping("/{flagName}")
    public ResponseEntity<FlagEvaluationResponse> evaluateFlagSimple(
            @PathVariable String flagName,
            @RequestParam String userId) {
        FlagEvaluationResponse response = rolloutService.evaluateFlag(flagName, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Evaluate all flags for a user
     * GET /api/evaluate/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FlagEvaluationResponse>> evaluateAllFlagsForUser(@PathVariable String userId) {
        List<FlagEvaluationResponse> responses = rolloutService.evaluateAllFlags(userId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get rollout statistics for a flag
     * GET /api/evaluate/{flagName}/stats?sampleSize=1000
     */
    @GetMapping("/{flagName}/stats")
    public ResponseEntity<?> getFlagStatistics(
            @PathVariable String flagName,
            @RequestParam(defaultValue = "1000") int sampleSize) {
        try {
            RolloutService.RolloutStatistics stats = rolloutService.getStatistics(flagName, sampleSize);
            return ResponseEntity.ok(stats);
        } catch (FlagNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    /**
     * Evaluate a flag for multiple users
     * POST /api/evaluate/batch
     */
    @PostMapping("/batch")
    public ResponseEntity<?> evaluateFlagBatch(@RequestBody BatchEvaluationRequest request) {
        if (request.getFlagName() == null || request.getFlagName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("flagName is required");
        }
        if (request.getUserIds() == null || request.getUserIds().isEmpty()) {
            return ResponseEntity.badRequest().body("userIds list cannot be empty");
        }

        BatchEvaluationResponse response = rolloutService.evaluateFlagForUsers(
                request.getFlagName(),
                request.getUserIds()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Simulate rollout with generated users
     * GET /api/evaluate/{flagName}/simulate?numberOfUsers=100
     */
    @GetMapping("/{flagName}/simulate")
    public ResponseEntity<?> simulateRollout(
            @PathVariable String flagName,
            @RequestParam(defaultValue = "100") int numberOfUsers) {
        if (numberOfUsers < 1 || numberOfUsers > 10000) {
            return ResponseEntity.badRequest().body("numberOfUsers must be between 1 and 10000");
        }

        BatchEvaluationResponse response = rolloutService.simulateRollout(flagName, numberOfUsers);
        return ResponseEntity.ok(response);
    }

    /**
     * Get distribution buckets for a flag
     * GET /api/evaluate/{flagName}/distribution?sampleSize=1000
     */
    @GetMapping("/{flagName}/distribution")
    public ResponseEntity<Map<String, Integer>> getDistribution(
            @PathVariable String flagName,
            @RequestParam(defaultValue = "1000") int sampleSize) {
        Map<String, Integer> distribution = rolloutService.getDistributionBuckets(flagName, sampleSize);
        return ResponseEntity.ok(distribution);
    }

    /**
     * Get ASCII visualization of distribution
     * GET /api/evaluate/{flagName}/distribution/visual
     */
    @GetMapping("/{flagName}/distribution/visual")
    public ResponseEntity<String> getVisualDistribution(@PathVariable String flagName) {
        Map<String, Integer> distribution = rolloutService.getDistributionBuckets(flagName, 1000);

        StringBuilder visual = new StringBuilder();
        visual.append("Distribution for flag: ").append(flagName).append("\n");
        visual.append("Sample size: 1000 users\n\n");

        int maxCount = distribution.values().stream().mapToInt(Integer::intValue).max().orElse(0);

        for (Map.Entry<String, Integer> entry : distribution.entrySet()) {
            String bucket = entry.getKey();
            int count = entry.getValue();
            double percentage = (count / 1000.0) * 100;

            // Create bar
            int barLength = (int) ((count / (double) maxCount) * 50);
            String bar = "█".repeat(Math.max(0, barLength));

            visual.append(String.format("%-8s | %-50s | %3d (%.1f%%)\n",
                    bucket, bar, count, percentage));
        }

        return ResponseEntity.ok(visual.toString());
    }

}