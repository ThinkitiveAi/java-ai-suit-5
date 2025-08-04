package com.healthfirst.provider.service;

import com.healthfirst.provider.dto.PatientLoginRequest;
import com.healthfirst.provider.dto.PatientLoginResponse;

import java.util.UUID;

public interface PatientLoginService {
    PatientLoginResponse login(PatientLoginRequest request, String ip, String userAgent);
    PatientLoginResponse refreshToken(String refreshToken, String ip);
    boolean logout(String refreshToken, UUID patientId);
    boolean logoutAll(UUID patientId);
} 