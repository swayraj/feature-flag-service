package com.flagservice.feature_flag_service.model;

import jakarta.persistence.*;

@Entity
@Table(name = "api_keys")
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String keyValue;

    @Column(nullable = false)
    private String ownerName;

    @Column(nullable = false)
    private boolean active = true;

    public Long getId() { return id; }
    public String getKeyValue() { return keyValue; }
    public void setKeyValue(String keyValue) { this.keyValue = keyValue; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}