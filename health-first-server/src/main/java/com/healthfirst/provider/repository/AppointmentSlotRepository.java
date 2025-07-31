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
    void deleteByAvailabilityId(UUID availabilityId);
    List<AppointmentSlot> findByAvailabilityIdAndStatus(UUID availabilityId, AppointmentSlot.SlotStatus status);
    @org.springframework.data.jpa.repository.Query("SELECT a FROM AppointmentSlot a WHERE a.status = 'AVAILABLE' AND a.softDeleted = false AND (:specialization IS NULL OR a.specialization = :specialization) AND (:locationType IS NULL OR a.locationType = :locationType) AND (:appointmentType IS NULL OR a.appointmentType = :appointmentType)")
    List<AppointmentSlot> findAvailableSlotsByCriteria(@org.springframework.data.repository.query.Param("specialization") String specialization, @org.springframework.data.repository.query.Param("locationType") String locationType, @org.springframework.data.repository.query.Param("startDate") String startDate, @org.springframework.data.repository.query.Param("endDate") String endDate, @org.springframework.data.repository.query.Param("appointmentType") String appointmentType);
} 