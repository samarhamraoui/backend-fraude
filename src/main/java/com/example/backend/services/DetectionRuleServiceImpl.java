package com.example.backend.services;

import com.example.backend.dao.DetectionRuleRepository;
import com.example.backend.entities.DetectionRule;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class DetectionRuleServiceImpl implements DetectionRuleService{

    private final DetectionRuleRepository detectionRuleRepository;

    public DetectionRuleServiceImpl(DetectionRuleRepository detectionRuleRepository) {
        this.detectionRuleRepository = detectionRuleRepository;
    }

    @Override
    public DetectionRule addDetectionRule(DetectionRule detectionRule) {
        return detectionRuleRepository.save(detectionRule);
    }

    @Override
    public DetectionRule editDetectionRule(DetectionRule detectionRule) {
        return detectionRuleRepository.save(detectionRule);
    }

    @Override
    public void deleteDetectionRule(Long idRule) {
        detectionRuleRepository.deleteById(idRule);
    }

    @Override
    public DetectionRule getDetectionRuleById(Long idRule) {
        Optional<DetectionRule> detectionRule = detectionRuleRepository.findById(idRule);
        if (!detectionRule.isPresent()) {
            throw new RuntimeException("Detection rule not found for id: " + idRule);
        }
        return detectionRule.get();
    }

    @Override
    public List<DetectionRule> getAllDetectionRules() {
        return detectionRuleRepository.findAll();
    }
}
