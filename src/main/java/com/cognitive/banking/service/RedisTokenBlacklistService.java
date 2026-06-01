package com.cognitive.banking.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisTokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String BLACKLIST_PREFIX = "token:blacklist:";

    public RedisTokenBlacklistService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Blacklist a JWT for the remaining of its TTL.
     * @param token JWT string
     * @param ttlMillis remaining time to live in milliseconds
     */
    public void blacklist(String token, long ttlMillis) {
        if (ttlMillis > 0) {
            redisTemplate.opsForValue()
                    .set(BLACKLIST_PREFIX + token, "true", ttlMillis, TimeUnit.MILLISECONDS);
        }
    }

    public boolean isBlacklisted(String token) {
        return redisTemplate.hasKey(BLACKLIST_PREFIX + token);
    }
}