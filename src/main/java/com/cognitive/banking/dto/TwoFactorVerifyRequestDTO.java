// TwoFactorVerifyRequestDTO.java
package com.cognitive.banking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TwoFactorVerifyRequestDTO {

    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "2FA code is required")
    @Size(min = 6, max = 6, message = "2FA code must be exactly 6 digits")
    @Pattern(regexp = "^[0-9]{6}$", message = "2FA code must contain only digits")
    private String code;

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}