package com.healthfirst.provider.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientLoginRequest {
    @NotBlank(message = "Identifier (email or phone) is required")
    private String identifier;
    
    @NotBlank(message = "Password is required")
    private String password;
    
    private Boolean rememberMe;
    
    private String deviceInfo;
} 