package com.example.backend.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RuleCredibilityDTO {
    private Integer ruleId;
    private String ruleName;
    private String ruleDescription;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    
    // Alert metrics
    private Long totalAlerts;
    private Long uniqueMsisdns;
    private Double alertsPerDay;
    
    // Decision metrics
    private Long totalDecisions;
    private Long blockedDecisions; // Status = 'D' (Block/Fraudulent)
    private Long whitelistedDecisions; // Status = 'W'
    private Long pendingAlerts; // Alerts without decisions
    
    // Credibility metrics
    private BigDecimal decisionRate; // Percentage of alerts that got decisions
    private BigDecimal blockingRate; // Percentage of decisions that were blocks
    private BigDecimal falsePositiveRate; // Percentage of decisions that were whitelisted
    private String credibilityScore; // EXCELLENT, GOOD, AVERAGE, POOR
    
    // Time metrics
    private Double averageDecisionTimeHours; // Average time from alert to decision
    private LocalDateTime firstAlert;
    private LocalDateTime lastAlert;
    private LocalDateTime lastDecision;
}
