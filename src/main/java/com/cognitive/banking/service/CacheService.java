// src/main/java/com/cognitive/banking/service/CacheService.java
package com.cognitive.banking.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class CacheService {

    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void put(String cacheName, String key, Object value, long ttlMinutes) {
        try {
            String cacheKey = buildKey(cacheName, key);
            redisTemplate.opsForValue().set(cacheKey, value, ttlMinutes, TimeUnit.MINUTES);
            logger.debug("Cached {} with key: {}", cacheName, cacheKey);
        } catch (Exception e) {
            logger.warn("Failed to cache {} with key: {}. Error: {}", cacheName, key, e.getMessage());
        }
    }

    public Object get(String cacheName, String key) {
        try {
            String cacheKey = buildKey(cacheName, key);
            Object value = redisTemplate.opsForValue().get(cacheKey);
            if (value != null) {
                logger.debug("Cache hit for {} with key: {}", cacheName, cacheKey);
            }
            return value;
        } catch (Exception e) {
            logger.warn("Failed to get cached {} with key: {}. Error: {}", cacheName, key, e.getMessage());
            return null;
        }
    }

    public void evict(String cacheName, String key) {
        try {
            String cacheKey = buildKey(cacheName, key);
            redisTemplate.delete(cacheKey);
            logger.debug("Evicted cache for {} with key: {}", cacheName, cacheKey);
        } catch (Exception e) {
            logger.warn("Failed to evict cache for {} with key: {}. Error: {}", cacheName, key, e.getMessage());
        }
    }

    public void evictPattern(String cacheName, String pattern) {
        try {
            String keyPattern = buildKey(cacheName, pattern);
            redisTemplate.delete(redisTemplate.keys(keyPattern + "*"));
            logger.debug("Evicted cache pattern for {} with pattern: {}", cacheName, pattern);
        } catch (Exception e) {
            logger.warn("Failed to evict cache pattern for {} with pattern: {}. Error: {}", cacheName, pattern, e.getMessage());
        }
    }

    public boolean exists(String cacheName, String key) {
        try {
            String cacheKey = buildKey(cacheName, key);
            return Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey));
        } catch (Exception e) {
            logger.warn("Failed to check cache existence for {} with key: {}. Error: {}", cacheName, key, e.getMessage());
            return false;
        }
    }

    private String buildKey(String cacheName, String key) {
        return String.format("%s:%s", cacheName, key);
    }
}