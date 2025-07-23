package com.example.backend.Controllers;

import com.example.backend.entities.dto.*;
import com.example.backend.services.RuleValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Rule Validation Controller
 * 
 * Consolidated controller providing comprehensive rule validation, credibility analysis,
 * MSISDN tracking, and timeline functionality.
 */
@RestController
@RequestMapping("/rule-validation")
@Tag(name = "Rule Validation", description = "Fraud detection rule validation, credibility analysis, and MSISDN tracking")
@Validated
@Slf4j
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class RuleValidationController {

    private final RuleValidationService ruleValidationService;

    @Operation(
            summary = "Validate fraud detection rules",
            description = "Performs comprehensive rule validation including MSISDN detection, " +
                         "decision analysis, and credibility metrics for the specified period."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Validation completed successfully",
                    content = @Content(schema = @Schema(implementation = RuleValidationResponseDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/validate")
    public ResponseEntity<RuleValidationResponseDTO> validateRules(
            @Valid @RequestBody RuleValidationRequestDTO request) {
        
        log.info("Rule validation request: {} rules, period: {} to {}", 
                request.getRuleIds().size(), request.getStartDate(), request.getEndDate());
        
        try {
            long startTime = System.currentTimeMillis();
            
            RuleValidationResponseDTO response = ruleValidationService.validateRules(request);
            
            long processingTime = System.currentTimeMillis() - startTime;
            log.info("Validation complete: {} MSISDNs detected, {} rules analyzed in {}ms", 
                    response.getTotalMsisdnsDetected(), 
                    response.getTotalRulesAnalyzed(),
                    processingTime);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error during rule validation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Get detected MSISDNs with pagination",
            description = "Retrieve MSISDNs detected by specified rules with optional filtering by decision status."
    )
    @GetMapping("/detected-msisdns")
    public ResponseEntity<Page<MsisdnDetectionDTO>> getDetectedMsisdns(
            @Parameter(description = "List of rule IDs", required = true)
            @RequestParam @NotEmpty List<Integer> ruleIds,
            
            @Parameter(description = "Start date (YYYY-MM-DD)", required = true)
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "End date (YYYY-MM-DD)", required = true)
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            
            @Parameter(description = "Filter by decision status (W/D/PENDING)")
            @RequestParam(required = false) String decisionStatus,
            
            @PageableDefault(size = 50, sort = "lastDetectionTime,desc") Pageable pageable) {
        
        log.debug("Fetching detected MSISDNs: rules={}, period={} to {}, status={}", 
                ruleIds, startDate, endDate, decisionStatus);
        
        try {
            Page<MsisdnDetectionDTO> results = ruleValidationService.getDetectedMsisdns(
                    ruleIds, startDate, endDate, decisionStatus, pageable);
            
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error fetching detected MSISDNs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Get rule credibility analysis",
            description = "Calculate credibility metrics for specified rules. " +
                         "Credibility is based on the ratio of blocked MSISDNs to total detected MSISDNs."
    )
    @GetMapping("/credibility")
    public ResponseEntity<List<RuleCredibilityAnalysisDTO>> getRuleCredibility(
            @Parameter(description = "List of rule IDs", required = true)
            @RequestParam @NotEmpty List<Integer> ruleIds,
            
            @Parameter(description = "Start date (YYYY-MM-DD)", required = true)
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "End date (YYYY-MM-DD)", required = true)
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Calculating credibility for {} rules", ruleIds.size());
        
        try {
            List<RuleCredibilityAnalysisDTO> analysis = ruleValidationService.analyzeRuleCredibility(
                    ruleIds, startDate, endDate);
            
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            log.error("Error calculating rule credibility", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Get MSISDN complete timeline",
            description = "Retrieve the complete chronological timeline for an MSISDN including " +
                         "all alerts from different rules and all decision changes."
    )
    @GetMapping("/msisdn/{msisdn}/timeline")
    public ResponseEntity<List<MsisdnHistoryEventDTO>> getMsisdnTimeline(
            @Parameter(description = "MSISDN to query", required = true)
            @PathVariable String msisdn,
            
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Fetching timeline for MSISDN: {}", msisdn);
        
        try {
            List<MsisdnHistoryEventDTO> timeline = ruleValidationService.getMsisdnCompleteHistory(
                    msisdn, startDate, endDate);
            
            if (timeline.isEmpty()) {
                log.info("No timeline events found for MSISDN: {}", msisdn);
                return ResponseEntity.ok(Collections.emptyList());
            }
            
            log.info("Found {} timeline events for MSISDN: {}", timeline.size(), msisdn);
            return ResponseEntity.ok(timeline);
        } catch (Exception e) {
            log.error("Error fetching MSISDN timeline: {}", msisdn, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Get MSISDN detection history",
            description = "Retrieve all rule detections for a specific MSISDN."
    )
    @GetMapping("/msisdn/{msisdn}/detections")
    public ResponseEntity<List<MsisdnDetectionDTO>> getMsisdnDetections(
            @Parameter(description = "MSISDN to query", required = true)
            @PathVariable String msisdn,
            
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Fetching detections for MSISDN: {}", msisdn);
        
        try {
            List<MsisdnDetectionDTO> detections = ruleValidationService.getMsisdnHistory(
                    msisdn, startDate, endDate);
            
            if (detections.isEmpty()) {
                log.info("No detections found for MSISDN: {}", msisdn);
                return ResponseEntity.ok(Collections.emptyList());
            }
            
            log.info("Found {} detections for MSISDN: {}", detections.size(), msisdn);
            return ResponseEntity.ok(detections);
        } catch (Exception e) {
            log.error("Error fetching MSISDN detections: {}", msisdn, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Get MSISDN decision timeline",
            description = "Get chronological decision timeline showing all decision changes for a specific MSISDN"
    )
    @GetMapping("/msisdn/{msisdn}/decision-timeline")
    public ResponseEntity<List<DecisionTimelineDTO>> getMsisdnDecisionTimeline(
            @Parameter(description = "MSISDN to query", required = true)
            @PathVariable String msisdn,
            
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Fetching decision timeline for MSISDN: {}", msisdn);
        
        try {
            List<DecisionTimelineDTO> timeline = ruleValidationService.getMsisdnDecisionTimeline(
                    msisdn, startDate, endDate);
            
            if (timeline.isEmpty()) {
                log.info("No decisions found for MSISDN: {}", msisdn);
                return ResponseEntity.ok(Collections.emptyList());
            }
            
            log.info("Found {} decision events for MSISDN: {}", timeline.size(), msisdn);
            return ResponseEntity.ok(timeline);
        } catch (Exception e) {
            log.error("Error fetching decision timeline: {}", msisdn, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Export validation results",
            description = "Export rule validation results in CSV or Excel format."
    )
    @PostMapping("/export")
    public ResponseEntity<byte[]> exportValidationResults(
            @Valid @RequestBody RuleValidationRequestDTO request,
            @Parameter(description = "Export format (CSV or EXCEL)")
            @RequestParam(defaultValue = "CSV") String format) {
        
        log.info("Exporting validation results in {} format", format);
        
        try {
            byte[] exportData = ruleValidationService.exportValidationResults(request, format.toUpperCase());
            
            String filename = String.format("rule_validation_%s.%s", 
                    LocalDate.now(), format.toLowerCase());
            
            String contentType = format.equalsIgnoreCase("EXCEL") ? 
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" : 
                    "text/csv";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .body(exportData);
                    
        } catch (Exception e) {
            log.error("Error exporting validation results", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Get validation summary statistics",
            description = "Get summary statistics for rule validation including overall metrics."
    )
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getValidationSummary(
            @Parameter(description = "List of rule IDs", required = true)
            @RequestParam @NotEmpty List<Integer> ruleIds,
            
            @Parameter(description = "Start date (YYYY-MM-DD)", required = true)
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "End date (YYYY-MM-DD)", required = true)
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Getting validation summary for {} rules", ruleIds.size());
        
        try {
            // Create request DTO for validation
            RuleValidationRequestDTO request = new RuleValidationRequestDTO();
            request.setRuleIds(ruleIds);
            request.setStartDate(startDate);
            request.setEndDate(endDate);
            
            RuleValidationResponseDTO response = ruleValidationService.validateRules(request);
            
            // Extract summary data
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalRules", response.getTotalRulesAnalyzed());
            summary.put("totalMsisdns", response.getTotalMsisdnsDetected());
            summary.put("totalAlerts", response.getTotalAlertsGenerated());
            summary.put("summary", response.getSummary());
            summary.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            log.error("Error getting validation summary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Get specific rule credibility",
            description = "Get credibility analysis for a single rule"
    )
    @GetMapping("/credibility/{ruleId}")
    public ResponseEntity<RuleCredibilityAnalysisDTO> getRuleCredibilityById(
            @Parameter(description = "Rule ID", required = true)
            @PathVariable Integer ruleId,
            
            @Parameter(description = "Start date (YYYY-MM-DD)", required = true)
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "End date (YYYY-MM-DD)", required = true)
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Getting credibility for rule {}", ruleId);
        
        try {
            RuleCredibilityAnalysisDTO analysis = ruleValidationService.analyzeRuleCredibility(
                    Collections.singletonList(ruleId), startDate, endDate).stream()
                    .findFirst()
                    .orElse(null);
            
            if (analysis == null) {
                log.warn("No credibility data found for rule {}", ruleId);
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            log.error("Error analyzing rule {} credibility", ruleId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Health check",
            description = "Check if the rule validation service is operational."
    )
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Rule Validation API");
        health.put("version", "1.0");
        health.put("timestamp", LocalDate.now().toString());
        
        return ResponseEntity.ok(health);
    }
}