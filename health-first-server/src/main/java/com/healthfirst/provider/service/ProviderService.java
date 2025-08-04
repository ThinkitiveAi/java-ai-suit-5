package com.healthfirst.provider.service;

import com.healthfirst.provider.dto.ProviderRegistrationRequest;
import com.healthfirst.provider.dto.ProviderRegistrationResponse;

public interface ProviderService {
    ProviderRegistrationResponse registerProvider(ProviderRegistrationRequest request, String ip);
} 