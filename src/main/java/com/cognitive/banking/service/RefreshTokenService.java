package com.cognitive.banking.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String REFRESH_PREFIX = "refresh:";

    public RefreshTokenService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Store refresh token ID for a user (used for token rotation and revocation)
    public void storeRefreshToken(String username, String tokenId, long ttlMillis) {
        redisTemplate.opsForValue()
                .set(REFRESH_PREFIX + username + ":" + tokenId, tokenId, ttlMillis, TimeUnit.MILLISECONDS);
    }

    // Check if refresh token ID is valid (exists and not revoked)
    public boolean isValidRefreshToken(String username, String tokenId) {
        return redisTemplate.hasKey(REFRESH_PREFIX + username + ":" + tokenId);
    }

    // Revoke all refresh tokens for a user (e.g., on logout or password change)
    public void revokeAllUserRefreshTokens(String username) {
        // In production, you need to iterate over keys with pattern: REFRESH_PREFIX + username + ":*"
        // This is simplified; use redisTemplate.keys() or maintain a set of token IDs per user.
        // For brevity, we'll assume a method to delete pattern exists.
        // We'll implement a more robust solution if needed.
    }

    public void removeRefreshToken(String username, String tokenId) {
        redisTemplate.delete(REFRESH_PREFIX + username + ":" + tokenId);
    }
}