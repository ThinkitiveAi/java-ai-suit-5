package com.healthfirst.provider.service.impl;

import com.healthfirst.provider.dto.AvailabilityBlockRequest;
import com.healthfirst.provider.dto.AppointmentSlotResponse;
import com.healthfirst.provider.dto.AvailabilityTemplateRequest;
import com.healthfirst.provider.dto.SlotSearchRequest;
import com.healthfirst.provider.entity.AppointmentSlot;
import com.healthfirst.provider.entity.ProviderAvailability;
import com.healthfirst.provider.entity.AvailabilityTemplate;
import com.healthfirst.provider.repository.AppointmentSlotRepository;
import com.healthfirst.provider.repository.ProviderAvailabilityRepository;
import com.healthfirst.provider.repository.AvailabilityTemplateRepository;
import com.healthfirst.provider.service.ProviderAvailabilityService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProviderAvailabilityServiceImpl implements ProviderAvailabilityService {
    
    @Autowired
    private ProviderAvailabilityRepository availabilityRepository;
    
    @Autowired
    private AppointmentSlotRepository slotRepository;
    
    @Autowired
    private AvailabilityTemplateRepository templateRepository;

    @Override
    @Transactional
    public List<AppointmentSlotResponse> createAvailabilityBlock(AvailabilityBlockRequest req) {
        // Validate and save ProviderAvailability
        ProviderAvailability availability = ProviderAvailability.builder()
                .providerId(UUID.fromString(req.getProviderId()))
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .timezone(req.getTimezone())
                .recurrenceRule(req.getRecurrenceRule())
                .slotDuration(req.getSlotDuration())
                .breakDuration(req.getBreakDuration())
                .isActive(true)
                .build();
        availability = availabilityRepository.save(availability);
        
        // Generate slots
        List<AppointmentSlot> slots = generateSlots(availability);
        
        // Prevent overlaps
        for (AppointmentSlot slot : slots) {
            boolean overlap = slotRepository.findByProviderIdAndSlotStartBetweenAndStatus(
                    slot.getProviderId(), slot.getSlotStart(), slot.getSlotEnd(), AppointmentSlot.SlotStatus.AVAILABLE
            ).stream().anyMatch(existing -> !existing.isSoftDeleted());
            if (overlap) throw new RuntimeException("Overlapping slot detected");
        }
        
        slotRepository.saveAll(slots);
        return slots.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getAvailabilityBlock(UUID availabilityId) {
        Optional<ProviderAvailability> availability = availabilityRepository.findById(availabilityId);
        if (availability.isEmpty()) {
            throw new RuntimeException("Availability not found");
        }

        List<AppointmentSlot> slots = slotRepository.findByAvailabilityId(availabilityId);

        return Map.of(
            "availability", availability.get(),
            "slots", slots.stream().map(this::toResponse).collect(Collectors.toList())
        );
    }

    @Override
    public List<Map<String, Object>> getProviderAvailability(UUID providerId, String startDate, String endDate) {
        List<ProviderAvailability> availabilities = availabilityRepository.findByProviderIdAndIsActiveTrue(providerId);

        return availabilities.stream().map(availability -> {
            List<AppointmentSlot> slots = slotRepository.findByAvailabilityId(availability.getId());
            return Map.of(
                "availability", availability,
                "slots", slots.stream().map(this::toResponse).collect(Collectors.toList())
            );
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<AppointmentSlotResponse> updateAvailabilityBlock(UUID availabilityId, AvailabilityBlockRequest request) {
        Optional<ProviderAvailability> existing = availabilityRepository.findById(availabilityId);
        if (existing.isEmpty()) {
            throw new RuntimeException("Availability not found");
        }

        // Delete existing slots
        slotRepository.deleteByAvailabilityId(availabilityId);

        // Update availability
        ProviderAvailability availability = existing.get();
        availability.setStartDate(request.getStartDate());
        availability.setEndDate(request.getEndDate());
        availability.setStartTime(request.getStartTime());
        availability.setEndTime(request.getEndTime());
        availability.setTimezone(request.getTimezone());
        availability.setRecurrenceRule(request.getRecurrenceRule());
        availability.setSlotDuration(request.getSlotDuration());
        availability.setBreakDuration(request.getBreakDuration());

        availability = availabilityRepository.save(availability);

        // Generate new slots
        List<AppointmentSlot> slots = generateSlots(availability);
        slotRepository.saveAll(slots);

        return slots.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean deleteAvailabilityBlock(UUID availabilityId) {
        Optional<ProviderAvailability> availability = availabilityRepository.findById(availabilityId);
        if (availability.isEmpty()) {
            return false;
        }

        // Check if any slots are booked
        List<AppointmentSlot> bookedSlots = slotRepository.findByAvailabilityIdAndStatus(availabilityId, AppointmentSlot.SlotStatus.BOOKED);
        if (!bookedSlots.isEmpty()) {
            return false; // Cannot delete if slots are booked
        }

        // Soft delete slots
        List<AppointmentSlot> allSlots = slotRepository.findByAvailabilityId(availabilityId);
        allSlots.forEach(slot -> slot.setSoftDeleted(true));
        slotRepository.saveAll(allSlots);

        // Deactivate availability
        ProviderAvailability av = availability.get();
        av.setActive(false);
        availabilityRepository.save(av);

        return true;
    }

    @Override
    public List<List<AppointmentSlotResponse>> createBulkAvailability(List<AvailabilityBlockRequest> requests) {
        return requests.stream()
                .map(this::createAvailabilityBlock)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> createAvailabilityTemplate(AvailabilityTemplateRequest request) {
        AvailabilityTemplate template = AvailabilityTemplate.builder()
                .providerId(UUID.fromString(request.getProviderId()))
                .name(request.getName())
                .daysOfWeek(request.getDaysOfWeek())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .slotDuration(request.getSlotDuration())
                .breakDuration(request.getBreakDuration())
                .timezone(request.getTimezone())
                .recurrenceRule(request.getRecurrenceRule())
                .build();

        template = templateRepository.save(template);

        return Map.of(
            "id", template.getId(),
            "name", template.getName(),
            "message", "Template created successfully"
        );
    }

    @Override
    public List<AvailabilityTemplate> getAvailabilityTemplates(UUID providerId) {
        return templateRepository.findByProviderId(providerId);
    }

    @Override
    public List<AppointmentSlotResponse> searchAvailableSlots(SlotSearchRequest request) {
        // Search for available slots based on criteria
        List<AppointmentSlot> slots = slotRepository.findAvailableSlotsByCriteria(
                request.getSpecialization(),
                request.getLocationType(),
                request.getStart().toString(),
                request.getEnd().toString(),
                request.getAppointmentType()
        );

        return slots.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private List<AppointmentSlot> generateSlots(ProviderAvailability availability) {
        List<AppointmentSlot> slots = new ArrayList<>();
        ZoneId zoneId = ZoneId.of(availability.getTimezone());
        LocalDate date = availability.getStartDate();
        
        while (!date.isAfter(availability.getEndDate())) {
            LocalTime time = availability.getStartTime();
            while (time.plusMinutes(availability.getSlotDuration()).isBefore(availability.getEndTime())
                    || time.plusMinutes(availability.getSlotDuration()).equals(availability.getEndTime())) {
                
                OffsetDateTime slotStart = OffsetDateTime.of(date, time, zoneId.getRules().getOffset(Instant.now()));
                OffsetDateTime slotEnd = slotStart.plusMinutes(availability.getSlotDuration());
                
                slots.add(AppointmentSlot.builder()
                        .availabilityId(availability.getId())
                        .providerId(availability.getProviderId())
                        .slotStart(slotStart.withOffsetSameInstant(ZoneOffset.UTC))
                        .slotEnd(slotEnd.withOffsetSameInstant(ZoneOffset.UTC))
                        .status(AppointmentSlot.SlotStatus.AVAILABLE)
                        .appointmentType(AppointmentSlot.AppointmentType.CONSULTATION)
                        .locationType(AppointmentSlot.LocationType.CLINIC)
                        .softDeleted(false)
                        .build());
                
                time = time.plusMinutes(availability.getSlotDuration() + availability.getBreakDuration());
            }
            date = date.plusDays(1); // TODO: handle recurrence_rule for more complex patterns
        }
        return slots;
    }

    private AppointmentSlotResponse toResponse(AppointmentSlot slot) {
        return AppointmentSlotResponse.builder()
                .id(slot.getId())
                .providerId(slot.getProviderId())
                .slotStart(slot.getSlotStart())
                .slotEnd(slot.getSlotEnd())
                .status(slot.getStatus().name())
                .appointmentType(slot.getAppointmentType().name())
                .locationType(slot.getLocationType().name())
                .specialization(slot.getSpecialization())
                .price(slot.getPrice())
                .requirements(slot.getRequirements())
                .build();
    }
} 