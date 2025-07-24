package com.healthfirst.provider.service;

import com.healthfirst.provider.dto.AvailabilityBlockRequest;
import com.healthfirst.provider.dto.AppointmentSlotResponse;
import com.healthfirst.provider.entity.AppointmentSlot;
import com.healthfirst.provider.entity.ProviderAvailability;
import com.healthfirst.provider.repository.AppointmentSlotRepository;
import com.healthfirst.provider.repository.ProviderAvailabilityRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProviderAvailabilityService {
    @Autowired
    private ProviderAvailabilityRepository availabilityRepository;
    @Autowired
    private AppointmentSlotRepository slotRepository;

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