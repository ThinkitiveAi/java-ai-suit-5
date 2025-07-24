package com.healthfirst.provider.repository;

import com.healthfirst.provider.entity.ProviderAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProviderAvailabilityRepository extends JpaRepository<ProviderAvailability, UUID> {
    List<ProviderAvailability> findByProviderIdAndIsActive(UUID providerId, boolean isActive);
    List<ProviderAvailability> findByProviderIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(UUID providerId, LocalDate end, LocalDate start);
} 