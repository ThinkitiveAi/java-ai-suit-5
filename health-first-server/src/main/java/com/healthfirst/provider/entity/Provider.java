package com.healthfirst.provider.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "providers", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"email"}),
        @UniqueConstraint(columnNames = {"phone_number"}),
        @UniqueConstraint(columnNames = {"license_number"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Provider {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "first_name", nullable = false, length = 50)
    @NotBlank
    @Size(min = 2, max = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    @NotBlank
    @Size(min = 2, max = 50)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)
    @NotBlank
    @Email
    private String email;

    @Column(name = "phone_number", nullable = false, unique = true)
    @NotBlank
    @Pattern(regexp = "^\\+\\d{10,15}$", message = "Phone number must be in E.164 format")
    private String phoneNumber;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "specialization", nullable = false)
    @NotBlank
    private String specialization;

    @Column(name = "license_number", nullable = false, unique = true)
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "License number must be alphanumeric")
    private String licenseNumber;

    @Column(name = "years_of_experience", nullable = false)
    @Min(0)
    @Max(50)
    private int yearsOfExperience;

    @Embedded
    private ClinicAddress clinicAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "license_document_url")
    private String licenseDocumentUrl;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "last_login")
    private OffsetDateTime lastLogin;

    @Column(name = "login_count", nullable = false)
    private int loginCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime updatedAt;

    public enum VerificationStatus {
        PENDING, VERIFIED, REJECTED
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClinicAddress {
        @Column(length = 200)
        private String street;
        @Column(length = 100)
        private String city;
        @Column(length = 50)
        private String state;
        @Column(length = 20)
        private String zip;
    }
} 