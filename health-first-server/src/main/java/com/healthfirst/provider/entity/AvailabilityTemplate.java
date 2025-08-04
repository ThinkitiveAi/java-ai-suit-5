package com.healthfirst.provider.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "availability_templates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityTemplate {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "provider_id", nullable = false)
    private UUID providerId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "days_of_week", nullable = false, length = 50)
    private String daysOfWeek; // e.g., "MON,TUE,WED"

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "slot_duration", nullable = false)
    private int slotDuration;

    @Column(name = "break_duration", nullable = false)
    private int breakDuration;

    @Column(name = "timezone", nullable = false, length = 50)
    private String timezone;

    @Column(name = "recurrence_rule", length = 255)
    private String recurrenceRule;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
} 