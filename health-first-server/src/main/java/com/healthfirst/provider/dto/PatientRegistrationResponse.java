package com.healthfirst.provider.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientRegistrationResponse {
    private boolean success;
    private String message;
    private Data data;

    @lombok.Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {
        private String patientId;
        private String email;
        private String phoneNumber;
        private boolean emailVerified;
        private boolean phoneVerified;
    }
} 