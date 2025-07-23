package com.example.backend.dao;

import com.example.backend.entities.AuditLog;
import com.example.backend.entities.DecisionFraude;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    @Query("SELECT d FROM AuditLog d WHERE d.modifiedAt BETWEEN :startDate AND :endDate ORDER BY d.modifiedAt DESC")
    List<AuditLog> findAllByModifiedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}


