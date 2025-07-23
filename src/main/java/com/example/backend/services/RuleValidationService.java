package com.example.backend.services;

import com.example.backend.entities.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for rule validation and credibility analysis
 */
public interface RuleValidationService {

    /**
     * Validate rules and get comprehensive response with all detected MSISDNs,
     * their metadata, decisions, and credibility analysis
     * 
     * @param request Rule validation request containing rule IDs and date range
     * @return Comprehensive validation response
     */
    RuleValidationResponseDTO validateRules(RuleValidationRequestDTO request);

    /**
     * Get paginated list of detected MSISDNs for specified rules
     * 
     * @param ruleIds List of rule IDs to query
     * @param startDate Start date for the analysis period
     * @param endDate End date for the analysis period
     * @param decisionStatus Optional filter by decision status (W/B/PENDING)
     * @param pageable Pagination parameters
     * @return Page of detected MSISDNs with metadata
     */
    Page<MsisdnDetectionDTO> getDetectedMsisdns(List<Integer> ruleIds, 
                                                LocalDate startDate, 
                                                LocalDate endDate, 
                                                String decisionStatus,
                                                Pageable pageable);

    /**
     * Analyze rule credibility based on simple fraudulent/total ratio
     * Credibility = (MSISDNs blocked) / (Total MSISDNs detected)
     * 
     * @param ruleIds List of rule IDs to analyze
     * @param startDate Start date for the analysis period
     * @param endDate End date for the analysis period
     * @return List of credibility analysis results
     */
    List<RuleCredibilityAnalysisDTO> analyzeRuleCredibility(List<Integer> ruleIds,
                                                           LocalDate startDate,
                                                           LocalDate endDate);

    /**
     * Get detection and decision history for a specific MSISDN
     * 
     * @param msisdn MSISDN to query
     * @param startDate Optional start date filter
     * @param endDate Optional end date filter
     * @return List of detection records for the MSISDN
     */
    List<MsisdnDetectionDTO> getMsisdnHistory(String msisdn,
                                             LocalDate startDate,
                                             LocalDate endDate);

    /**
     * Export validation results in specified format (CSV or Excel)
     * 
     * @param request Rule validation request
     * @param format Export format (CSV or EXCEL)
     * @return Byte array containing the exported data
     */
    byte[] exportValidationResults(RuleValidationRequestDTO request, String format);

    /**
     * Get rule validation data with alert information and decision status
     * (Legacy method for backward compatibility)
     */
    Page<RuleValidationDTO> getRuleValidationData(RuleValidationRequestDTO request);

    /**
     * Get comprehensive rule credibility analysis
     * (Legacy method for backward compatibility)
     */
    List<RuleCredibilityDTO> getRuleCredibilityAnalysis(List<Integer> ruleIds, 
                                                       LocalDate startDate, 
                                                       LocalDate endDate);

    /**
     * Get rule credibility analysis for a single rule
     * (Legacy method for backward compatibility)
     */
    RuleCredibilityDTO getSingleRuleCredibility(Integer ruleId, 
                                               LocalDate startDate, 
                                               LocalDate endDate);

    /**
     * Get rule validation summary statistics
     * (Legacy method for backward compatibility)
     */
    RuleValidationSummaryDTO getRuleValidationSummary(List<Integer> ruleIds,
                                                     LocalDate startDate,
                                                     LocalDate endDate);
    
    /**
     * Get decision timeline for an MSISDN showing all decision changes
     * 
     * @param msisdn The MSISDN to query
     * @param startDate Optional start date filter
     * @param endDate Optional end date filter
     * @return List of all decisions in chronological order
     */
    List<DecisionTimelineDTO> getMsisdnDecisionTimeline(String msisdn,
                                                       LocalDate startDate,
                                                       LocalDate endDate);
    
    /**
     * Get complete history for an MSISDN including all alerts and decisions
     * 
     * @param msisdn The MSISDN to query
     * @param startDate Optional start date filter
     * @param endDate Optional end date filter
     * @return List of all events (alerts and decisions) in chronological order
     */
    List<MsisdnHistoryEventDTO> getMsisdnCompleteHistory(String msisdn,
                                                        LocalDate startDate,
                                                        LocalDate endDate);
}