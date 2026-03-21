package com.flagservice.feature_flag_service.repository;

import com.flagservice.feature_flag_service.model.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    Optional<ApiKey> findByKeyValueAndActiveTrue(String keyValue);
}