package com.healthfirst.provider.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointment_slots", indexes = {
        @Index(name = "idx_slots_provider_date", columnList = "provider_id,slot_start"),
        @Index(name = "idx_slots_status", columnList = "status"),
        @Index(name = "idx_provider_specialization", columnList = "provider_id,specialization"),
        @Index(name = "idx_slots_location", columnList = "location_type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentSlot {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "availability_id", nullable = false)
    private UUID availabilityId;

    @Column(name = "provider_id", nullable = false)
    private UUID providerId;

    @Column(name = "slot_start", nullable = false)
    private OffsetDateTime slotStart;

    @Column(name = "slot_end", nullable = false)
    private OffsetDateTime slotEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SlotStatus status = SlotStatus.AVAILABLE;

    @Enumerated(EnumType.STRING)
    @Column(name = "appointment_type", nullable = false)
    private AppointmentType appointmentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "location_type", nullable = false)
    private LocationType locationType;

    @Column(name = "specialization", length = 100)
    private String specialization;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "requirements", columnDefinition = "TEXT")
    private String requirements;

    @Column(name = "soft_deleted", nullable = false)
    private boolean softDeleted = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public enum SlotStatus {
        AVAILABLE, BOOKED, CANCELLED, BLOCKED
    }
    public enum AppointmentType {
        CONSULTATION, EMERGENCY, TELEMEDICINE
    }
    public enum LocationType {
        CLINIC, HOME, VIRTUAL
    }
} 