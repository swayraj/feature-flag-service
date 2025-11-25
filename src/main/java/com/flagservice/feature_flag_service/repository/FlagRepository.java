package com.flagservice.feature_flag_service.repository;

import com.flagservice.feature_flag_service.model.Flag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlagRepository extends JpaRepository<Flag, Long> {

    /**
     * Find flag by name (case-insensitive)
     */
    Optional<Flag> findByNameIgnoreCase(String name);

    /**
     * Check if flag exists by name (case-insensitive)
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Find all enabled flags
     */
    List<Flag> findByEnabledTrue();

    /**
     * Find flags by name containing string (case-insensitive)
     */
    List<Flag> findByNameContainingIgnoreCase(String name);
}