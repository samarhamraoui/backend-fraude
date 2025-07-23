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
public class RuleValidationDTO {
    private String msisdn;
    private Integer ruleId;
    private String ruleName;
    private String ruleDescription;
    private LocalDateTime firstDateDetection;
    private LocalDateTime lastDateDetection;
    private String decisionStatus; // Decision from DecisionFraude (D=Block/Fraudulent, W=Whitelist, null=pending)
    private LocalDateTime dateDecision;
    private String decisionUser;
    private Long alertCount; // Number of alerts for this msisdn-rule combination
}
