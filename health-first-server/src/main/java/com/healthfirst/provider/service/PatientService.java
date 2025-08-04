package com.healthfirst.provider.service;

import com.healthfirst.provider.dto.PatientRegistrationRequest;
import com.healthfirst.provider.dto.PatientRegistrationResponse;

public interface PatientService {
    PatientRegistrationResponse registerPatient(PatientRegistrationRequest request, String ip);
} 