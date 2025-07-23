package com.example.backend.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO representing a decision event in the timeline for an MSISDN
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DecisionTimelineDTO {
    
    // MSISDN information
    private String msisdn;
    
    // Decision details
    private String decision; // 'W' for Whitelist, 'D' for Block/Fraudulent
    private LocalDateTime decisionTime;
    private String decisionUser;
    private Integer decisionRuleId;
    private String decisionRuleName;
    
    // Alert context at the time of decision
    private Integer totalAlertsAtDecision; // Total alerts across all rules at the time of decision
    private Integer totalRulesTriggeredAtDecision; // Number of rules that had triggered alerts
    
    // Previous decision info (if this is a change)
    private String previousDecision;
    private LocalDateTime previousDecisionTime;
    private String previousDecisionRuleName;
    
    // Decision metadata
    private String changeReason; // Reason for decision change if applicable
    private boolean isInitialDecision; // True if this is the first decision for this MSISDN
    private boolean isDecisionChange; // True if this changed from a previous decision
    
    /**
     * Helper method to get a formatted decision change description
     */
    public String getDecisionChangeDescription() {
        if (isInitialDecision) {
            return String.format("Initial decision: %s by %s", 
                getDecisionLabel(decision), decisionRuleName);
        } else if (isDecisionChange) {
            return String.format("Changed from %s to %s by %s", 
                getDecisionLabel(previousDecision), 
                getDecisionLabel(decision), 
                decisionRuleName);
        } else {
            return String.format("Confirmed as %s by %s", 
                getDecisionLabel(decision), decisionRuleName);
        }
    }
    
    private String getDecisionLabel(String decisionCode) {
        if ("D".equals(decisionCode)) return "Blocked";
        if ("W".equals(decisionCode)) return "Whitelisted";
        return "Unknown";
    }
}