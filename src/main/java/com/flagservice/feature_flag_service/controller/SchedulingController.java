package com.flagservice.feature_flag_service.controller;

import com.flagservice.feature_flag_service.model.Flag;
import com.flagservice.feature_flag_service.service.SchedulingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/schedule")
public class SchedulingController {

    private final SchedulingService schedulingService;

    public SchedulingController(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    /**
     * Schedule a one-time rollout change
     * POST /api/schedule/rollout
     * Body: {"flagId": 1, "targetPercentage": 50, "scheduledTime": "2024-01-20T15:00:00"}
     */
    @PostMapping("/rollout")
    public ResponseEntity<?> scheduleRollout(@RequestBody Map<String, Object> request) {
        try {
            Long flagId = Long.valueOf(request.get("flagId").toString());
            int targetPercentage = Integer.parseInt(request.get("targetPercentage").toString());
            String timeStr = request.get("scheduledTime").toString();

            LocalDateTime scheduledTime = LocalDateTime.parse(timeStr);

            Flag flag = schedulingService.scheduleRollout(flagId, targetPercentage, scheduledTime);

            return ResponseEntity.ok(flag);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Enable gradual auto-rollout
     * POST /api/schedule/auto-rollout
     * Body: {"flagId": 1, "step": 25, "intervalHours": 24}
     */
    @PostMapping("/auto-rollout")
    public ResponseEntity<?> enableAutoRollout(@RequestBody Map<String, Object> request) {
        try {
            Long flagId = Long.valueOf(request.get("flagId").toString());
            int step = Integer.parseInt(request.get("step").toString());
            int intervalHours = Integer.parseInt(request.get("intervalHours").toString());

            Flag flag = schedulingService.enableAutoRollout(flagId, step, intervalHours);

            Map<String, Object> response = new HashMap<>();
            response.put("flag", flag);
            response.put("message", "Auto-rollout enabled. Will increase by " + step + "% every " + intervalHours + " hours");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Disable auto-rollout
     * POST /api/schedule/auto-rollout/{flagId}/disable
     */
    @PostMapping("/auto-rollout/{flagId}/disable")
    public ResponseEntity<?> disableAutoRollout(@PathVariable Long flagId) {
        try {
            Flag flag = schedulingService.disableAutoRollout(flagId);
            return ResponseEntity.ok(flag);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Cancel scheduled rollout
     * DELETE /api/schedule/rollout/{flagId}
     */
    @DeleteMapping("/rollout/{flagId}")
    public ResponseEntity<?> cancelScheduledRollout(@PathVariable Long flagId) {
        try {
            Flag flag = schedulingService.cancelScheduledRollout(flagId);
            return ResponseEntity.ok(flag);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}