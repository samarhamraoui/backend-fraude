package com.example.backend.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RuleValidationSummaryDTO {
    private Integer ruleCount;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    
    // Alert summary
    private Long totalAlerts;
    private Long totalDecisions;
    private Long totalBlocked;
    private Long totalWhitelisted;
    private Long pendingAlerts;
    
    // Credibility distribution
    private Long excellentRules;
    private Long goodRules;
    private Long averageRules;
    private Long poorRules;
}
