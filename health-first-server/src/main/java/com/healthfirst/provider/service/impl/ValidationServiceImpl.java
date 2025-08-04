package com.healthfirst.provider.service.impl;

import com.healthfirst.provider.dto.ProviderRegistrationRequest;
import com.healthfirst.provider.service.ValidationService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
public class ValidationServiceImpl implements ValidationService {
    
    private static final Set<String> SPECIALIZATIONS = new HashSet<>(Arrays.asList(
            "Cardiology", "Dermatology", "Neurology", "Pediatrics", "Psychiatry", 
            "Oncology", "Orthopedics", "General Medicine"
    ));

    @Override
    public boolean isValidSpecialization(String specialization) {
        return SPECIALIZATIONS.contains(specialization);
    }

    @Override
    public boolean passwordsMatch(String password, String confirmPassword) {
        return password != null && password.equals(confirmPassword);
    }

    @Override
    public ProviderRegistrationRequest trimAndSanitize(ProviderRegistrationRequest req) {
        req.setFirstName(trim(req.getFirstName()));
        req.setLastName(trim(req.getLastName()));
        req.setEmail(trim(req.getEmail()));
        req.setPhoneNumber(trim(req.getPhoneNumber()));
        req.setSpecialization(trim(req.getSpecialization()));
        req.setLicenseNumber(trim(req.getLicenseNumber()));
        
        if (req.getClinicAddress() != null) {
            req.getClinicAddress().setStreet(trim(req.getClinicAddress().getStreet()));
            req.getClinicAddress().setCity(trim(req.getClinicAddress().getCity()));
            req.getClinicAddress().setState(trim(req.getClinicAddress().getState()));
            req.getClinicAddress().setZip(trim(req.getClinicAddress().getZip()));
        }
        
        return req;
    }

    private String trim(String s) {
        return s == null ? null : s.trim();
    }
} 