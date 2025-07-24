package com.healthfirst.provider.controller;

import com.healthfirst.provider.dto.PatientRegistrationRequest;
import com.healthfirst.provider.dto.PatientRegistrationResponse;
import com.healthfirst.provider.service.PatientService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/patient")
public class PatientController {
    @Autowired
    private PatientService patientService;

    private final Map<String, Integer> attempts = new ConcurrentHashMap<>();
    private final Map<String, Long> lastAttempt = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 3;
    private static final long WINDOW_MILLIS = 60 * 60 * 1000; // 1 hour

    @PostMapping("/register")
    public ResponseEntity<?> registerPatient(@Valid @RequestBody PatientRegistrationRequest request, HttpServletRequest httpRequest) {
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
} 