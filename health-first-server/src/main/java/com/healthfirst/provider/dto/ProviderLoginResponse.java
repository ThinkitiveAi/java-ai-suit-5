package com.healthfirst.provider.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderLoginResponse {
    private boolean success;
    private String message;
    private Data data;
    private String errorCode;

    @lombok.Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {
        private String accessToken;
        private String refreshToken;
        private long expiresIn;
        private String tokenType;
        private ProviderInfo provider;
    }

    @lombok.Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProviderInfo {
        private String id;
        private String firstName;
        private String lastName;
        private String email;
        private String specialization;
        private String verificationStatus;
        private boolean isActive;
    }
} 