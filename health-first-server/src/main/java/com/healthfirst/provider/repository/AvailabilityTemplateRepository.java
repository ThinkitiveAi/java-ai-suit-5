package com.healthfirst.provider.repository;

import com.healthfirst.provider.entity.AvailabilityTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AvailabilityTemplateRepository extends JpaRepository<AvailabilityTemplate, UUID> {
    List<AvailabilityTemplate> findByProviderId(UUID providerId);
} 