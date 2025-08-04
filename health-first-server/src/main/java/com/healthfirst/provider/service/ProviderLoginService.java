package com.healthfirst.provider.service;

import com.healthfirst.provider.dto.ProviderLoginRequest;
import com.healthfirst.provider.dto.ProviderLoginResponse;

public interface ProviderLoginService {
    ProviderLoginResponse login(ProviderLoginRequest request, String ip, String userAgent);
    ProviderLoginResponse refreshToken(String refreshToken, String ip);
} 