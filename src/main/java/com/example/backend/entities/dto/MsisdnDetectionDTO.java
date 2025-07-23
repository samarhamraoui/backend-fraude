package com.example.backend.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO representing a detected MSISDN with all relevant metadata
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MsisdnDetectionDTO {
    
    // Basic detection info
    private String msisdn;
    private Integer ruleId;
    private String ruleName;
    private String ruleCategory;
    private String ruleType; // Manual or Automatic rule
    
    // Detection metadata
    private LocalDateTime firstDetectionTime;
    private LocalDateTime lastDetectionTime;
    private Integer alertCount;
    private String detectionSource; // ALERT or DECISION_ONLY
    
    // Decision information
    private String decisionStatus; // 'W' for Whitelist, 'D' for Block/Fraudulent, null for pending
    private LocalDateTime decisionTime;
    private String decisionUser;
    private String decisionReason;
    private Integer decisionRuleId; // Rule that made the decision (may differ from alerting rule)
    
    // Aggregated information
    private Integer totalRulesTriggered; // Total number of rules that triggered alerts for this MSISDN
    private Integer totalAlertsAllRules; // Total alerts across all rules
    
    // Additional metadata
    private String subscriberType; // Prepaid/Postpaid if available
    private String location; // If location data is available
    private Double riskScore; // Optional risk score if calculated
    
    /**
     * Helper method to check if MSISDN is blocked (fraudulent)
     */
    public boolean isFraudulent() {
        return "D".equals(decisionStatus);
    }
    
    /**
     * Helper method to check if MSISDN is whitelisted
     */
    public boolean isWhitelisted() {
        return "W".equals(decisionStatus);
    }
    
    /**
     * Helper method to check if decision is pending
     */
    public boolean isPending() {
        return decisionStatus == null;
    }
}