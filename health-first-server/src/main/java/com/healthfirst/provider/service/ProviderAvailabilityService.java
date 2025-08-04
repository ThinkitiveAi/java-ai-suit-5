package com.healthfirst.provider.service;

import com.healthfirst.provider.dto.AvailabilityBlockRequest;
import com.healthfirst.provider.dto.AppointmentSlotResponse;
import com.healthfirst.provider.dto.AvailabilityTemplateRequest;
import com.healthfirst.provider.dto.SlotSearchRequest;
import com.healthfirst.provider.entity.AvailabilityTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ProviderAvailabilityService {
    List<AppointmentSlotResponse> createAvailabilityBlock(AvailabilityBlockRequest request);
    Map<String, Object> getAvailabilityBlock(UUID availabilityId);
    List<Map<String, Object>> getProviderAvailability(UUID providerId, String startDate, String endDate);
    List<AppointmentSlotResponse> updateAvailabilityBlock(UUID availabilityId, AvailabilityBlockRequest request);
    boolean deleteAvailabilityBlock(UUID availabilityId);
    List<List<AppointmentSlotResponse>> createBulkAvailability(List<AvailabilityBlockRequest> requests);
    Map<String, Object> createAvailabilityTemplate(AvailabilityTemplateRequest request);
    List<AvailabilityTemplate> getAvailabilityTemplates(UUID providerId);
    List<AppointmentSlotResponse> searchAvailableSlots(SlotSearchRequest request);
} 