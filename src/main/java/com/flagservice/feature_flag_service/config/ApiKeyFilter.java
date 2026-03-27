package com.flagservice.feature_flag_service.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import com.flagservice.feature_flag_service.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    private final ApiKeyRepository apiKeyRepository;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Value("${WRITES_ENABLED:true}")
    private boolean writesEnabled;

    public ApiKeyFilter(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        boolean isCreateFlag = "POST".equalsIgnoreCase(method) && path.equals("/api/flags");
        boolean isDeleteFlag = "DELETE".equalsIgnoreCase(method) && path.startsWith("/api/flags/");

        if (!isCreateFlag && !isDeleteFlag) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!writesEnabled) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Flag creation and deletion are disabled in demo mode.\"}");
            return;
        }

        String apiKey = request.getHeader("X-API-Key");

        if (apiKey == null || apiKeyRepository.findByKeyValueAndActiveTrue(apiKey).isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid or missing API key\"}");
            return;
        }

        Bucket bucket = buckets.computeIfAbsent(apiKey, k ->
                Bucket.builder()
                        .addLimit(Bandwidth.builder()
                                .capacity(20)
                                .refillGreedy(20, Duration.ofMinutes(1))
                                .build())
                        .build()
        );

        if (!bucket.tryConsume(1)) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Rate limit exceeded. Max 20 requests per minute.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}