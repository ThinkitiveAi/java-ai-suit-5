package com.healthfirst.provider.service.impl;

import com.healthfirst.provider.dto.PatientRegistrationRequest;
import com.healthfirst.provider.dto.PatientRegistrationResponse;
import com.healthfirst.provider.entity.Patient;
import com.healthfirst.provider.repository.PatientRepository;
import com.healthfirst.provider.service.PatientService;
import com.healthfirst.provider.util.EmailUtils;
import com.healthfirst.provider.util.PasswordUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

@Service
public class PatientServiceImpl implements PatientService {
    
    @Autowired
    private PatientRepository patientRepository;

    @Override
    @Transactional
    public PatientRegistrationResponse registerPatient(PatientRegistrationRequest req, String ip) {
        // Normalize and sanitize
        req.setFirstName(trim(req.getFirstName()));
        req.setLastName(trim(req.getLastName()));
        req.setEmail(trim(req.getEmail()).toLowerCase());
        req.setPhoneNumber(trim(req.getPhoneNumber()));
        
        if (req.getAddress() != null) {
            req.getAddress().setStreet(trim(req.getAddress().getStreet()));
            req.getAddress().setCity(trim(req.getAddress().getCity()));
            req.getAddress().setState(trim(req.getAddress().getState()));
            req.getAddress().setZip(trim(req.getAddress().getZip()));
        }
        
        if (req.getEmergencyContact() != null) {
            req.getEmergencyContact().setName(trim(req.getEmergencyContact().getName()));
            req.getEmergencyContact().setPhone(trim(req.getEmergencyContact().getPhone()));
            req.getEmergencyContact().setRelationship(trim(req.getEmergencyContact().getRelationship()));
        }
        
        if (req.getInsuranceInfo() != null) {
            req.getInsuranceInfo().setProvider(trim(req.getInsuranceInfo().getProvider()));
            req.getInsuranceInfo().setPolicyNumber(trim(req.getInsuranceInfo().getPolicyNumber()));
        }
        
        /*
        // Validate age
        if (!isValidAge(req.getDateOfBirth())) {
            return PatientRegistrationResponse.builder()
                    .success(false)
                    .message("Patient must be at least 13 years old.")
                    .build();
        }
        
        // Check for duplicates
        if (patientRepository.findByEmail(req.getEmail()).isPresent()) {
            return PatientRegistrationResponse.builder()
                    .success(false)
                    .message("Email already registered.")
                    .build();
        }
        
        if (patientRepository.findByPhoneNumber(req.getPhoneNumber()).isPresent()) {
            return PatientRegistrationResponse.builder()
                    .success(false)
                    .message("Phone number already registered.")
                    .build();
        }
        
        // Password rules
        if (!PasswordUtils.isValid(req.getPassword())) {
            return PatientRegistrationResponse.builder()
                    .success(false)
                    .message("Password does not meet security requirements.")
                    .build();
        }
        
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            return PatientRegistrationResponse.builder()
                    .success(false)
                    .message("Passwords do not match.")
                    .build();
        }
        */
        
        // Hash password
        String passwordHash = PasswordUtils.hashPassword(req.getPassword());
        
        // Build entity
        Patient patient = Patient.builder()
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .email(req.getEmail())
                .phoneNumber(req.getPhoneNumber())
                .passwordHash(passwordHash)
                .dateOfBirth(req.getDateOfBirth())
                .gender(req.getGender().toLowerCase())
                .address(req.getAddress() != null ? new Patient.Address(
                        req.getAddress().getStreet(),
                        req.getAddress().getCity(),
                        req.getAddress().getState(),
                        req.getAddress().getZip()) : null)
                .emergencyContact(req.getEmergencyContact() != null ? new Patient.EmergencyContact(
                        req.getEmergencyContact().getName(),
                        req.getEmergencyContact().getPhone(),
                        req.getEmergencyContact().getRelationship()) : null)
                .medicalHistory(req.getMedicalHistory())
                .insuranceInfo(req.getInsuranceInfo() != null ? new Patient.InsuranceInfo(
                        req.getInsuranceInfo().getProvider(),
                        req.getInsuranceInfo().getPolicyNumber()) : null)
                .emailVerified(false)
                .phoneVerified(false)
                .isActive(true)
                .marketingOptIn(Boolean.TRUE.equals(req.getMarketingOptIn()))
                .build();
        
        patient = patientRepository.save(patient);
        
        // Generate and send verification email
        String token = EmailUtils.generateVerificationToken();
        // TODO: Save token to DB (not shown here)
        
        return PatientRegistrationResponse.builder()
                .success(true)
                .message("Patient registered successfully. Verification email sent.")
                .data(PatientRegistrationResponse.Data.builder()
                        .patientId(patient.getId().toString())
                        .email(patient.getEmail())
                        .phoneNumber(patient.getPhoneNumber())
                        .emailVerified(false)
                        .phoneVerified(false)
                        .build())
                .build();
    }
    
    private boolean isValidAge(LocalDate dob) {
        if (dob == null) return false;
        return Period.between(dob, LocalDate.now()).getYears() >= 13;
    }
    
    private String trim(String s) {
        return s == null ? null : s.trim();
    }
} 