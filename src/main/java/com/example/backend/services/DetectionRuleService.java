package com.example.backend.services;

import com.example.backend.entities.DetectionRule;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface DetectionRuleService {
    DetectionRule addDetectionRule(DetectionRule detectionRule);
    DetectionRule editDetectionRule(DetectionRule detectionRule);
    void deleteDetectionRule(Long idRule);
    DetectionRule getDetectionRuleById(Long idRule);
    List<DetectionRule> getAllDetectionRules();
}
