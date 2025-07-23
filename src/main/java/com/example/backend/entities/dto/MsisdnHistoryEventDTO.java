package com.example.backend.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO representing a single event in MSISDN history (either alert or decision)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MsisdnHistoryEventDTO {
    
    // Event identification
    private String eventType; // "ALERT" or "DECISION"
    private LocalDateTime eventTime;
    private String msisdn;
    
    // Rule information
    private Integer ruleId;
    private String ruleName;
    private String ruleType; // Manual or Automatic
    private String ruleCategory;
    
    // Alert-specific fields (when eventType = "ALERT")
    private Integer alertCount; // Number of alerts for this rule
    private LocalDateTime firstAlertTime;
    private LocalDateTime lastAlertTime;
    
    // Decision-specific fields (when eventType = "DECISION")
    private String decision; // 'W' or 'D'
    private String previousDecision; // Previous decision if this is a change
    private String decisionUser;
    private String decisionChangeReason;
    
    // Context information
    private Integer totalAlertsAllRulesAtTime; // Total alerts from all rules at this event time
    private Integer totalRulesTriggeredAtTime; // Total rules that had triggered alerts at this time
    
    // Display helpers
    private String eventDescription;
    private String eventIcon;
    private String eventSeverity;
    
    /**
     * Get formatted event description for display
     */
    public String getFormattedDescription() {
        if ("ALERT".equals(eventType)) {
            if (alertCount > 1) {
                return String.format("Rule '%s' triggered %d alerts (from %s to %s)", 
                    ruleName, alertCount, 
                    formatTime(firstAlertTime), 
                    formatTime(lastAlertTime));
            } else {
                return String.format("Rule '%s' triggered an alert", ruleName);
            }
        } else if ("DECISION".equals(eventType)) {
            if (previousDecision != null && !previousDecision.equals(decision)) {
                return String.format("Decision changed from %s to %s by rule '%s' (User: %s)", 
                    getDecisionLabel(previousDecision), 
                    getDecisionLabel(decision), 
                    ruleName, 
                    decisionUser != null ? decisionUser : "System");
            } else {
                return String.format("Initial decision: %s by rule '%s' (User: %s)", 
                    getDecisionLabel(decision), 
                    ruleName, 
                    decisionUser != null ? decisionUser : "System");
            }
        }
        return eventDescription;
    }
    
    private String getDecisionLabel(String decisionCode) {
        if ("D".equals(decisionCode)) return "Blocked";
        if ("W".equals(decisionCode)) return "Whitelisted";
        return "Unknown";
    }
    
    private String formatTime(LocalDateTime time) {
        if (time == null) return "";
        return time.toLocalDate().toString() + " " + 
               time.toLocalTime().toString().substring(0, 5);
    }
}