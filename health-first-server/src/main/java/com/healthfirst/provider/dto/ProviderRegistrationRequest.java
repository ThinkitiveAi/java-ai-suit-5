package com.healthfirst.provider.dto;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import lombok.Data;

@Data
public class ProviderRegistrationRequest {
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

    @NotBlank
    private String specialization;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "License number must be alphanumeric")
    private String licenseNumber;

    @Min(0)
    @Max(50)
    private int yearsOfExperience;

    @Valid
    private ClinicAddress clinicAddress;

    @Data
    public static class ClinicAddress {
        @Size(max = 200)
        private String street;
        @Size(max = 100)
        private String city;
        @Size(max = 50)
        private String state;
        @Size(max = 20)
        private String zip;
    }
} 