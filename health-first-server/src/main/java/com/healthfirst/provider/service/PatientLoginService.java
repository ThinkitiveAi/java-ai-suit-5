package com.healthfirst.provider.service;

import com.healthfirst.provider.dto.PatientLoginRequest;
import com.healthfirst.provider.dto.PatientLoginResponse;
import com.healthfirst.provider.entity.Patient;
import com.healthfirst.provider.entity.PatientSession;
import com.healthfirst.provider.repository.PatientRepository;
import com.healthfirst.provider.repository.PatientSessionRepository;
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
public class PatientLoginService {
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private PatientSessionRepository patientSessionRepository;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private LoginRateLimiter loginRateLimiter;

    private static final long ACCESS_TOKEN_EXPIRY = 1800; // 30 minutes
    private static final long ACCESS_TOKEN_EXPIRY_REMEMBER = 14400; // 4 hours
    private static final long REFRESH_TOKEN_EXPIRY = 604800; // 7 days
    private static final long REFRESH_TOKEN_EXPIRY_REMEMBER = 2592000; // 30 days
    private static final int MAX_SESSIONS = 3;

    @Transactional
    public PatientLoginResponse login(PatientLoginRequest req, String ip, String userAgent) {
        String identifier = req.getIdentifier().trim();
        String password = req.getPassword();
        boolean rememberMe = req.getRememberMe() != null && req.getRememberMe();
        String deviceInfo = req.getDeviceInfo() != null ? req.getDeviceInfo() : "Unknown Device";
        String rateKey = identifier + ":" + ip;

        // Rate limit and lockout
        if (!loginRateLimiter.isAllowed(rateKey)) {
            return PatientLoginResponse.builder()
                    .success(false)
                    .message("Too many failed login attempts. Account locked.")
                    .errorCode("ACCOUNT_LOCKED")
                    .build();
        }

        Optional<Patient> patientOpt = identifier.contains("@") ?
                patientRepository.findByEmail(identifier) :
                patientRepository.findByPhoneNumber(identifier);
        
        if (patientOpt.isEmpty()) {
            return failAttempt(rateKey, "Invalid credentials");
        }
        
        Patient patient = patientOpt.get();
        
        if (!PasswordUtils.matches(password, patient.getPasswordHash())) {
            return failAttempt(rateKey, "Invalid credentials");
        }
        
        // Pre-login checks
        if (!patient.isEmailVerified()) {
            return PatientLoginResponse.builder()
                    .success(false)
                    .message("Email not verified. Please verify your email first.")
                    .errorCode("EMAIL_NOT_VERIFIED")
                    .build();
        }
        
        if (!patient.isActive()) {
            return PatientLoginResponse.builder()
                    .success(false)
                    .message("Account is deactivated.")
                    .errorCode("ACCOUNT_DEACTIVATED")
                    .build();
        }
        
        if (patient.getAccountLockedUntil() != null && 
            patient.getAccountLockedUntil().isAfter(OffsetDateTime.now())) {
            return PatientLoginResponse.builder()
                    .success(false)
                    .message("Account is locked. Please try again later.")
                    .errorCode("ACCOUNT_LOCKED")
                    .build();
        }

        // Success: reset rate limiter and failed login count
        loginRateLimiter.reset(rateKey);
        patient.setFailedLoginCount(0);
        patient.setAccountLockedUntil(null);
        
        // Update login stats
        patient.setLastLogin(OffsetDateTime.now());
        patient.setLoginCount(patient.getLoginCount() + 1);
        patient.setLastLoginIp(ip);
        patient.setLastLoginDevice(deviceInfo);
        patientRepository.save(patient);

        // Check session limit
        long activeSessions = patientSessionRepository.countActiveSessionsByPatientId(patient.getId());
        if (activeSessions >= MAX_SESSIONS) {
            // Deactivate oldest session
            patientSessionRepository.findActiveSessionsByPatientId(patient.getId())
                    .stream()
                    .sorted((s1, s2) -> s1.getCreatedAt().compareTo(s2.getCreatedAt()))
                    .findFirst()
                    .ifPresent(oldSession -> {
                        oldSession.setActive(false);
                        patientSessionRepository.save(oldSession);
                    });
        }

        // Generate tokens
        long accessExpiry = rememberMe ? ACCESS_TOKEN_EXPIRY_REMEMBER : ACCESS_TOKEN_EXPIRY;
        long refreshExpiry = rememberMe ? REFRESH_TOKEN_EXPIRY_REMEMBER : REFRESH_TOKEN_EXPIRY;
        
        Map<String, Object> accessClaims = new HashMap<>();
        accessClaims.put("patient_id", patient.getId().toString());
        accessClaims.put("email", patient.getEmail());
        accessClaims.put("role", "patient");
        
        String accessToken = jwtUtils.generateToken(accessClaims, accessExpiry);
        String refreshToken = jwtUtils.generateToken(Map.of("patient_id", patient.getId().toString()), refreshExpiry);
        
        // Store refresh token
        PatientSession session = PatientSession.builder()
                .patientId(patient.getId())
                .tokenHash(PasswordUtils.hashPassword(refreshToken)) // Hash the refresh token
                .deviceInfo(deviceInfo)
                .ipAddress(ip)
                .userAgent(userAgent)
                .expiresAt(OffsetDateTime.now().plusSeconds(refreshExpiry))
                .build();
        patientSessionRepository.save(session);

        return PatientLoginResponse.builder()
                .success(true)
                .message("Login successful")
                .data(PatientLoginResponse.Data.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .expiresIn(accessExpiry)
                        .tokenType("Bearer")
                        .patient(PatientLoginResponse.PatientInfo.builder()
                                .id(patient.getId().toString())
                                .firstName(patient.getFirstName())
                                .lastName(patient.getLastName())
                                .email(patient.getEmail())
                                .phoneNumber(patient.getPhoneNumber())
                                .emailVerified(patient.isEmailVerified())
                                .phoneVerified(patient.isPhoneVerified())
                                .isActive(patient.isActive())
                                .build())
                        .build())
                .build();
    }

