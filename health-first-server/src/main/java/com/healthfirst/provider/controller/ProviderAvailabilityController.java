package com.healthfirst.provider.controller;

import com.healthfirst.provider.dto.AvailabilityBlockRequest;
import com.healthfirst.provider.dto.AppointmentSlotResponse;
import com.healthfirst.provider.dto.AvailabilityTemplateRequest;
import com.healthfirst.provider.dto.SlotSearchRequest;
import com.healthfirst.provider.service.ProviderAvailabilityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/provider")
public class ProviderAvailabilityController {
    @Autowired
    private ProviderAvailabilityService availabilityService;

    @PostMapping("/availability")
    public ResponseEntity<List<AppointmentSlotResponse>> createAvailability(@RequestBody AvailabilityBlockRequest request) {
        List<AppointmentSlotResponse> slots = availabilityService.createAvailabilityBlock(request);
        return ResponseEntity.ok(slots);
    }

    @GetMapping("/availability/{availabilityId}")
    public ResponseEntity<?> getAvailability(@PathVariable String availabilityId) {
        try {
            UUID id = UUID.fromString(availabilityId);
            return ResponseEntity.ok(availabilityService.getAvailabilityBlock(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid availability ID"));
        }
    }

    @GetMapping("/availability")
    public ResponseEntity<?> getProviderAvailability(@RequestParam String providerId, 
                                                   @RequestParam(required = false) String startDate,
                                                   @RequestParam(required = false) String endDate) {
        try {
            UUID providerUUID = UUID.fromString(providerId);
            return ResponseEntity.ok(availabilityService.getProviderAvailability(providerUUID, startDate, endDate));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid provider ID"));
        }
    }

    @PutMapping("/availability/{availabilityId}")
    public ResponseEntity<?> updateAvailability(@PathVariable String availabilityId, 
                                              @RequestBody AvailabilityBlockRequest request) {
        try {
            UUID id = UUID.fromString(availabilityId);
            return ResponseEntity.ok(availabilityService.updateAvailabilityBlock(id, request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid availability ID"));
        }
    }

    @DeleteMapping("/availability/{availabilityId}")
    public ResponseEntity<?> deleteAvailability(@PathVariable String availabilityId) {
        try {
            UUID id = UUID.fromString(availabilityId);
            boolean success = availabilityService.deleteAvailabilityBlock(id);
            if (success) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Availability deleted successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("success", false, "message", "Cannot delete availability with booked slots"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid availability ID"));
        }
    }

    @PostMapping("/availability/bulk")
    public ResponseEntity<?> createBulkAvailability(@RequestBody List<AvailabilityBlockRequest> requests) {
        return ResponseEntity.ok(availabilityService.createBulkAvailability(requests));
    }

    @PostMapping("/availability/templates")
    public ResponseEntity<?> createAvailabilityTemplate(@RequestBody AvailabilityTemplateRequest request) {
        return ResponseEntity.ok(availabilityService.createAvailabilityTemplate(request));
    }

    @GetMapping("/availability/templates")
    public ResponseEntity<?> getAvailabilityTemplates(@RequestParam String providerId) {
        try {
            UUID providerUUID = UUID.fromString(providerId);
            return ResponseEntity.ok(availabilityService.getAvailabilityTemplates(providerUUID));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid provider ID"));
        }
    }

    @PostMapping("/availability/search")
    public ResponseEntity<?> searchAvailableSlots(@RequestBody SlotSearchRequest request) {
        return ResponseEntity.ok(availabilityService.searchAvailableSlots(request));
    }
} 