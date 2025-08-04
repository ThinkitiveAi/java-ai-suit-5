package com.healthfirst.provider.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class PatientRegistrationRequest {
    @NotBlank
    @Size(min = 2, max = 50)
    private String firstName;
    @NotBlank
    @Size(min = 2, max = 50)
    private String lastName;
    @NotBlank
    @Email
    private String email;
    @NotBlank
    @Pattern(regexp = "^\\+\\d{10,15}$", message = "Phone number must be in E.164 format")
    private String phoneNumber;
    @NotBlank
    @Size(min = 8, max = 100)
    private String password;
    @NotBlank
    @Size(min = 8, max = 100)
    private String confirmPassword;
    @NotNull
    @Past
    private LocalDate dateOfBirth;
    @NotBlank
    @Pattern(regexp = "^(male|female|other|prefer_not_to_say)$", flags = Pattern.Flag.CASE_INSENSITIVE)
    private String gender;
    @Valid
    private Address address;
    @Valid
    private EmergencyContact emergencyContact;
    private List<String> medicalHistory;
    @Valid
    private InsuranceInfo insuranceInfo;
    private Boolean marketingOptIn = false;

    @Data
    public static class Address {
        @NotBlank
        @Size(max = 200)
        private String street;
        @NotBlank
        @Size(max = 100)
        private String city;
        @NotBlank
        @Size(max = 50)
        private String state;
        @NotBlank
        @Size(max = 20)
        private String zip;
    }
    @Data
    public static class EmergencyContact {
        @NotBlank
        @Size(max = 100)
        private String name;
        @NotBlank
        @Pattern(regexp = "^\\+\\d{10,15}$", message = "Phone number must be in E.164 format")
        private String phone;
        @NotBlank
        @Size(max = 50)
        private String relationship;
    }
    @Data
    public static class InsuranceInfo {
        @NotBlank
        @Size(max = 100)
        private String provider;
        @NotBlank
        @Size(max = 50)
        private String policyNumber;
    }
} 