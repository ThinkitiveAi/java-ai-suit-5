package com.healthfirst.provider.repository;

import com.healthfirst.provider.entity.AppointmentSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlot, UUID> {
    List<AppointmentSlot> findByProviderIdAndSlotStartBetweenAndStatus(UUID providerId, OffsetDateTime start, OffsetDateTime end, AppointmentSlot.SlotStatus status);
    List<AppointmentSlot> findByAvailabilityId(UUID availabilityId);
    List<AppointmentSlot> findByStatusAndSlotStartBetween(AppointmentSlot.SlotStatus status, OffsetDateTime start, OffsetDateTime end);
    List<AppointmentSlot> findByProviderIdAndSoftDeletedFalse(UUID providerId);
} 