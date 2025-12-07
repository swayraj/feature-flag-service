package com.flagservice.feature_flag_service.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "flags")
public class Flag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "rollout_percentage", nullable = false)
    private int rolloutPercentage;

    @Column(name = "target_user_ids", length = 2000)
    private String targetUserIds;

    @Column(name = "scheduled_rollout_percentage")
    private Integer scheduledRolloutPercentage;  // Percentage to change to

    @Column(name = "scheduled_rollout_time")
    private LocalDateTime scheduledRolloutTime;  // When to apply the change

    @Column(name = "auto_rollout_enabled")
    private Boolean autoRolloutEnabled;  // Enable gradual auto-rollout

    @Column(name = "auto_rollout_step")
    private Integer autoRolloutStep;  // Increment by X% each step (e.g., 25)

    @Column(name = "auto_rollout_interval_hours")
    private Integer autoRolloutIntervalHours;  // Hours between steps (e.g., 24)

    @Column(name = "user_segment")
    private String userSegment;  // JSON: {"country":"US","platform":"iOS"}

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Default constructor (REQUIRED by JPA)
    public Flag() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.rolloutPercentage = 0;
        this.enabled = false;
        this.autoRolloutEnabled = false;
    }

    // Constructor with parameters
    public Flag(Long id, String name, String description, boolean enabled, int rolloutPercentage) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.enabled = enabled;
        this.rolloutPercentage = rolloutPercentage;
        this.targetUserIds = "";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.autoRolloutEnabled = false;
    }

    // Automatically set timestamps before save
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    // Automatically update timestamp before update
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters (keep all existing ones)

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getRolloutPercentage() {
        return rolloutPercentage;
    }

    public void setRolloutPercentage(int rolloutPercentage) {
        this.rolloutPercentage = rolloutPercentage;
    }

    public String getTargetUserIds() {
        return targetUserIds;
    }

    public void setTargetUserIds(String targetUserIds) {
        this.targetUserIds = targetUserIds;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getScheduledRolloutPercentage() {
        return scheduledRolloutPercentage;
    }

    public void setScheduledRolloutPercentage(Integer scheduledRolloutPercentage) {
        this.scheduledRolloutPercentage = scheduledRolloutPercentage;
    }

    public LocalDateTime getScheduledRolloutTime() {
        return scheduledRolloutTime;
    }

    public void setScheduledRolloutTime(LocalDateTime scheduledRolloutTime) {
        this.scheduledRolloutTime = scheduledRolloutTime;
    }

    public Boolean isAutoRolloutEnabled() {
        return autoRolloutEnabled;
    }

    public void setAutoRolloutEnabled(Boolean autoRolloutEnabled) {
        this.autoRolloutEnabled = autoRolloutEnabled;
    }

    public Integer getAutoRolloutStep() {
        return autoRolloutStep;
    }

    public void setAutoRolloutStep(Integer autoRolloutStep) {
        this.autoRolloutStep = autoRolloutStep;
    }

    public Integer getAutoRolloutIntervalHours() {
        return autoRolloutIntervalHours;
    }

    public void setAutoRolloutIntervalHours(Integer autoRolloutIntervalHours) {
        this.autoRolloutIntervalHours = autoRolloutIntervalHours;
    }

    public String getUserSegment() {
        return userSegment;
    }

    public void setUserSegment(String userSegment) {
        this.userSegment = userSegment;
    }

    @Override
    public String toString() {
        return "Flag{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", enabled=" + enabled +
                ", rolloutPercentage=" + rolloutPercentage +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}