package com.flagservice.feature_flag_service.service;

import com.flagservice.feature_flag_service.exception.FlagNotFoundException;
import com.flagservice.feature_flag_service.exception.FlagValidationException;
import com.flagservice.feature_flag_service.model.Flag;
import com.flagservice.feature_flag_service.repository.FlagRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FlagService {

    private final FlagRepository flagRepository;

    // Constructor injection
    public FlagService(FlagRepository flagRepository) {
        this.flagRepository = flagRepository;
        initializeSampleData();
    }

    /**
     * Initialize with sample data if database is empty
     */
    private void initializeSampleData() {
        if (flagRepository.count() == 0) {
            flagRepository.save(new Flag(null, "dark_mode", "Enable dark mode UI", false, 0));
            flagRepository.save(new Flag(null, "new_checkout", "New checkout flow", true, 25));
            flagRepository.save(new Flag(null, "ai_recommendations", "AI-powered product recommendations", true, 50));
        }
    }

    /**
     * Get all flags
     */
    public List<Flag> getAllFlags() {
        return flagRepository.findAll();
    }

    /**
     * Get flag by ID
     */
    public Optional<Flag> getFlagById(Long id) {
        return flagRepository.findById(id);
    }

    /**
     * Create a new flag with validation
     */
    public Flag createFlag(Flag flag) {
        // Validation
        validateFlagName(flag.getName());
        validateRolloutPercentage(flag.getRolloutPercentage());

        // Check for duplicate names
        if (flagRepository.existsByNameIgnoreCase(flag.getName())) {
            throw new FlagValidationException("Flag with name '" + flag.getName() + "' already exists");
        }

        // Save to database
        return flagRepository.save(flag);
    }

    /**
     * Update an existing flag
     * Invalidates all cache entries for this flag
     */
    @CacheEvict(value = "flagEvaluation", allEntries = true)
    public Flag updateFlag(Long id, Flag updatedFlag) {
        Flag existingFlag = flagRepository.findById(id)
                .orElseThrow(() -> new FlagNotFoundException(id));

        // Validation
        validateFlagName(updatedFlag.getName());
        validateRolloutPercentage(updatedFlag.getRolloutPercentage());

        // Check if new name conflicts with another flag
        if (!existingFlag.getName().equalsIgnoreCase(updatedFlag.getName())) {
            if (flagRepository.existsByNameIgnoreCase(updatedFlag.getName())) {
                throw new FlagValidationException("Flag with name '" + updatedFlag.getName() + "' already exists");
            }
        }

        // Update fields
        existingFlag.setName(updatedFlag.getName());
        existingFlag.setDescription(updatedFlag.getDescription());
        existingFlag.setEnabled(updatedFlag.isEnabled());
        existingFlag.setRolloutPercentage(updatedFlag.getRolloutPercentage());

        return flagRepository.save(existingFlag);
    }

    /**
     * Delete a flag
     * Clears cache
     */
    @CacheEvict(value = "flagEvaluation", allEntries = true)
    public void deleteFlag(Long id) {
        if (!flagRepository.existsById(id)) {
            throw new FlagNotFoundException(id);
        }
        flagRepository.deleteById(id);
    }

    /**
     * Search flags by name (case-insensitive)
     */
    public List<Flag> searchFlagsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return flagRepository.findAll();
        }
        return flagRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Get only enabled flags
     */
    public List<Flag> getEnabledFlags() {
        return flagRepository.findByEnabledTrue();
    }

    /**
     * Toggle flag on/off
     * Clears cache
     */
    @CacheEvict(value = "flagEvaluation", allEntries = true)
    public Flag toggleFlag(Long id) {
        Flag flag = flagRepository.findById(id)
                .orElseThrow(() -> new FlagNotFoundException(id));

        flag.setEnabled(!flag.isEnabled());

        return flagRepository.save(flag);
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

        if (!name.matches("^[a-zA-Z0-9_-]+$")) {
            throw new FlagValidationException("Flag name can only contain letters, numbers, underscores, and hyphens");
        }
    }

    private void validateRolloutPercentage(int percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new FlagValidationException("Rollout percentage must be between 0 and 100");
        }
    }
}