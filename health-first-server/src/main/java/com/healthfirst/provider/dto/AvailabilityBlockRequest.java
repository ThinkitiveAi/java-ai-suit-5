package com.healthfirst.provider.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AvailabilityBlockRequest {
    @NotNull
    private String providerId;
    @NotNull
    private LocalDate startDate;
    @NotNull
    private LocalDate endDate;
    @NotNull
    private LocalTime startTime;
    @NotNull
    private LocalTime endTime;
    @NotBlank
    private String timezone;
    private String recurrenceRule;
    @Min(15)
    @Max(240)
    private int slotDuration;
    @Min(0)
    @Max(120)
    private int breakDuration;
} 