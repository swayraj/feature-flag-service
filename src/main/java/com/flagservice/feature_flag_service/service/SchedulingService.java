package com.flagservice.feature_flag_service.service;

import com.flagservice.feature_flag_service.model.Flag;
import com.flagservice.feature_flag_service.repository.FlagRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class SchedulingService {

    private final FlagRepository flagRepository;

    public SchedulingService(FlagRepository flagRepository) {
        this.flagRepository = flagRepository;
    }

    /**
     * Process scheduled rollout changes
     * Runs every minute
     */
    @Scheduled(fixedRate = 60000)  // 60000ms = 1 minute
    public void processScheduledRollouts() {
        LocalDateTime now = LocalDateTime.now();

        List<Flag> flags = flagRepository.findAll();

        for (Flag flag : flags) {
            // Check for scheduled one-time rollout
            if (flag.getScheduledRolloutTime() != null &&
                    flag.getScheduledRolloutPercentage() != null) {

                if (now.isAfter(flag.getScheduledRolloutTime())) {
                    System.out.println("📅 Applying scheduled rollout for flag: " + flag.getName() +
                            " → " + flag.getScheduledRolloutPercentage() + "%");

                    flag.setRolloutPercentage(flag.getScheduledRolloutPercentage());
                    flag.setScheduledRolloutTime(null);  // Clear the schedule
                    flag.setScheduledRolloutPercentage(null);

                    flagRepository.save(flag);
                }
            }

            // Check for gradual auto-rollout (WITH NULL CHECK!)
            if (Boolean.TRUE.equals(flag.isAutoRolloutEnabled()) &&
                    flag.getAutoRolloutStep() != null &&
                    flag.getAutoRolloutIntervalHours() != null) {

                processAutoRollout(flag, now);
            }
        }
    }

    /**
     * Process gradual auto-rollout for a flag
     */
    private void processAutoRollout(Flag flag, LocalDateTime now) {
        // Check if enough time has passed since last update
        LocalDateTime lastUpdate = flag.getUpdatedAt();
        long hoursSinceUpdate = java.time.Duration.between(lastUpdate, now).toHours();

        if (hoursSinceUpdate >= flag.getAutoRolloutIntervalHours()) {
            int currentPercentage = flag.getRolloutPercentage();
            int step = flag.getAutoRolloutStep();
            int newPercentage = Math.min(currentPercentage + step, 100);

            if (newPercentage > currentPercentage) {
                System.out.println("🚀 Auto-rollout for flag: " + flag.getName() +
                        " → " + currentPercentage + "% to " + newPercentage + "%");

                flag.setRolloutPercentage(newPercentage);
                flagRepository.save(flag);

                // Disable auto-rollout if reached 100%
                if (newPercentage >= 100) {
                    flag.setAutoRolloutEnabled(false);
                    flagRepository.save(flag);
                    System.out.println("✅ Auto-rollout complete for flag: " + flag.getName());
                }
            }
        }
    }

    /**
     * Schedule a one-time rollout change
     */
    public Flag scheduleRollout(Long flagId, int targetPercentage, LocalDateTime scheduledTime) {
        Flag flag = flagRepository.findById(flagId)
                .orElseThrow(() -> new IllegalArgumentException("Flag not found"));

        if (targetPercentage < 0 || targetPercentage > 100) {
            throw new IllegalArgumentException("Target percentage must be between 0 and 100");
        }

        if (scheduledTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Scheduled time must be in the future");
        }

        flag.setScheduledRolloutPercentage(targetPercentage);
        flag.setScheduledRolloutTime(scheduledTime);

        return flagRepository.save(flag);
    }

    /**
     * Enable gradual auto-rollout
     */
    public Flag enableAutoRollout(Long flagId, int step, int intervalHours) {
        Flag flag = flagRepository.findById(flagId)
                .orElseThrow(() -> new IllegalArgumentException("Flag not found"));

        if (step < 1 || step > 100) {
            throw new IllegalArgumentException("Step must be between 1 and 100");
        }

        if (intervalHours < 1) {
            throw new IllegalArgumentException("Interval must be at least 1 hour");
        }

        flag.setAutoRolloutEnabled(true);
        flag.setAutoRolloutStep(step);
        flag.setAutoRolloutIntervalHours(intervalHours);

        return flagRepository.save(flag);
    }

    /**
     * Disable auto-rollout
     */
    public Flag disableAutoRollout(Long flagId) {
        Flag flag = flagRepository.findById(flagId)
                .orElseThrow(() -> new IllegalArgumentException("Flag not found"));

        flag.setAutoRolloutEnabled(false);

        return flagRepository.save(flag);
    }

    /**
     * Cancel scheduled rollout
     */
    public Flag cancelScheduledRollout(Long flagId) {
        Flag flag = flagRepository.findById(flagId)
                .orElseThrow(() -> new IllegalArgumentException("Flag not found"));

        flag.setScheduledRolloutTime(null);
        flag.setScheduledRolloutPercentage(null);

        return flagRepository.save(flag);
    }
}