package com.example.backend.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for rule credibility analysis based on simple fraudulent/total ratio
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RuleCredibilityAnalysisDTO {
    
    // Rule information
    private Integer ruleId;
    private String ruleName;
    private String ruleDescription;
    private String ruleCategory;
    
    // Analysis period
    private LocalDate periodStart;
    private LocalDate periodEnd;
    
    // Detection metrics
    private Long totalMsisdnsDetected;
    private Long uniqueMsisdnsDetected;
    private Long totalAlerts;
    
    // Decision metrics
    private Long totalDecisions;
    private Long fraudulentMsisdns; // Blocked MSISDNs (decision = 'D')
    private Long whitelistedMsisdns; // Whitelisted MSISDNs (decision = 'W')
    private Long pendingMsisdns; // No decision yet
    
    // Credibility calculation (simple ratio)
    private BigDecimal credibilityRatio; // fraudulent / total
    private BigDecimal credibilityPercentage; // credibilityRatio * 100
    
    // Time-based insights
    private LocalDateTime firstAlert;
    private LocalDateTime lastAlert;
    private BigDecimal averageAlertsPerDay;
    
    // Decision efficiency
    private BigDecimal decisionRate; // decisions / total detections
    private BigDecimal averageDecisionTimeHours;
    
    /**
     * Calculate credibility ratio (fraudulent/total)
     */
    public void calculateCredibility() {
        if (totalMsisdnsDetected == null || totalMsisdnsDetected == 0) {
            this.credibilityRatio = BigDecimal.ZERO;
            this.credibilityPercentage = BigDecimal.ZERO;
        } else {
            this.credibilityRatio = BigDecimal.valueOf(fraudulentMsisdns)
                .divide(BigDecimal.valueOf(totalMsisdnsDetected), 4, RoundingMode.HALF_UP);
            this.credibilityPercentage = this.credibilityRatio
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
        }
    }
    
    /**
     * Get credibility rating based on percentage
     */
    public String getCredibilityRating() {
        if (credibilityPercentage == null) {
            return "UNKNOWN";
        }
        
        double percentage = credibilityPercentage.doubleValue();
        if (percentage >= 80) {
            return "EXCELLENT";
        } else if (percentage >= 60) {
            return "GOOD";
        } else if (percentage >= 40) {
            return "AVERAGE";
        } else if (percentage >= 20) {
            return "FAIR";
        } else {
            return "POOR";
        }
    }
    
    /**
     * Helper to check if rule is effective
     */
    public boolean isEffective() {
        return credibilityPercentage != null && credibilityPercentage.compareTo(BigDecimal.valueOf(50)) >= 0;
    }
}