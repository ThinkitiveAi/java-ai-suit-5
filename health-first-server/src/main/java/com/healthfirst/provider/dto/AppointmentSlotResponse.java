package com.healthfirst.provider.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class AppointmentSlotResponse {
    private UUID id;
    private UUID providerId;
    private OffsetDateTime slotStart;
    private OffsetDateTime slotEnd;
    private String status;
    private String appointmentType;
    private String locationType;
    private String specialization;
    private BigDecimal price;
    private String requirements;
} 