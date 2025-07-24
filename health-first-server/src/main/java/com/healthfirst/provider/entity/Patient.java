package com.healthfirst.provider.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "patients", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"email"}),
        @UniqueConstraint(columnNames = {"phone_number"})
})
public class Patient {
    @Id
    @org.springframework.data.annotation.Id
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

    @Column(name = "date_of_birth", nullable = false)
    @Past
    private LocalDate dateOfBirth;

    @Column(name = "gender", nullable = false)
    @NotBlank
    private String gender;

    @Embedded
    private Address address;

    @Embedded
    private EmergencyContact emergencyContact;

    @ElementCollection
    private List<String> medicalHistory;

    @Embedded
    private InsuranceInfo insuranceInfo;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "phone_verified", nullable = false)
    private boolean phoneVerified = false;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "marketing_opt_in", nullable = false)
    private boolean marketingOptIn = false;

    @Column(name = "last_login")
    private OffsetDateTime lastLogin;

    @Column(name = "login_count", nullable = false)
    private int loginCount = 0;

    @Column(name = "failed_login_count", nullable = false)
    private int failedLoginCount = 0;

    @Column(name = "last_failed_login")
    private OffsetDateTime lastFailedLogin;

    @Column(name = "account_locked_until")
    private OffsetDateTime accountLockedUntil;

    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    @Column(name = "last_login_device", length = 200)
    private String lastLoginDevice;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime updatedAt;

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Address {
        @Column(length = 200)
        private String street;
        @Column(length = 100)
        private String city;
        @Column(length = 50)
        private String state;
        @Column(length = 20)
        private String zip;
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmergencyContact {
        @Column(length = 100)
        private String name;
        @Column(length = 20)
        private String phone;
        @Column(length = 50)
        private String relationship;
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InsuranceInfo {
        @Column(length = 100)
        private String provider;
        @Column(length = 50)
        private String policyNumber;
    }
} 