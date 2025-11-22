package com.flagservice.feature_flag_service.model;

import java.time.LocalDateTime;

public class Flag {

    private Long id;
    private String name;
    private String description;
    private boolean enabled;
    private int rolloutPercentage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor (empty - for creating new flags)
    public Flag() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.rolloutPercentage = 0;
        this.enabled = false;
    }

    // Constructor (with parameters - for convenience)
    public Flag(Long id, String name, String description, boolean enabled, int rolloutPercentage) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.enabled = enabled;
        this.rolloutPercentage = rolloutPercentage;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }


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
        this.updatedAt = LocalDateTime.now();
    }

    public int getRolloutPercentage() {
        return rolloutPercentage;
    }

    public void setRolloutPercentage(int rolloutPercentage) {
        this.rolloutPercentage = rolloutPercentage;
        this.updatedAt = LocalDateTime.now();
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
