package com.healthfirst.provider.service;

import com.healthfirst.provider.dto.ProviderRegistrationRequest;

public interface ValidationService {
    ProviderRegistrationRequest trimAndSanitize(ProviderRegistrationRequest request);
    boolean isValidSpecialization(String specialization);
    boolean passwordsMatch(String password, String confirmPassword);
} 