package com.example.backend.dao;

import com.example.backend.entities.DecisionFraude;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
@Repository
public interface DecisionFraudeRepository extends JpaRepository<DecisionFraude,Integer> {
    @Query("SELECT d FROM DecisionFraude d WHERE d.dateDecision BETWEEN :startDate AND :endDate")
    List<DecisionFraude> findAllByDateDecisionBetween(@Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);
}
