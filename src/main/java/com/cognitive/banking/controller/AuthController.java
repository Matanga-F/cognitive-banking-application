package com.cognitive.banking.controller;

import com.cognitive.banking.dto.AuthResponse;
import com.cognitive.banking.dto.LoginRequestDTO;
import com.cognitive.banking.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        UserDetails user = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(user);

        Date expiresAtDate = jwtService.extractExpiration(token);
        LocalDateTime expiresAt = expiresAtDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime issuedAt = LocalDateTime.now();

        return ResponseEntity.ok(new AuthResponse(token, issuedAt, expiresAt));
    }
}
