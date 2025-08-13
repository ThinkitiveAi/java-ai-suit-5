package com.healthfirst.provider.controller;

import com.healthfirst.provider.dto.ProviderRegistrationRequest;
import com.healthfirst.provider.dto.ProviderRegistrationResponse;
import com.healthfirst.provider.dto.ProviderLoginRequest;
import com.healthfirst.provider.dto.ProviderLoginResponse;
import com.healthfirst.provider.exception.ValidationException;
import com.healthfirst.provider.service.ProviderService;
import com.healthfirst.provider.service.ProviderLoginService;
import com.healthfirst.provider.util.RateLimiter;
import com.healthfirst.provider.entity.Provider;
import com.healthfirst.provider.repository.ProviderRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/provider")
public class ProviderController {
    @Autowired
    private ProviderService providerService;
    @Autowired
    private ProviderLoginService providerLoginService;
    @Autowired
    private RateLimiter rateLimiter;
    @Autowired
    private ProviderRepository providerRepository;

    @PostMapping("/register")
    public ResponseEntity<?> registerProvider(@RequestBody ProviderRegistrationRequest request, HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        if (!rateLimiter.isAllowed(ip)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new ProviderRegistrationResponse(false, "Too many registration attempts. Please try again later.", null));
        }
        ProviderRegistrationResponse response = providerService.registerProvider(request, ip);
        if (!response.isSuccess()) {
            if (response.getMessage().toLowerCase().contains("duplicate") || response.getMessage().toLowerCase().contains("already")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody ProviderLoginRequest request, HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        ProviderLoginResponse response = providerLoginService.login(request, ip, userAgent);
        if (!response.isSuccess()) {
            if ("ACCOUNT_LOCKED".equals(response.getErrorCode())) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        return ResponseEntity.ok(response);
    }

    // Testing endpoint to verify and activate provider
    @PostMapping("/test/verify")
    public ResponseEntity<?> verifyProviderForTesting(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Email is required"));
        }
        
        try {
            var providerOpt = providerRepository.findByEmail(email.trim());
            if (providerOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Provider provider = providerOpt.get();
            provider.setVerificationStatus(Provider.VerificationStatus.VERIFIED);
            provider.setActive(true);
            providerRepository.save(provider);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Provider verified and activated for testing",
                "provider_id", provider.getId().toString(),
                "email", provider.getEmail(),
                "verification_status", provider.getVerificationStatus().name(),
                "is_active", provider.isActive()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }
} 