package com.flagservice.feature_flag_service.config;

import com.flagservice.feature_flag_service.model.ApiKey;
import com.flagservice.feature_flag_service.repository.ApiKeyRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {

    private final ApiKeyRepository apiKeyRepository;

    @Value("${API_KEY:}")
    private String apiKey;

    public DataInitializer(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (apiKey == null || apiKey.isBlank()) {
            return;
        }
        if (apiKeyRepository.findByKeyValueAndActiveTrue(apiKey).isEmpty()) {
            ApiKey key = new ApiKey();
            key.setKeyValue(apiKey);
            key.setOwnerName("admin");
            apiKeyRepository.save(key);
        }
    }
}
