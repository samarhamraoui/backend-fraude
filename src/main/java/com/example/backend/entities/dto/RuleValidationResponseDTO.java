package com.example.backend.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Comprehensive response DTO for rule validation containing all detected MSISDNs,
 * their metadata, decisions, and credibility analysis
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RuleValidationResponseDTO {
    
    // Request parameters
    private List<Integer> ruleIds;
    private LocalDate startDate;
    private LocalDate endDate;
    
    // Response metadata
    private Integer totalRulesAnalyzed;
    private Long totalMsisdnsDetected;
    private Long totalAlertsGenerated;
    
    // Detected MSISDNs with full details
    private List<MsisdnDetectionDTO> detectedMsisdns;
    
    // Credibility analysis per rule
    private List<RuleCredibilityAnalysisDTO> ruleCredibilityAnalysis;
    
    // Summary statistics
    private SummaryStatistics summary;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SummaryStatistics {
        // Overall metrics
        private Long totalUniquesMsisdns;
        private Long totalFraudulentMsisdns;
        private Long totalWhitelistedMsisdns;
        private Long totalPendingMsisdns;
        
        // Decision metrics
        private Double overallDecisionRate;
        private Double overallFraudRate; // fraudulent/total ratio
        private Double averageRuleCredibility;
        
        // Time metrics
        private Double averageDetectionToDecisionHours;
        private LocalDate mostActiveDetectionDate;
        
        // Rule effectiveness
        private Integer highlyEffectiveRules; // credibility >= 80%
        private Integer moderatelyEffectiveRules; // credibility 50-79%
        private Integer lowEffectiveRules; // credibility < 50%
        
        // Top insights
        private List<String> topFraudulentMsisdns; // Most frequently detected as fraud
        private Map<Integer, Long> alertDistributionByRule; // Rule ID -> Alert count
    }
}