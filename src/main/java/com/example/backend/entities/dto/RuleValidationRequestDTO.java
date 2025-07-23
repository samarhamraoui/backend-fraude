package com.example.backend.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RuleValidationRequestDTO {
    @NotEmpty(message = "Rule IDs list cannot be empty")
    private List<Integer> ruleIds;
    
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    private LocalDate endDate;
    
    // Optional filters
    private String msisdn; // Filter by specific MSISDN
    private String decisionStatus; // Filter by decision status (D, W, null)
    private Integer page = 0; // For pagination
    private Integer size = 50; // For pagination
}
