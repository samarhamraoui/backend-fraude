package com.example.backend.dao;

import com.example.backend.entities.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for rule validation operations
 */
public interface IRuleValidationRepository {
    
    /**
     * Get rule validation data with pagination
     */
    Page<RuleValidationDTO> getRuleValidationData(
            List<Integer> ruleIds, 
            LocalDate startDate,
            LocalDate endDate, 
            String msisdn,
            String decisionStatus, 
            Pageable pageable);
    
    /**
     * Get all detected MSISDNs with their metadata
     */
    Page<MsisdnDetectionDTO> getDetectedMsisdns(
            List<Integer> ruleIds,
            LocalDate startDate,
            LocalDate endDate,
            String decisionStatus,
            Pageable pageable);
    
    /**
     * Calculate rule credibility based on fraudulent/total ratio
     */
    List<RuleCredibilityAnalysisDTO> calculateRuleCredibility(
            List<Integer> ruleIds,
            LocalDate startDate,
            LocalDate endDate);
    
    /**
     * Get MSISDN detection history
     */
    List<MsisdnDetectionDTO> getMsisdnHistory(
            String msisdn,
            LocalDate startDate,
            LocalDate endDate);
    
    /**
     * Get rule credibility analysis (legacy method)
     */
    List<RuleCredibilityDTO> getRuleCredibilityAnalysis(
            List<Integer> ruleIds,
            LocalDate startDate,
            LocalDate endDate);
    
    /**
     * Calculate credibility score (legacy method)
     */
    String calculateCredibilityScore(
            Long totalAlerts,
            Long totalDecisions,
            Long whitelistedDecisions,
            Long pendingAlerts);
}