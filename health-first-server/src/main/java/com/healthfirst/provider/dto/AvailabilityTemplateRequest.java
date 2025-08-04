package com.healthfirst.provider.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.time.LocalTime;

@Data
public class AvailabilityTemplateRequest {
    @NotNull
    private String providerId;
    @NotBlank
    private String name;
    @NotBlank
    private String daysOfWeek; // e.g., "MON,TUE,WED"
    @NotNull
    private LocalTime startTime;
    @NotNull
    private LocalTime endTime;
    @Min(15)
    @Max(240)
    private int slotDuration;
    @Min(0)
    @Max(120)
    private int breakDuration;
    @NotBlank
    private String timezone;
    private String recurrenceRule;
} 