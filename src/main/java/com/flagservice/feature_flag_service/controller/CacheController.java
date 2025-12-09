package com.flagservice.feature_flag_service.controller;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cache")
public class CacheController {

    private final CacheManager cacheManager;

    public CacheController(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Get cache statistics
     * GET /api/cache/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();

        Cache cache = cacheManager.getCache("flagEvaluation");

        if (cache != null) {
            stats.put("cacheName", "flagEvaluation");
            stats.put("cacheType", cache.getClass().getSimpleName());

            if (cache instanceof RedisCache redisCache) {
                stats.put("nativeCache", redisCache.getNativeCache().getClass().getSimpleName());
            }

            stats.put("status", "active");
        } else {
            stats.put("status", "not found");
        }

        return ResponseEntity.ok(stats);
    }

    /**
     * Clear all caches
     * DELETE /api/cache/clear
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, String>> clearCache() {
        Cache cache = cacheManager.getCache("flagEvaluation");

        if (cache != null) {
            cache.clear();
            return ResponseEntity.ok(Map.of(
                    "message", "Cache cleared successfully",
                    "cacheName", "flagEvaluation"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "message", "No cache to clear"
        ));
    }

    /**
     * Clear specific cache entry
     * DELETE /api/cache/clear/{flagName}/{userId}
     */
    @DeleteMapping("/clear/{flagName}/{userId}")
    public ResponseEntity<Map<String, String>> clearCacheEntry(
            @PathVariable String flagName,
            @PathVariable String userId) {
        Cache cache = cacheManager.getCache("flagEvaluation");

        if (cache != null) {
            String key = flagName + ":" + userId;
            cache.evict(key);

            return ResponseEntity.ok(Map.of(
                    "message", "Cache entry cleared",
                    "key", key
            ));
        }

        return ResponseEntity.ok(Map.of(
                "message", "Cache not found"
        ));
    }
}