package com.healthfirst.provider.repository;

import com.healthfirst.provider.entity.PatientSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientSessionRepository extends JpaRepository<PatientSession, UUID> {
    
    @Query("SELECT ps FROM PatientSession ps WHERE ps.patientId = :patientId AND ps.isActive = true")
    List<PatientSession> findActiveSessionsByPatientId(@Param("patientId") UUID patientId);
    
    @Query("SELECT ps FROM PatientSession ps WHERE ps.tokenHash = :tokenHash AND ps.isActive = true AND ps.expiresAt > :now")
    Optional<PatientSession> findByTokenHashAndActive(@Param("tokenHash") String tokenHash, @Param("now") OffsetDateTime now);
    
    @Query("SELECT COUNT(ps) FROM PatientSession ps WHERE ps.patientId = :patientId AND ps.isActive = true")
    long countActiveSessionsByPatientId(@Param("patientId") UUID patientId);
    
    @Query("UPDATE PatientSession ps SET ps.isActive = false WHERE ps.patientId = :patientId")
    void deactivateAllSessionsByPatientId(@Param("patientId") UUID patientId);
    
    @Query("UPDATE PatientSession ps SET ps.isActive = false WHERE ps.id = :sessionId AND ps.patientId = :patientId")
    void deactivateSessionByIdAndPatientId(@Param("sessionId") UUID sessionId, @Param("patientId") UUID patientId);
} 