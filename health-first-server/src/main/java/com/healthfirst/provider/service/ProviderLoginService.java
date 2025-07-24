package com.healthfirst.provider.service;

import com.healthfirst.provider.dto.ProviderLoginRequest;
import com.healthfirst.provider.dto.ProviderLoginResponse;
import com.healthfirst.provider.entity.Provider;
import com.healthfirst.provider.repository.ProviderRepository;
import com.healthfirst.provider.util.JwtUtils;
import com.healthfirst.provider.util.LoginRateLimiter;
import com.healthfirst.provider.util.PasswordUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProviderLoginService {
    @Autowired
    private ProviderRepository providerRepository;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private LoginRateLimiter loginRateLimiter;

    private static final long ACCESS_TOKEN_EXPIRY = 3600; // 1 hour
    private static final long ACCESS_TOKEN_EXPIRY_REMEMBER = 86400; // 24h
    private static final long REFRESH_TOKEN_EXPIRY = 604800; // 7 days
    private static final long REFRESH_TOKEN_EXPIRY_REMEMBER = 2592000; // 30d

    @Transactional
    public ProviderLoginResponse login(ProviderLoginRequest req, String ip) {
        String identifier = req.getIdentifier().trim();
        String password = req.getPassword();
        boolean rememberMe = req.getRememberMe() != null && req.getRememberMe();
        String rateKey = identifier + ":" + ip;

        // Rate limit and lockout
        if (!loginRateLimiter.isAllowed(rateKey)) {
            return ProviderLoginResponse.builder()
                    .success(false)
                    .message("Too many failed login attempts. Account locked.")
                    .errorCode("ACCOUNT_LOCKED")
                    .build();
        }

        Optional<Provider> providerOpt = identifier.contains("@") ?
                providerRepository.findByEmail(identifier) :
                providerRepository.findByPhoneNumber(identifier);
        if (providerOpt.isEmpty()) {
            return failAttempt(rateKey, "Invalid credentials");
        }
        Provider provider = providerOpt.get();
        if (!PasswordUtils.matches(password, provider.getPasswordHash())) {
            return failAttempt(rateKey, "Invalid credentials");
        }
        if (!provider.isActive() || provider.getVerificationStatus() != Provider.VerificationStatus.VERIFIED) {
            return ProviderLoginResponse.builder()
                    .success(false)
                    .message("Account is not active or not verified.")
                    .errorCode("ACCOUNT_INACTIVE_OR_UNVERIFIED")
                    .build();
        }
        // Success: reset rate limiter
        loginRateLimiter.reset(rateKey);
        // Update login stats
        provider.setLastLogin(OffsetDateTime.now());
        provider.setLoginCount(provider.getLoginCount() + 1);
        providerRepository.save(provider);
        // JWT claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("provider_id", provider.getId().toString());
        claims.put("email", provider.getEmail());
        claims.put("role", "provider");
        claims.put("specialization", provider.getSpecialization());
        claims.put("verification_status", provider.getVerificationStatus().name().toLowerCase());
        long accessExpiry = rememberMe ? ACCESS_TOKEN_EXPIRY_REMEMBER : ACCESS_TOKEN_EXPIRY;
        long refreshExpiry = rememberMe ? REFRESH_TOKEN_EXPIRY_REMEMBER : REFRESH_TOKEN_EXPIRY;
        String accessToken = jwtUtils.generateToken(claims, accessExpiry);
        String refreshToken = jwtUtils.generateToken(Map.of("provider_id", provider.getId().toString()), refreshExpiry);
        // TODO: Store hashed refresh token in DB, implement rotation/blacklist
        return ProviderLoginResponse.builder()
                .success(true)
                .message("Login successful")
                .data(ProviderLoginResponse.Data.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .expiresIn(accessExpiry)
                        .tokenType("Bearer")
                        .provider(ProviderLoginResponse.ProviderInfo.builder()
                                .id(provider.getId().toString())
                                .firstName(provider.getFirstName())
                                .lastName(provider.getLastName())
                                .email(provider.getEmail())
                                .specialization(provider.getSpecialization())
                                .verificationStatus(provider.getVerificationStatus().name().toLowerCase())
                                .isActive(provider.isActive())
                                .build())
                        .build())
                .build();
    }

    private ProviderLoginResponse failAttempt(String rateKey, String msg) {
        // Increment failed attempt
        loginRateLimiter.isAllowed(rateKey); // increments count
        return ProviderLoginResponse.builder()
                .success(false)
                .message(msg)
                .errorCode("INVALID_CREDENTIALS")
                .build();
    }
} 