    private PatientLoginResponse failAttempt(String rateKey, String message) {
        // The isAllowed method already records the attempt when called
        return PatientLoginResponse.builder()
                .success(false)
                .message(message)
                .errorCode("INVALID_CREDENTIALS")
                .build();
    }

    @Transactional
    public PatientLoginResponse refreshToken(String refreshToken, String ip) {
        try {
            var claims = jwtUtils.parseToken(refreshToken);
            String patientId = claims.getBody().get("patient_id", String.class);
            
            Optional<PatientSession> sessionOpt = patientSessionRepository.findByTokenHashAndActive(
                    PasswordUtils.hashPassword(refreshToken), OffsetDateTime.now());
            
            if (sessionOpt.isEmpty()) {
                return PatientLoginResponse.builder()
                        .success(false)
                        .message("Invalid or expired refresh token")
                        .errorCode("INVALID_REFRESH_TOKEN")
                        .build();
            }
            
            PatientSession session = sessionOpt.get();
            if (!session.getPatientId().toString().equals(patientId)) {
                return PatientLoginResponse.builder()
                        .success(false)
                        .message("Token mismatch")
                        .errorCode("TOKEN_MISMATCH")
                        .build();
            }
            
            // Generate new access token
            Map<String, Object> accessClaims = new HashMap<>();
            accessClaims.put("patient_id", patientId);
            accessClaims.put("role", "patient");
            
            String newAccessToken = jwtUtils.generateToken(accessClaims, ACCESS_TOKEN_EXPIRY);
            
            return PatientLoginResponse.builder()
                    .success(true)
                    .message("Token refreshed successfully")
                    .data(PatientLoginResponse.Data.builder()
                            .accessToken(newAccessToken)
                            .refreshToken(refreshToken) // Keep same refresh token
                            .expiresIn(ACCESS_TOKEN_EXPIRY)
                            .tokenType("Bearer")
                            .build())
                    .build();
            
        } catch (Exception e) {
            return PatientLoginResponse.builder()
                    .success(false)
                    .message("Invalid refresh token")
                    .errorCode("INVALID_REFRESH_TOKEN")
                    .build();
        }
    }

    @Transactional
    public boolean logout(String refreshToken, UUID patientId) {
        try {
            Optional<PatientSession> sessionOpt = patientSessionRepository.findByTokenHashAndActive(
                    PasswordUtils.hashPassword(refreshToken), OffsetDateTime.now());
            
            if (sessionOpt.isPresent() && sessionOpt.get().getPatientId().equals(patientId)) {
                PatientSession session = sessionOpt.get();
                session.setActive(false);
                patientSessionRepository.save(session);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional
    public boolean logoutAll(UUID patientId) {
        try {
            patientSessionRepository.deactivateAllSessionsByPatientId(patientId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
} 