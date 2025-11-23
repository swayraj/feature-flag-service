package com.flagservice.feature_flag_service.service;

import com.flagservice.feature_flag_service.exception.FlagNotFoundException;
import com.flagservice.feature_flag_service.exception.FlagValidationException;
import com.flagservice.feature_flag_service.model.Flag;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FlagService {

    public List<Flag> flags = new ArrayList<>();
    private Long nextId = 1L;

    public FlagService() {
        flags.add(new Flag(nextId++, "dark_mode", "Enable dark mode UI", false, 0));
        flags.add(new Flag(nextId++, "new_checkout", "New checkout flow", true, 25));
        flags.add(new Flag(nextId++, "ai_recommendations", "AI-powered product recommendations", true, 50));
    }

    //Get all Flags
    public List<Flag> getAllFlags()
    {
        return new ArrayList<>(flags);
    }

    //Get Flag by Id
    public Optional<Flag> getFlagById(Long id)
    {
        return flags.stream()
                .filter(f -> f.getId().equals(id))
                .findFirst();
    }

    //Create a new Flag
    public Flag createFlag(Flag flag)
    {
        //validation
        validateFlagName(flag.getName());
        validateRolloutPercentage(flag.getRolloutPercentage());

        // Check for duplicate names
        if (flagExistsByName(flag.getName())) {
            throw new FlagValidationException("Flag with name '" + flag.getName() + "' already exists");
        }

        // Set ID and timestamps
        flag.setId(nextId++);
        flag.setCreatedAt(LocalDateTime.now());
        flag.setUpdatedAt(LocalDateTime.now());

        flags.add(flag);
        return flag;
    }

    //Update an existing flag
    public Flag updateFlag(Long id, Flag updatedFlag)
    {
        Flag existingFlag = getFlagById(id)
                .orElseThrow(() -> new FlagNotFoundException(id));

        // Validation
        validateFlagName(updatedFlag.getName());
        validateRolloutPercentage(updatedFlag.getRolloutPercentage());

        // Check if new name conflicts with another flag
        if (!existingFlag.getName().equals(updatedFlag.getName()) && flagExistsByName(updatedFlag.getName())) {
            throw new FlagValidationException("Flag with name '" + updatedFlag.getName() + "' already exists");
        }

        // Update fields
        existingFlag.setName(updatedFlag.getName());
        existingFlag.setDescription(updatedFlag.getDescription());
        existingFlag.setEnabled(updatedFlag.isEnabled());
        existingFlag.setRolloutPercentage(updatedFlag.getRolloutPercentage());
        existingFlag.setUpdatedAt(LocalDateTime.now());

        return existingFlag;
    }

    //Delete a Flag
    public void deleteFlag(Long id) {
        boolean removed = flags.removeIf(f -> f.getId().equals(id));
        if (!removed) {
            throw new FlagNotFoundException(id);
        }
    }

    //Search Flag by name
    public List<Flag> searchFlagsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return getAllFlags();
        }

        return flags.stream()
                .filter(f -> f.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
    }

    //Get only enabled Flags
    public List<Flag> getEnabledFlags() {
        return flags.stream()
                .filter(Flag::isEnabled)
                .toList();
    }

    //Toggle flag ON or OFF
    public Flag toggleFlag(Long id) {
        Flag flag = getFlagById(id)
                .orElseThrow(() -> new FlagNotFoundException(id));

        flag.setEnabled(!flag.isEnabled());
        flag.setUpdatedAt(LocalDateTime.now());

        return flag;
    }

    // ========== VALIDATION METHODS ==========

    private void validateFlagName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new FlagValidationException("Flag name cannot be empty");
        }

        if (name.length() < 3) {
            throw new FlagValidationException("Flag name must be at least 3 characters long");
        }

        if (name.length() > 50) {
            throw new FlagValidationException("Flag name cannot exceed 50 characters");
        }

        // Only allow alphanumeric, underscores, and hyphens
        if (!name.matches("^[a-zA-Z0-9_-]+$")) {
            throw new FlagValidationException("Flag name can only contain letters, numbers, underscores, and hyphens");
        }
    }

    private void validateRolloutPercentage(int percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new FlagValidationException("Rollout percentage must be between 0 and 100");
        }
    }

    private boolean flagExistsByName(String name) {
        return flags.stream()
                .anyMatch(f -> f.getName().equalsIgnoreCase(name));
    }
}