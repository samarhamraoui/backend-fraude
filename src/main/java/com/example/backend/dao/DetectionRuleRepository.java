package com.example.backend.dao;

import com.example.backend.entities.DetectionRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetectionRuleRepository extends JpaRepository<DetectionRule,Long> {
    DetectionRule getDetectionRuleByRuleId(Long ruleId);
}
