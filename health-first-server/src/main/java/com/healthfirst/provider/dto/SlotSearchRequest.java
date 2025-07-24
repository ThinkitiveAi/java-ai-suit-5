package com.healthfirst.provider.dto;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class SlotSearchRequest {
    private OffsetDateTime start;
    private OffsetDateTime end;
    private String specialization;
    private String locationType;
    private String appointmentType;
    private String insurance;
    private Double minPrice;
    private Double maxPrice;
} 