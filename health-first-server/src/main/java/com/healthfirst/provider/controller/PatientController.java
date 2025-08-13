package com.healthfirst.provider.controller;

import com.healthfirst.provider.dto.PatientRegistrationRequest;
import com.healthfirst.provider.dto.PatientRegistrationResponse;
import com.healthfirst.provider.dto.PatientLoginRequest;
import com.healthfirst.provider.dto.PatientLoginResponse;
import com.healthfirst.provider.service.PatientService;
import com.healthfirst.provider.service.PatientLoginService;
import com.healthfirst.provider.entity.PatientSession;
import com.healthfirst.provider.repository.PatientSessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/patient")
public class PatientController {
    @Autowired
    private PatientService patientService;
    
    @Autowired
    private PatientLoginService patientLoginService;
    
    @Autowired
    private PatientSessionRepository patientSessionRepository;

    private final Map<String, Integer> attempts = new ConcurrentHashMap<>();
    private final Map<String, Long> lastAttempt = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 3;
    private static final long WINDOW_MILLIS = 60 * 60 * 1000; // 1 hour

    @PostMapping("/register")
    public ResponseEntity<?> registerPatient(@RequestBody PatientRegistrationRequest request, HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        long now = System.currentTimeMillis();
        lastAttempt.putIfAbsent(ip, now);
        if (now - lastAttempt.get(ip) > WINDOW_MILLIS) {
            attempts.put(ip, 0);
            lastAttempt.put(ip, now);
        }
        attempts.putIfAbsent(ip, 0);
        if (attempts.get(ip) >= MAX_ATTEMPTS) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("success", false, "message", "Too many registration attempts. Please try again later."));
        }
        attempts.put(ip, attempts.get(ip) + 1);
        PatientRegistrationResponse response = patientService.registerPatient(request, ip);
        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody PatientLoginRequest request, HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        
        PatientLoginResponse response = patientLoginService.login(request, ip, userAgent);
        
        if (!response.isSuccess()) {
            if ("ACCOUNT_LOCKED".equals(response.getErrorCode())) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        String refreshToken = request.get("refresh_token");
        String ip = httpRequest.getRemoteAddr();
        
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Refresh token is required"));
        }
        
        PatientLoginResponse response = patientLoginService.refreshToken(refreshToken, ip);
        
        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        String refreshToken = request.get("refresh_token");
        String patientIdStr = request.get("patient_id");
        
        if (refreshToken == null || patientIdStr == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Refresh token and patient_id are required"));
        }
        
        try {
            UUID patientId = UUID.fromString(patientIdStr);
            boolean success = patientLoginService.logout(refreshToken, patientId);
            
            if (success) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Logged out successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Invalid token"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid patient_id format"));
        }
    }

    @PostMapping("/logout-all")
    public ResponseEntity<?> logoutAll(@RequestBody Map<String, String> request) {
        String patientIdStr = request.get("patient_id");
        String password = request.get("password");
        
        if (patientIdStr == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Patient ID and password are required"));
        }
        
        try {
            UUID patientId = UUID.fromString(patientIdStr);
            // TODO: Verify password before logout all
            boolean success = patientLoginService.logoutAll(patientId);
            
            if (success) {
                return ResponseEntity.ok(Map.of("success", true, "message", "All sessions logged out successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "Failed to logout all sessions"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid patient_id format"));
        }
    }

    @GetMapping("/sessions")
    public ResponseEntity<?> getSessions(@RequestParam String patientId) {
        try {
            UUID patientUUID = UUID.fromString(patientId);
            List<PatientSession> sessions = patientSessionRepository.findActiveSessionsByPatientId(patientUUID);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "sessions", sessions.stream().map(session -> Map.of(
                    "id", session.getId().toString(),
                    "deviceInfo", session.getDeviceInfo(),
                    "ipAddress", session.getIpAddress(),
                    "createdAt", session.getCreatedAt(),
                    "expiresAt", session.getExpiresAt()
                )).toList()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid patient_id format"));
        }
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<?> revokeSession(@PathVariable String sessionId, @RequestParam String patientId) {
        try {
            UUID sessionUUID = UUID.fromString(sessionId);
            UUID patientUUID = UUID.fromString(patientId);
            
            patientSessionRepository.deactivateSessionByIdAndPatientId(sessionUUID, patientUUID);
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Session revoked successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid UUID format"));
        }
    }
} 