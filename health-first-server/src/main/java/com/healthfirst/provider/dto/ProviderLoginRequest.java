package com.healthfirst.provider.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProviderLoginRequest {
    @NotBlank
    private String identifier; // email or phone
    @NotBlank
    private String password;
    private Boolean rememberMe = false;
} 