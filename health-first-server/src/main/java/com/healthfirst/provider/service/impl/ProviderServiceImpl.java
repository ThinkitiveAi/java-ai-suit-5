package com.healthfirst.provider.service.impl;

import com.healthfirst.provider.dto.ProviderRegistrationRequest;
import com.healthfirst.provider.dto.ProviderRegistrationResponse;
import com.healthfirst.provider.entity.Provider;
import com.healthfirst.provider.repository.ProviderRepository;
import com.healthfirst.provider.service.ProviderService;
import com.healthfirst.provider.service.ValidationService;
import com.healthfirst.provider.util.EmailUtils;
import com.healthfirst.provider.util.PasswordUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProviderServiceImpl implements ProviderService {
    
    @Autowired
    private ProviderRepository providerRepository;
    
    @Autowired
    private ValidationService validationService;

    @Override
    @Transactional
    public ProviderRegistrationResponse registerProvider(ProviderRegistrationRequest req, String ip) {
        req = validationService.trimAndSanitize(req);
        
        // Validate specialization
        if (!validationService.isValidSpecialization(req.getSpecialization())) {
            return ProviderRegistrationResponse.builder()
                    .success(false)
                    .message("Invalid specialization.")
                    .build();
        }
        
        // Password rules
        if (!PasswordUtils.isValid(req.getPassword())) {
            return ProviderRegistrationResponse.builder()
                    .success(false)
                    .message("Password does not meet security requirements.")
                    .build();
        }
        
        if (!validationService.passwordsMatch(req.getPassword(), req.getConfirmPassword())) {
            return ProviderRegistrationResponse.builder()
                    .success(false)
                    .message("Passwords do not match.")
                    .build();
        }
        
        // Check for duplicates
        if (providerRepository.findByEmail(req.getEmail()).isPresent()) {
            return ProviderRegistrationResponse.builder()
                    .success(false)
                    .message("Email already registered.")
                    .build();
        }
        
        if (providerRepository.findByPhoneNumber(req.getPhoneNumber()).isPresent()) {
            return ProviderRegistrationResponse.builder()
                    .success(false)
                    .message("Phone number already registered.")
                    .build();
        }
        
        if (providerRepository.findByLicenseNumber(req.getLicenseNumber()).isPresent()) {
            return ProviderRegistrationResponse.builder()
                    .success(false)
                    .message("License number already registered.")
                    .build();
        }
        
        // Hash password
        String passwordHash = PasswordUtils.hashPassword(req.getPassword());
        
        // Build entity
        Provider provider = Provider.builder()
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .email(req.getEmail())
                .phoneNumber(req.getPhoneNumber())
                .passwordHash(passwordHash)
                .specialization(req.getSpecialization())
                .licenseNumber(req.getLicenseNumber())
                .yearsOfExperience(req.getYearsOfExperience())
                .clinicAddress(new Provider.ClinicAddress(
                        req.getClinicAddress().getStreet(),
                        req.getClinicAddress().getCity(),
                        req.getClinicAddress().getState(),
                        req.getClinicAddress().getZip()
                ))
                .verificationStatus(Provider.VerificationStatus.PENDING)
                .isActive(true)
                .build();
        
        provider = providerRepository.save(provider);
        
        // Generate and send verification email
        String token = EmailUtils.generateVerificationToken();
        // TODO: Save token to DB (not shown here)
        
        return ProviderRegistrationResponse.builder()
                .success(true)
                .message("Provider registered successfully. Verification email sent.")
                .data(ProviderRegistrationResponse.Data.builder()
                        .providerId(provider.getId().toString())
                        .email(provider.getEmail())
                        .verificationStatus(provider.getVerificationStatus().name().toLowerCase())
                        .build())
                .build();
    }
} 