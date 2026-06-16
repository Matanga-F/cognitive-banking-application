package com.cognitive.banking.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.Serializable;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class RefreshTokenService {
    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);

    private final CacheService cacheService;

    @Value("${app.jwt.refresh-expiration-ms:604800000}")
    private long refreshTokenExpirationMs;

    // Explicit constructor to initialize cacheService
    public RefreshTokenService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public String createRefreshToken(String username) {
        String refreshToken = UUID.randomUUID().toString();

        RefreshTokenData tokenData = new RefreshTokenData(
                username,
                Instant.now().plusMillis(refreshTokenExpirationMs)
        );

        cacheService.put(CacheService.REFRESH_TOKEN_CACHE, refreshToken, tokenData,
                refreshTokenExpirationMs, TimeUnit.MILLISECONDS);

        // Also store mapping from username to token for easy revocation
        cacheService.put(CacheService.REFRESH_TOKEN_CACHE, username + ":token", refreshToken,
                refreshTokenExpirationMs, TimeUnit.MILLISECONDS);

        logger.info("Created refresh token for user: {}", username);
        return refreshToken;
    }

    public Optional<String> validateAndGetUsername(String refreshToken) {
        Optional<RefreshTokenData> tokenDataOpt =
                cacheService.get(CacheService.REFRESH_TOKEN_CACHE, refreshToken, RefreshTokenData.class);

        if (tokenDataOpt.isEmpty()) {
            logger.warn("Refresh token not found: {}", refreshToken);
            return Optional.empty();
        }

        RefreshTokenData tokenData = tokenDataOpt.get();
        if (tokenData.getExpiresAt().isBefore(Instant.now())) {
            logger.warn("Refresh token expired: {}", refreshToken);
            deleteRefreshToken(refreshToken);
            return Optional.empty();
        }

        logger.debug("Validated refresh token for user: {}", tokenData.getUsername());
        return Optional.of(tokenData.getUsername());
    }

    public void deleteRefreshToken(String refreshToken) {
        Optional<RefreshTokenData> tokenDataOpt =
                cacheService.get(CacheService.REFRESH_TOKEN_CACHE, refreshToken, RefreshTokenData.class);

        if (tokenDataOpt.isPresent()) {
            String username = tokenDataOpt.get().getUsername();
            cacheService.evict(CacheService.REFRESH_TOKEN_CACHE, refreshToken);
            cacheService.evict(CacheService.REFRESH_TOKEN_CACHE, username + ":token");
            logger.debug("Deleted refresh token for user: {}", username);
        }
    }

    public void deleteAllUserTokens(String username) {
        cacheService.revokeAllUserRefreshTokens(username);
        logger.debug("Deleted all refresh tokens for user: {}", username);
    }

    public boolean hasValidToken(String username) {
        return cacheService.exists(CacheService.REFRESH_TOKEN_CACHE, username + ":token");
    }

    // Serializable class for Redis storage
    public static class RefreshTokenData implements Serializable {
        private static final long serialVersionUID = 1L;

        private String username;
        private Instant expiresAt;

        public RefreshTokenData() {}

        public RefreshTokenData(String username, Instant expiresAt) {
            this.username = username;
            this.expiresAt = expiresAt;
        }

        public String getUsername() { return username; }
        public Instant getExpiresAt() { return expiresAt; }
        public void setUsername(String username) { this.username = username; }
        public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    }
}