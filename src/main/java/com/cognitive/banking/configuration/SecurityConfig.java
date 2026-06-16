package com.cognitive.banking.configuration;

import com.cognitive.banking.security.JwtAuthenticationFilter;
import com.cognitive.banking.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@EnableAspectJAutoProxy
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtFilter;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for stateless REST APIs
                .csrf(csrf -> csrf.disable())

                // Configure CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Use stateless session management (no sessions)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Configure headers for security
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
                )

                // Configure authorization rules
                .authorizeHttpRequests(auth -> auth
                        // PUBLIC ENDPOINTS - NO AUTHENTICATION REQUIRED
                        // FIXED: Added both with and without the API base path
                        .requestMatchers(
                                // Auth endpoints with full path
                                "/api/cognitive/bank/auth/**",
                                "/api/cognitive/bank/auth/register",
                                "/api/cognitive/bank/auth/login",
                                "/api/cognitive/bank/auth/authenticate",
                                "/api/cognitive/bank/auth/forgot-password",
                                "/api/cognitive/bank/auth/reset-password",
                                "/api/cognitive/bank/auth/verify-email/**",
                                "/api/cognitive/bank/auth/send-verification-email",

                                // Auth endpoints without path (for flexibility)
                                "/auth/**",
                                "/auth/register",
                                "/auth/login",
                                "/auth/authenticate",
                                "/auth/forgot-password",
                                "/auth/reset-password",
                                "/auth/verify-email/**",
                                "/auth/send-verification-email",

                                // Actuator endpoints
                                "/actuator/health",
                                "/actuator/info",
                                "/actuator/health/**",
                                "/actuator/prometheus",

                                // Swagger UI and API docs
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/swagger-ui.html",
                                "/api-docs/**",

                                // H2 console (development only)
                                "/h2-console/**"
                        ).permitAll()

                        // Metrics endpoints - role-based access
//                        .requestMatchers("/actuator/metrics/**").hasRole("ADMIN")
//                        .requestMatchers("/actuator/prometheus").hasAnyRole("ADMIN", "AUDITOR")
//                        .requestMatchers("/actuator/**").hasRole("ADMIN")

                        // Admin-only endpoints
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/cognitive/bank/admin/**").hasRole("ADMIN")

                        // Auditor endpoints
                        .requestMatchers("/audit/**").hasAnyRole("ADMIN", "AUDITOR")
                        .requestMatchers("/api/cognitive/bank/audit/**").hasAnyRole("ADMIN", "AUDITOR")

                        // Account manager endpoints
                        .requestMatchers("/accounts/manage/**").hasAnyRole("ADMIN", "ACCOUNT_MANAGER")
                        .requestMatchers("/api/cognitive/bank/accounts/manage/**").hasAnyRole("ADMIN", "ACCOUNT_MANAGER")

                        // Loan officer endpoints
                        .requestMatchers("/loans/approve/**").hasAnyRole("ADMIN", "LOAN_OFFICER")
                        .requestMatchers("/api/cognitive/bank/loans/approve/**").hasAnyRole("ADMIN", "LOAN_OFFICER")

                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )

                // Configure authentication provider
                .authenticationProvider(authenticationProvider())

                // Add JWT filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                // Configure exception handling
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json");
                            response.setStatus(401);
                            response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"" + authException.getMessage() + "\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json");
                            response.setStatus(403);
                            response.getWriter().write("{\"error\":\"Forbidden\",\"message\":\"" + accessDeniedException.getMessage() + "\"}");
                        })
                );

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setHideUserNotFoundExceptions(false);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow specific origins
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:4200",
                "http://localhost:8080",
                "http://localhost:8081",
                "https://your-domain.com"
        ));

        // Allow specific HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"
        ));

        // Allow specific headers
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers",
                "X-Correlation-ID",
                "X-Trace-ID"
        ));

        // Allow credentials
        configuration.setAllowCredentials(true);

        // Expose headers to client
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "X-Correlation-ID",
                "X-Trace-ID"
        ));

        // Max age of pre-flight request cache (1 hour)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}