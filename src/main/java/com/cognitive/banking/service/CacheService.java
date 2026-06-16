package com.cognitive.banking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class CacheService {
    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);

    private final RedisTemplate<String, Object> redisTemplate;

    public CacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Cache name constants
    public static final String USER_CACHE = "users";
    public static final String TOKEN_BLACKLIST = "token:blacklist";
    public static final String REFRESH_TOKEN_CACHE = "refresh:tokens";
    public static final String FAILED_ATTEMPTS_CACHE = "auth:failed:attempts";
    public static final String SESSION_CACHE = "sessions";
    public static final String OTP_CACHE = "otp";
    public static final String RATE_LIMIT_CACHE = "rate:limit";
    public static final String PHONE_OTP_CACHE = "phone:otp";

    // ==================== GENERIC CACHE OPS ====================

    public void put(String namespace, String key, Object value) {
        put(namespace, key, value, 30, TimeUnit.MINUTES);
    }

    public void put(String namespace, String key, Object value, long ttl) {
        put(namespace, key, value, ttl, TimeUnit.MINUTES);
    }

    public void put(String namespace, String key, Object value, long ttl, TimeUnit unit) {
        try {
            String fullKey = buildKey(namespace, key);
            redisTemplate.opsForValue().set(fullKey, value, ttl, unit);
            logger.debug("Cached {}:{} with TTL {} {}", namespace, key, ttl, unit);
        } catch (Exception e) {
            logger.warn("Failed to cache {}:{} Error: {}", namespace, key, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String namespace, String key, Class<T> type) {
        try {
            String fullKey = buildKey(namespace, key);
            Object value = redisTemplate.opsForValue().get(fullKey);
            if (value != null) {
                if (type.isInstance(value)) {
                    return Optional.of((T) value);
                }
                // Handle cases where value might be stored as different type
                logger.debug("Type mismatch for key {}: expected {}, got {}", key, type.getSimpleName(), value.getClass().getSimpleName());
            }
            return Optional.empty();
        } catch (Exception e) {
            logger.warn("Failed to get {}:{} Error: {}", namespace, key, e.getMessage());
            return Optional.empty();
        }
    }

    public void evict(String namespace, String key) {
        try {
            redisTemplate.delete(buildKey(namespace, key));
            logger.debug("Evicted {}:{}", namespace, key);
        } catch (Exception e) {
            logger.warn("Failed to evict {}:{} Error: {}", namespace, key, e.getMessage());
        }
    }

    public void evictPattern(String namespace, String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(buildKey(namespace, pattern) + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                logger.debug("Evicted {} keys matching pattern {}:{}", keys.size(), namespace, pattern);
            }
        } catch (Exception e) {
            logger.warn("Failed to evict pattern {}:{} Error: {}", namespace, pattern, e.getMessage());
        }
    }

    public boolean exists(String namespace, String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(buildKey(namespace, key)));
        } catch (Exception e) {
            logger.warn("Failed to check existence {}:{} Error: {}", namespace, key, e.getMessage());
            return false;
        }
    }

    // ==================== USER CACHE ====================

    public void evictUser(String userId) {
        evict(USER_CACHE, userId);
        logger.debug("Evicted user from cache: {}", userId);
    }

    // ==================== TOKEN INVALIDATION ====================

    public void invalidateUserTokens(String email) {
        revokeAllUserRefreshTokens(email);
        invalidateAllUserSessions(email);
        evictPattern(TOKEN_BLACKLIST, email + ":*");
        logger.info("All tokens invalidated for user: {}", email);
    }

    public void revokeAllUserRefreshTokens(String email) {
        try {
            evictPattern(REFRESH_TOKEN_CACHE, email + ":*");
            evict(REFRESH_TOKEN_CACHE, email);
            logger.info("All refresh tokens revoked for user: {}", email);
        } catch (Exception e) {
            logger.error("Failed to revoke refresh tokens for user: {}. Error: {}", email, e.getMessage());
        }
    }

    // ==================== SESSION MANAGEMENT ====================

    public void invalidateAllUserSessions(String username) {
        try {
            String userSessionsKey = buildKey(SESSION_CACHE, "user:" + username);
            Set<Object> sessionIds = redisTemplate.opsForSet().members(userSessionsKey);
            if (sessionIds != null) {
                for (Object sessionId : sessionIds) {
                    redisTemplate.delete(buildKey(SESSION_CACHE, sessionId.toString()));
                }
            }
            redisTemplate.delete(userSessionsKey);
            logger.info("All sessions invalidated for user: {}", username);
        } catch (Exception e) {
            logger.error("Failed to invalidate sessions for user: {}", username, e);
        }
    }

    // ==================== FAILED ATTEMPTS ====================

    public int incrementFailedAttempts(String email) {
        String key = buildKey(FAILED_ATTEMPTS_CACHE, email);
        Integer attempts = (Integer) redisTemplate.opsForValue().get(key);
        if (attempts == null) attempts = 0;
        attempts++;
        redisTemplate.opsForValue().set(key, attempts, 15, TimeUnit.MINUTES);
        logger.debug("Failed attempt {} for user: {}", attempts, email);
        return attempts;
    }

    public void clearFailedAttempts(String email) {
        redisTemplate.delete(buildKey(FAILED_ATTEMPTS_CACHE, email));
        logger.debug("Failed attempts cleared for user: {}", email);
    }

    public int getFailedAttempts(String email) {
        Integer attempts = (Integer) redisTemplate.opsForValue().get(buildKey(FAILED_ATTEMPTS_CACHE, email));
        return attempts != null ? attempts : 0;
    }

    // ==================== OTP MANAGEMENT ====================

    public void saveOtp(String key, String otp, long ttlMinutes) {
        put(OTP_CACHE, key, otp, ttlMinutes, TimeUnit.MINUTES);
        logger.debug("OTP saved for key: {}", key);
    }

    public Optional<String> getOtp(String key) {
        return get(OTP_CACHE, key, String.class);
    }

    public void deleteOtp(String key) {
        evict(OTP_CACHE, key);
    }

    public boolean validateOtp(String key, String otp) {
        return getOtp(key).map(storedOtp -> storedOtp.equals(otp)).orElse(false);
    }

    // ==================== PHONE OTP ====================

    public void savePhoneOtp(String phoneNumber, String otp, long ttlMinutes) {
        put(PHONE_OTP_CACHE, phoneNumber, otp, ttlMinutes, TimeUnit.MINUTES);
        logger.debug("Phone OTP saved for: {}", maskPhoneNumber(phoneNumber));
    }

    public boolean validatePhoneOtp(String phoneNumber, String otp) {
        return get(PHONE_OTP_CACHE, phoneNumber, String.class)
                .map(storedOtp -> storedOtp.equals(otp))
                .orElse(false);
    }

    // ==================== RATE LIMITING ====================

    public boolean isRateLimited(String key, int maxRequests, long windowSeconds) {
        String rateKey = buildKey(RATE_LIMIT_CACHE, key);
        Long count = redisTemplate.opsForValue().increment(rateKey);

        if (count == 1) {
            redisTemplate.expire(rateKey, windowSeconds, TimeUnit.SECONDS);
        }

        return count != null && count > maxRequests;
    }

    // ==================== Utility ====================

    private String buildKey(String namespace, String key) {
        return namespace + ":" + key;
    }

    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 8) return "***";
        return phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(phoneNumber.length() - 2);
    }
}