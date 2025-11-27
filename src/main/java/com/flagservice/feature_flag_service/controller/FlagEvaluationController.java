package com.flagservice.feature_flag_service.controller;

import com.flagservice.feature_flag_service.dto.FlagEvaluationRequest;
import com.flagservice.feature_flag_service.dto.FlagEvaluationResponse;
import com.flagservice.feature_flag_service.exception.FlagNotFoundException;
import com.flagservice.feature_flag_service.service.RolloutService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        try {
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

        } catch (FlagNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    /**
     * Evaluate a single flag for a user (simpler GET endpoint)
     * GET /api/evaluate/{flagName}?userId=xxx
     */
    @GetMapping("/{flagName}")
    public ResponseEntity<?> evaluateFlagSimple(
            @PathVariable String flagName,
            @RequestParam String userId) {
        try {
            FlagEvaluationResponse response = rolloutService.evaluateFlag(flagName, userId);
            return ResponseEntity.ok(response);
        } catch (FlagNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
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
}