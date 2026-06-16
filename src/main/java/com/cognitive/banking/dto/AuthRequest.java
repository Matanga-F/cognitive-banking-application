package com.cognitive.banking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @JsonProperty("email")
    private String email;

    @NotBlank(message = "Password is required")
    @JsonProperty("password")
    private String password;

    @JsonProperty("two_factor_code")
    private String twoFactorCode;

    @JsonProperty("remember_me")
    private boolean rememberMe;

    // Constructors
    public AuthRequest() {}

    public AuthRequest(String email, String password, String twoFactorCode, boolean rememberMe) {
        this.email = email;
        this.password = password;
        this.twoFactorCode = twoFactorCode;
        this.rememberMe = rememberMe;
    }

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getTwoFactorCode() { return twoFactorCode; }
    public void setTwoFactorCode(String twoFactorCode) { this.twoFactorCode = twoFactorCode; }

    public boolean isRememberMe() { return rememberMe; }
    public void setRememberMe(boolean rememberMe) { this.rememberMe = rememberMe; }
}