package com.healthfirst.provider.controller;

import com.healthfirst.provider.dto.AvailabilityBlockRequest;
import com.healthfirst.provider.dto.AppointmentSlotResponse;
import com.healthfirst.provider.service.ProviderAvailabilityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/provider")
public class ProviderAvailabilityController {
    @Autowired
    private ProviderAvailabilityService availabilityService;

    @PostMapping("/availability")
    public ResponseEntity<List<AppointmentSlotResponse>> createAvailability(@Valid @RequestBody AvailabilityBlockRequest request) {
        List<AppointmentSlotResponse> slots = availabilityService.createAvailabilityBlock(request);
        return ResponseEntity.ok(slots);
    }
    // TODO: Implement other endpoints (view, update, delete, search, bulk, templates)
} 