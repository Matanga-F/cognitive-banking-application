package com.cognitive.banking.dto;

import java.time.LocalDateTime;

public class AuthResponse {

    private String token;
    private String tokenType;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;

    public AuthResponse() {}

    public AuthResponse(String token, LocalDateTime issuedAt, LocalDateTime expiresAt) {
        this.token = token;
        this.tokenType = "Bearer";
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }

    // Getters and setters
    public String getToken() { return token; }
    public String getTokenType() { return tokenType; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setToken(String token) { this.token = token; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}


