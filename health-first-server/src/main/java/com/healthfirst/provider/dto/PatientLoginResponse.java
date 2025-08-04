package com.healthfirst.provider.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientLoginResponse {
    private boolean success;
    private String message;
    private String errorCode;
    private Data data;
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getErrorCode() {
        return errorCode;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class Data {
        private String accessToken;
        private String refreshToken;
        private long expiresIn;
        private String tokenType;
        private PatientInfo patient;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PatientInfo {
        private String id;
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        private boolean emailVerified;
        private boolean phoneVerified;
        private boolean isActive;
    }
} 