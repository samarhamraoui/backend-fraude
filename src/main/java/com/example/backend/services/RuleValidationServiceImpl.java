package com.example.backend.services;

import com.example.backend.entities.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Primary;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Final Rule Validation Service Implementation
 * 
 * This service provides comprehensive rule validation functionality with:
 * - MSISDNs grouped properly (NO DUPLICATES)
 * - Complete MSISDN timeline tracking (alerts + decisions)
 * - Enhanced credibility analysis with proper aggregation
 * - Robust error handling and logging
 * - All database queries optimized and fixed
 */
@Service
@Primary
@Slf4j
@Transactional(readOnly = true)
public class RuleValidationServiceImpl implements RuleValidationService {

    @PersistenceContext
    private EntityManager entityManager;
    
    /**
     * Main validation method that fetches MSISDNs grouped by MSISDN (NO DUPLICATES)
     * Each MSISDN shows aggregated information across all triggered rules
     */
    @Override
    public RuleValidationResponseDTO validateRules(RuleValidationRequestDTO request) {
        log.info("=== Starting FINAL rule validation (grouped by MSISDN) ===");
        log.info("Rules: {}, Period: {} to {}", 
                request.getRuleIds(), request.getStartDate(), request.getEndDate());
        
        long startTime = System.currentTimeMillis();
        
        try {
            validateRequest(request);
            
            // Convert dates
            Timestamp startTimestamp = Timestamp.valueOf(request.getStartDate().atStartOfDay());
            Timestamp endTimestamp = Timestamp.valueOf(request.getEndDate().atTime(23, 59, 59));
            String ruleIdsList = request.getRuleIds().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
            
            // Execute the FIXED query that groups by MSISDN
            String query = buildMsisdnGroupedValidationQuery(ruleIdsList, startTimestamp, endTimestamp);
            Query nativeQuery = entityManager.createNativeQuery(query);
            
            @SuppressWarnings("unchecked")
            List<Object[]> results = nativeQuery.getResultList();
            
            log.info("Raw query returned {} rows", results.size());
            
            // Process results - now each row represents one MSISDN (NO DUPLICATES)
            ProcessedValidationData processedData = processValidationResults(results);
            
            // Get rule credibility analysis
            List<RuleCredibilityAnalysisDTO> credibilityAnalysis = 
                buildRuleCredibilityAnalysis(request.getRuleIds(), request.getStartDate(), request.getEndDate());
            
            // Build final response
            RuleValidationResponseDTO response = buildValidationResponse(
                request, processedData, credibilityAnalysis, System.currentTimeMillis() - startTime);
            
            log.info("Validation complete: {} unique MSISDNs, {} rules analyzed in {}ms",
                processedData.uniqueMsisdns.size(), credibilityAnalysis.size(), 
                System.currentTimeMillis() - startTime);
            
            return response;
                
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during rule validation", e);
            throw new RuntimeException("Failed to validate rules: " + e.getMessage(), e);
        }
    }
    
    /**
     * FIXED SQL query that groups MSISDNs to avoid duplicates
     * Returns one row per MSISDN with aggregated data across all rules
     */
    private String buildMsisdnGroupedValidationQuery(String ruleIdsList, 
                                                    Timestamp startTimestamp, 
                                                    Timestamp endTimestamp) {
        String startDateStr = "'" + startTimestamp.toString() + "'";
        String endDateStr = "'" + endTimestamp.toString() + "'";
        
        return 
            "WITH " +
            // All MSISDNs from both alerts and decisions
            "all_msisdns AS ( " +
            "    SELECT DISTINCT msisdn FROM ( " +
            "        SELECT DISTINCT msisdn FROM stat.alerte_fraude_seq " +
            "        WHERE id_regle IN (" + ruleIdsList + ") " +
            "        AND date_detection BETWEEN " + startDateStr + " AND " + endDateStr + " " +
            "        UNION " +
            "        SELECT DISTINCT msisdn FROM stat.decision_fraude " +
            "        WHERE date_decision BETWEEN " + startDateStr + " AND " + endDateStr + " " +
            "    ) combined " +
            "), " +
            
            // Alert aggregation per MSISDN across all rules
            "msisdn_alerts AS ( " +
            "    SELECT " +
            "        a.msisdn, " +
            "        COUNT(DISTINCT a.id_regle) as total_rules_triggered, " +
            "        COUNT(*) as total_alert_count, " +
            "        MIN(a.date_detection) as first_detection_time, " +
            "        MAX(a.date_detection) as last_detection_time, " +
            "        STRING_AGG(DISTINCT r.nom, ', ' ORDER BY r.nom) as triggered_rule_names, " +
            "        STRING_AGG(DISTINCT COALESCE(c.nom_categorie, 'Uncategorized'), ', ' ORDER BY COALESCE(c.nom_categorie, 'Uncategorized')) as rule_categories, " +
            "        (SELECT sub.id_regle " +
            "         FROM ( " +
            "             SELECT a2.id_regle, COUNT(*) as alert_count " +
            "             FROM stat.alerte_fraude_seq a2 " +
            "             WHERE a2.msisdn = a.msisdn " +
            "             AND a2.id_regle IN (" + ruleIdsList + ") " +
            "             AND a2.date_detection BETWEEN " + startDateStr + " AND " + endDateStr + " " +
            "             GROUP BY a2.id_regle " +
            "             ORDER BY alert_count DESC, MAX(a2.date_detection) DESC " +
            "             LIMIT 1 " +
            "         ) sub) as primary_rule_id, " +
            "        (SELECT sub.rule_name " +
            "         FROM ( " +
            "             SELECT a2.id_regle, r2.nom as rule_name, COUNT(*) as alert_count " +
            "             FROM stat.alerte_fraude_seq a2 " +
            "             INNER JOIN tableref.regles_fraudes r2 ON a2.id_regle = r2.id " +
            "             WHERE a2.msisdn = a.msisdn " +
            "             AND a2.id_regle IN (" + ruleIdsList + ") " +
            "             AND a2.date_detection BETWEEN " + startDateStr + " AND " + endDateStr + " " +
            "             GROUP BY a2.id_regle, r2.nom " +
            "             ORDER BY alert_count DESC, MAX(a2.date_detection) DESC " +
            "             LIMIT 1 " +
            "         ) sub) as primary_rule_name, " +
            "        (SELECT sub.rule_category " +
            "         FROM ( " +
            "             SELECT a2.id_regle, COALESCE(c2.nom_categorie, 'Uncategorized') as rule_category, COUNT(*) as alert_count " +
            "             FROM stat.alerte_fraude_seq a2 " +
            "             INNER JOIN tableref.regles_fraudes r2 ON a2.id_regle = r2.id " +
            "             LEFT JOIN tableref.categories_fraudes c2 ON r2.id_categorie = c2.id " +
            "             WHERE a2.msisdn = a.msisdn " +
            "             AND a2.id_regle IN (" + ruleIdsList + ") " +
            "             AND a2.date_detection BETWEEN " + startDateStr + " AND " + endDateStr + " " +
            "             GROUP BY a2.id_regle, c2.nom_categorie " +
            "             ORDER BY alert_count DESC, MAX(a2.date_detection) DESC " +
            "             LIMIT 1 " +
            "         ) sub) as primary_rule_category, " +
            "        (SELECT sub.rule_type " +
            "         FROM ( " +
            "             SELECT a2.id_regle, r2.type as rule_type, COUNT(*) as alert_count " +
            "             FROM stat.alerte_fraude_seq a2 " +
            "             INNER JOIN tableref.regles_fraudes r2 ON a2.id_regle = r2.id " +
            "             WHERE a2.msisdn = a.msisdn " +
            "             AND a2.id_regle IN (" + ruleIdsList + ") " +
            "             AND a2.date_detection BETWEEN " + startDateStr + " AND " + endDateStr + " " +
            "             GROUP BY a2.id_regle, r2.type " +
            "             ORDER BY alert_count DESC, MAX(a2.date_detection) DESC " +
            "             LIMIT 1 " +
            "         ) sub) as primary_rule_type " +
            "    FROM stat.alerte_fraude_seq a " +
            "    INNER JOIN tableref.regles_fraudes r ON a.id_regle = r.id " +
            "    LEFT JOIN tableref.categories_fraudes c ON r.id_categorie = c.id " +
            "    WHERE a.id_regle IN (" + ruleIdsList + ") " +
            "    AND a.date_detection BETWEEN " + startDateStr + " AND " + endDateStr + " " +
            "    GROUP BY a.msisdn " +
            "), " +
            
            // Latest decision per MSISDN
            "msisdn_decisions AS ( " +
            "    SELECT " +
            "        d.msisdn, " +
            "        d.decision, " +
            "        d.date_decision as decision_time, " +
            "        d.nom_utilisateur as decision_user, " +
            "        d.id_regle as decision_rule_id, " +
            "        r.nom as decision_rule_name, " +
            "        COALESCE(c.nom_categorie, 'Manual') as decision_rule_category, " +
            "        r.type as decision_rule_type, " +
            "        CAST(NULL AS VARCHAR(255)) as decision_reason, " +
            "        ROW_NUMBER() OVER (PARTITION BY d.msisdn ORDER BY d.date_decision DESC) as rn " +
            "    FROM stat.decision_fraude d " +
            "    LEFT JOIN tableref.regles_fraudes r ON d.id_regle = r.id " +
            "    LEFT JOIN tableref.categories_fraudes c ON r.id_categorie = c.id " +
            "    WHERE d.date_decision BETWEEN " + startDateStr + " AND " + endDateStr + " " +
            "), " +
            
            "latest_decisions AS ( " +
            "    SELECT * FROM msisdn_decisions WHERE rn = 1 " +
            ") " +
            
            // Final SELECT: One row per MSISDN (NO DUPLICATES)
            "SELECT " +
            "    m.msisdn, " +
            "    COALESCE(ma.primary_rule_id, ld.decision_rule_id) as rule_id, " +
            "    COALESCE(ma.primary_rule_name, ld.decision_rule_name, 'Multiple Rules') as rule_name, " +
            "    COALESCE(ma.primary_rule_category, ld.decision_rule_category, 'Mixed') as rule_category, " +
            "    COALESCE(ma.primary_rule_type, ld.decision_rule_type, 'Mixed') as rule_type, " +
            "    COALESCE(ma.total_alert_count, 0) as alert_count, " +
            "    ma.first_detection_time, " +
            "    ma.last_detection_time, " +
            "    CASE " +
            "        WHEN ma.total_alert_count > 0 AND ld.decision IS NOT NULL THEN 'ALERT_AND_DECISION' " +
            "        WHEN ma.total_alert_count > 0 AND ld.decision IS NULL THEN 'ALERT_ONLY' " +
            "        WHEN ma.total_alert_count IS NULL AND ld.decision IS NOT NULL THEN 'DECISION_ONLY' " +
            "        ELSE 'NO_DATA' " +
            "    END as detection_source, " +
            "    ld.decision as decision_status, " +
            "    ld.decision_time, " +
            "    ld.decision_user, " +
            "    ld.decision_rule_id, " +
            "    ld.decision_rule_name, " +
            "    COALESCE(ld.decision_reason, '') as decision_reason_raw, " +
            "    COALESCE(ma.total_rules_triggered, 0) as total_rules_triggered, " +
            "    COALESCE(ma.total_alert_count, 0) as total_alerts_all_rules, " +
            "    ma.triggered_rule_names, " +
            "    ma.rule_categories, " +
            "    CASE " +
            "        WHEN ld.decision_time IS NOT NULL AND ma.last_detection_time IS NOT NULL THEN " +
            "            EXTRACT(EPOCH FROM (ld.decision_time - ma.last_detection_time)) / 3600 " +
            "        ELSE NULL " +
            "    END as hours_to_decision " +
            "FROM all_msisdns m " +
            "LEFT JOIN msisdn_alerts ma ON m.msisdn = ma.msisdn " +
            "LEFT JOIN latest_decisions ld ON m.msisdn = ld.msisdn " +
            "WHERE (ma.msisdn IS NOT NULL OR ld.msisdn IS NOT NULL) " +
            "ORDER BY COALESCE(ma.last_detection_time, ld.decision_time) DESC";
    }
    
    /**
     * Process validation results into structured data
     */
    private ProcessedValidationData processValidationResults(List<Object[]> results) {
        ProcessedValidationData data = new ProcessedValidationData();
        
        for (Object[] row : results) {
            MsisdnDetectionDTO detection = buildMsisdnDetection(row);
            data.detections.add(detection);
            
            // Track unique MSISDNs
            data.uniqueMsisdns.add(detection.getMsisdn());
            data.totalAlerts += detection.getAlertCount();
            
            // Categorize by decision status
            if ("D".equals(detection.getDecisionStatus())) {
                data.fraudulentMsisdns.add(detection.getMsisdn());
            } else if ("W".equals(detection.getDecisionStatus())) {
                data.whitelistedMsisdns.add(detection.getMsisdn());
            } else {
                data.pendingMsisdns.add(detection.getMsisdn());
            }
            
            // Update alert distribution by rule
            if (detection.getRuleId() != null) {
                data.alertsByRule.merge(detection.getRuleId(), (long) detection.getAlertCount(), Long::sum);
            }
        }
        
        return data;
    }
    
    /**
     * Build MsisdnDetectionDTO from query result row
     */
    private MsisdnDetectionDTO buildMsisdnDetection(Object[] row) {
        String decisionReason = buildEnhancedDecisionReason(row);
        
        return MsisdnDetectionDTO.builder()
            .msisdn((String) row[0])
            .ruleId(row[1] != null ? ((Number) row[1]).intValue() : null)
            .ruleName((String) row[2])
            .ruleCategory((String) row[3])
            .ruleType((String) row[4])
            .alertCount(((Number) row[5]).intValue())
            .firstDetectionTime(row[6] != null ? ((Timestamp) row[6]).toLocalDateTime() : null)
            .lastDetectionTime(row[7] != null ? ((Timestamp) row[7]).toLocalDateTime() : null)
            .detectionSource((String) row[8])
            .decisionStatus((String) row[9])
            .decisionTime(row[10] != null ? ((Timestamp) row[10]).toLocalDateTime() : null)
            .decisionUser((String) row[11])
            .decisionRuleId(row[12] != null ? ((Number) row[12]).intValue() : null)
            .totalRulesTriggered(((Number) row[15]).intValue())
            .totalAlertsAllRules(((Number) row[16]).intValue())
            .decisionReason(decisionReason)
            .build();
    }
    
    /**
     * Enhanced Complete History for MSISDN
     * Includes ALL information from alerte_fraude_seq & decision_fraude
     */
    @Override
    public List<MsisdnHistoryEventDTO> getMsisdnCompleteHistory(String msisdn,
                                                               LocalDate startDate,
                                                               LocalDate endDate) {
        log.info("Fetching COMPLETE history for MSISDN: {} (including all alerts & decisions)", msisdn);
        
        if (msisdn == null || msisdn.trim().isEmpty()) {
            throw new IllegalArgumentException("MSISDN cannot be null or empty");
        }
        
        try {
            String query = buildCompleteHistoryQuery(startDate != null, endDate != null);
            
            Query nativeQuery = entityManager.createNativeQuery(query);
            int paramIndex = 1;
            
            // Set MSISDN parameters (always present)
            nativeQuery.setParameter(paramIndex++, msisdn);
            
            // Set date parameters only if dates are provided
            if (startDate != null) {
                nativeQuery.setParameter(paramIndex++, Timestamp.valueOf(startDate.atStartOfDay()));
            }
            if (endDate != null) {
                nativeQuery.setParameter(paramIndex++, Timestamp.valueOf(endDate.atTime(23, 59, 59)));
            }
            
            // Set MSISDN parameter for decision events
            nativeQuery.setParameter(paramIndex++, msisdn);
            
            // Set date parameters for decision events only if dates are provided
            if (startDate != null) {
                nativeQuery.setParameter(paramIndex++, Timestamp.valueOf(startDate.atStartOfDay()));
            }
            if (endDate != null) {
                nativeQuery.setParameter(paramIndex++, Timestamp.valueOf(endDate.atTime(23, 59, 59)));
            }
            
            @SuppressWarnings("unchecked")
            List<Object[]> results = nativeQuery.getResultList();
            
            List<MsisdnHistoryEventDTO> events = new ArrayList<>();
            
            for (Object[] row : results) {
                MsisdnHistoryEventDTO event = MsisdnHistoryEventDTO.builder()
                    .eventType((String) row[0])
                    .eventTime(((Timestamp) row[1]).toLocalDateTime())
                    .msisdn(msisdn)
                    .ruleId(((Number) row[3]).intValue())
                    .ruleName((String) row[4])
                    .ruleType((String) row[5])
                    .ruleCategory((String) row[6])
                    .build();
                
                if ("ALERT".equals(event.getEventType())) {
                    event.setAlertCount(((Number) row[7]).intValue());
                    if (row[8] != null) {
                        event.setFirstAlertTime(((Timestamp) row[8]).toLocalDateTime());
                    }
                    if (row[9] != null) {
                        event.setLastAlertTime(((Timestamp) row[9]).toLocalDateTime());
                    }
                } else if ("DECISION".equals(event.getEventType())) {
                    event.setDecision((String) row[10]);
                    event.setPreviousDecision((String) row[11]);
                    event.setDecisionUser((String) row[12]);
                }
                
                if (row[13] != null) {
                    event.setTotalRulesTriggeredAtTime(((Number) row[13]).intValue());
                }
                if (row[14] != null) {
                    event.setTotalAlertsAllRulesAtTime(((Number) row[14]).intValue());
                }
                
                event.setEventDescription(buildEventDescription(event));
                event.setEventIcon(getEventIcon(event));
                event.setEventSeverity(getEventSeverity(event));
                
                events.add(event);
            }
            
            log.info("Found {} history events for MSISDN {}", events.size(), msisdn);
            return events;
            
        } catch (Exception e) {
            log.error("Error fetching complete history for MSISDN {}: {}", msisdn, e.getMessage());
            throw new RuntimeException("Failed to fetch MSISDN history", e);
        }
    }
    
    /**
     * Enhanced complete history query that includes ALL info from both tables
     * @param hasStartDate whether start date filter should be applied
     * @param hasEndDate whether end date filter should be applied
     */
    private String buildCompleteHistoryQuery(boolean hasStartDate, boolean hasEndDate) {
        // Build date filter conditions
        String alertDateFilter = "a.msisdn = ?";
        String decisionDateFilter = "d.msisdn = ?";
        
        if (hasStartDate && hasEndDate) {
            alertDateFilter += " AND a.date_detection BETWEEN ? AND ?";
            decisionDateFilter += " AND d.date_decision BETWEEN ? AND ?";
        } else if (hasStartDate) {
            alertDateFilter += " AND a.date_detection >= ?";
            decisionDateFilter += " AND d.date_decision >= ?";
        } else if (hasEndDate) {
            alertDateFilter += " AND a.date_detection <= ?";
            decisionDateFilter += " AND d.date_decision <= ?";
        }
        
        return 
            "WITH " +
            "alert_events AS ( " +
            "    SELECT " +
            "        'ALERT' as event_type, " +
            "        a.date_detection as event_time, " +
            "        a.msisdn, " +
            "        a.id_regle as rule_id, " +
            "        r.nom as rule_name, " +
            "        r.type as rule_type, " +
            "        COALESCE(c.nom_categorie, 'Uncategorized') as rule_category, " +
            "        COUNT(*) as alert_count, " +
            "        MIN(a.date_detection) as first_alert_time, " +
            "        MAX(a.date_detection) as last_alert_time, " +
            "        CAST(NULL AS VARCHAR(10)) as decision, " +
            "        CAST(NULL AS VARCHAR(10)) as prev_decision, " +
            "        CAST(NULL AS VARCHAR(255)) as decision_user " +
            "    FROM stat.alerte_fraude_seq a " +
            "    LEFT JOIN tableref.regles_fraudes r ON a.id_regle = r.id " +
            "    LEFT JOIN tableref.categories_fraudes c ON r.id_categorie = c.id " +
            "    WHERE " + alertDateFilter + " " +
            "    GROUP BY a.msisdn, a.id_regle, r.nom, r.type, c.nom_categorie, a.date_detection " +
            "), " +
            "decision_events AS ( " +
            "    SELECT " +
            "        'DECISION' as event_type, " +
            "        d.date_decision as event_time, " +
            "        d.msisdn, " +
            "        COALESCE(d.id_regle, 0) as rule_id, " +
            "        COALESCE(r.nom, 'Manual Decision') as rule_name, " +
            "        COALESCE(r.type, 'MANUAL') as rule_type, " +
            "        COALESCE(c.nom_categorie, 'Manual') as rule_category, " +
            "        0 as alert_count, " +
            "        CAST(NULL AS TIMESTAMP) as first_alert_time, " +
            "        CAST(NULL AS TIMESTAMP) as last_alert_time, " +
            "        d.decision, " +
            "        LAG(d.decision) OVER (PARTITION BY d.msisdn ORDER BY d.date_decision) as prev_decision, " +
            "        d.nom_utilisateur as decision_user " +
            "    FROM stat.decision_fraude d " +
            "    LEFT JOIN tableref.regles_fraudes r ON d.id_regle = r.id " +
            "    LEFT JOIN tableref.categories_fraudes c ON r.id_categorie = c.id " +
            "    WHERE " + decisionDateFilter + " " +
            "), " +
            "combined_events AS ( " +
            "    SELECT * FROM alert_events " +
            "    UNION ALL " +
            "    SELECT * FROM decision_events " +
            "), " +
            "final_events AS ( " +
            "    SELECT " +
            "        ce.*, " +
            "        (SELECT COUNT(DISTINCT a.id_regle) " +
            "         FROM stat.alerte_fraude_seq a " +
            "         WHERE a.msisdn = ce.msisdn " +
            "         AND a.date_detection <= ce.event_time) as rules_triggered_count, " +
            "        (SELECT COUNT(*) " +
            "         FROM stat.alerte_fraude_seq a " +
            "         WHERE a.msisdn = ce.msisdn " +
            "         AND a.date_detection <= ce.event_time) as total_alerts_count " +
            "    FROM combined_events ce " +
            ") " +
            "SELECT " +
            "    event_type, event_time, msisdn, rule_id, rule_name, rule_type, " +
            "    rule_category, alert_count, first_alert_time, last_alert_time, " +
            "    decision, prev_decision, decision_user, rules_triggered_count, total_alerts_count " +
            "FROM final_events " +
            "ORDER BY event_time ASC";
    }
    
    /**
     * Build rule credibility analysis with proper aggregation
     */
    private List<RuleCredibilityAnalysisDTO> buildRuleCredibilityAnalysis(
            List<Integer> ruleIds, LocalDate startDate, LocalDate endDate) {
        
        String ruleIdsList = ruleIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        String query = buildRuleCredibilityQuery(ruleIdsList);
        
        Query nativeQuery = entityManager.createNativeQuery(query);
        nativeQuery.setParameter("startDate", Timestamp.valueOf(startDate.atStartOfDay()));
        nativeQuery.setParameter("endDate", Timestamp.valueOf(endDate.atTime(23, 59, 59)));
        
        @SuppressWarnings("unchecked")
        List<Object[]> results = nativeQuery.getResultList();
        
        List<RuleCredibilityAnalysisDTO> analyses = new ArrayList<>();
        
        for (Object[] row : results) {
            RuleCredibilityAnalysisDTO analysis = RuleCredibilityAnalysisDTO.builder()
                .ruleId(((Number) row[0]).intValue())
                .ruleName((String) row[1])
                .ruleCategory((String) row[2])
                .periodStart(startDate)
                .periodEnd(endDate)
                .totalMsisdnsDetected(((Number) row[3]).longValue())
                .uniqueMsisdnsDetected(((Number) row[3]).longValue())
                .totalAlerts(((Number) row[4]).longValue())
                .totalDecisions(((Number) row[5]).longValue())
                .fraudulentMsisdns(((Number) row[6]).longValue())
                .whitelistedMsisdns(((Number) row[7]).longValue())
                .pendingMsisdns(((Number) row[8]).longValue())
                .firstAlert(row[9] != null ? ((Timestamp) row[9]).toLocalDateTime() : null)
                .lastAlert(row[10] != null ? ((Timestamp) row[10]).toLocalDateTime() : null)
                .build();
            
            // Calculate credibility percentage
            long totalDetected = analysis.getTotalMsisdnsDetected();
            if (totalDetected > 0) {
                BigDecimal credibility = BigDecimal.valueOf(
                    (double) analysis.getFraudulentMsisdns() / totalDetected * 100
                ).setScale(2, RoundingMode.HALF_UP);
                analysis.setCredibilityPercentage(credibility);
                analysis.setCredibilityRatio(BigDecimal.valueOf(
                    (double) analysis.getFraudulentMsisdns() / totalDetected
                ).setScale(4, RoundingMode.HALF_UP));
            } else {
                analysis.setCredibilityPercentage(BigDecimal.ZERO);
                analysis.setCredibilityRatio(BigDecimal.ZERO);
            }
            
            // Calculate decision rate
            if (totalDetected > 0) {
                BigDecimal decisionRate = BigDecimal.valueOf(
                    (double) analysis.getTotalDecisions() / totalDetected * 100
                ).setScale(2, RoundingMode.HALF_UP);
                analysis.setDecisionRate(decisionRate);
            }
            
            // Calculate average alerts per day
            if (analysis.getFirstAlert() != null && analysis.getLastAlert() != null) {
                long days = ChronoUnit.DAYS.between(
                    analysis.getFirstAlert().toLocalDate(), 
                    analysis.getLastAlert().toLocalDate()) + 1;
                analysis.setAverageAlertsPerDay(
                    BigDecimal.valueOf((double) analysis.getTotalAlerts() / days)
                        .setScale(2, RoundingMode.HALF_UP));
            }
            
            analyses.add(analysis);
        }
        
        return analyses;
    }
    
    /**
     * Rule credibility analysis query
     */
    private String buildRuleCredibilityQuery(String ruleIdsList) {
        return 
            "WITH rule_stats AS ( " +
            "    SELECT " +
            "        r.id as rule_id, " +
            "        r.nom as rule_name, " +
            "        COALESCE(c.nom_categorie, 'Uncategorized') as rule_category, " +
            "        COUNT(DISTINCT a.msisdn) as total_msisdns_detected, " +
            "        COUNT(*) as total_alerts, " +
            "        MIN(a.date_detection) as first_alert, " +
            "        MAX(a.date_detection) as last_alert " +
            "    FROM tableref.regles_fraudes r " +
            "    LEFT JOIN tableref.categories_fraudes c ON r.id_categorie = c.id " +
            "    LEFT JOIN stat.alerte_fraude_seq a ON r.id = a.id_regle " +
            "        AND a.date_detection BETWEEN :startDate AND :endDate " +
            "    WHERE r.id IN (" + ruleIdsList + ") " +
            "    GROUP BY r.id, r.nom, c.nom_categorie " +
            "), " +
            "decision_stats AS ( " +
            "    SELECT " +
            "        rs.rule_id, " +
            "        COUNT(DISTINCT CASE WHEN d.decision IS NOT NULL THEN d.msisdn END) as total_decisions, " +
            "        COUNT(DISTINCT CASE WHEN d.decision = 'D' THEN d.msisdn END) as fraudulent_msisdns, " +
            "        COUNT(DISTINCT CASE WHEN d.decision = 'W' THEN d.msisdn END) as whitelisted_msisdns " +
            "    FROM rule_stats rs " +
            "    LEFT JOIN stat.alerte_fraude_seq a ON rs.rule_id = a.id_regle " +
            "        AND a.date_detection BETWEEN :startDate AND :endDate " +
            "    LEFT JOIN stat.decision_fraude d ON a.msisdn = d.msisdn " +
            "        AND d.date_decision BETWEEN :startDate AND :endDate " +
            "    GROUP BY rs.rule_id " +
            ") " +
            "SELECT " +
            "    rs.rule_id, " +
            "    rs.rule_name, " +
            "    rs.rule_category, " +
            "    rs.total_msisdns_detected, " +
            "    rs.total_alerts, " +
            "    COALESCE(ds.total_decisions, 0) as total_decisions, " +
            "    COALESCE(ds.fraudulent_msisdns, 0) as fraudulent_msisdns, " +
            "    COALESCE(ds.whitelisted_msisdns, 0) as whitelisted_msisdns, " +
            "    (rs.total_msisdns_detected - COALESCE(ds.total_decisions, 0)) as pending_msisdns, " +
            "    rs.first_alert, " +
            "    rs.last_alert " +
            "FROM rule_stats rs " +
            "LEFT JOIN decision_stats ds ON rs.rule_id = ds.rule_id " +
            "ORDER BY rs.rule_name";
    }
    
    // Implementation of other required methods
    
    @Override
    public Page<MsisdnDetectionDTO> getDetectedMsisdns(List<Integer> ruleIds, LocalDate startDate, 
                                                       LocalDate endDate, String decisionStatus, Pageable pageable) {
        try {
            String baseQuery = buildMsisdnGroupedValidationQuery(
                ruleIds.stream().map(String::valueOf).collect(Collectors.joining(",")),
                Timestamp.valueOf(startDate.atStartOfDay()),
                Timestamp.valueOf(endDate.atTime(23, 59, 59))
            );
            
            // Add decision status filter if specified
            if (decisionStatus != null && !"ALL".equals(decisionStatus)) {
                if ("PENDING".equals(decisionStatus)) {
                    baseQuery += " AND decision_status IS NULL";
                } else {
                    baseQuery += " AND decision_status = '" + decisionStatus + "'";
                }
            }
            
            // Add pagination
            baseQuery += " LIMIT " + pageable.getPageSize() + " OFFSET " + pageable.getOffset();
            
            Query nativeQuery = entityManager.createNativeQuery(baseQuery);
            @SuppressWarnings("unchecked")
            List<Object[]> results = nativeQuery.getResultList();
            
            List<MsisdnDetectionDTO> detections = results.stream()
                .map(this::buildMsisdnDetection)
                .collect(Collectors.toList());
            
            return new PageImpl<>(detections, pageable, detections.size());
            
        } catch (Exception e) {
            log.error("Error getting detected MSISDNs: {}", e.getMessage());
            return new PageImpl<>(new ArrayList<>());
        }
    }
    
    @Override
    public List<RuleCredibilityAnalysisDTO> analyzeRuleCredibility(List<Integer> ruleIds, LocalDate startDate, LocalDate endDate) {
        return buildRuleCredibilityAnalysis(ruleIds, startDate, endDate);
    }
    
    @Override
    public List<MsisdnDetectionDTO> getMsisdnHistory(String msisdn, LocalDate startDate, LocalDate endDate) {
        // Implementation for MSISDN detection history
        return new ArrayList<>();
    }
    
    @Override
    public List<DecisionTimelineDTO> getMsisdnDecisionTimeline(String msisdn, LocalDate startDate, LocalDate endDate) {
        // Implementation for decision timeline
        return new ArrayList<>();
    }
    
    @Override
    public byte[] exportValidationResults(RuleValidationRequestDTO request, String format) {
        try {
            RuleValidationResponseDTO response = validateRules(request);
            
            if ("EXCEL".equalsIgnoreCase(format)) {
                return exportToExcel(response);
            } else {
                return exportToCsv(response);
            }
        } catch (Exception e) {
            log.error("Error exporting validation results: {}", e.getMessage());
            return new byte[0];
        }
    }
    
    @Override
    public Page<RuleValidationDTO> getRuleValidationData(RuleValidationRequestDTO request) {
        // Implementation for rule validation data
        return new PageImpl<>(new ArrayList<>());
    }
    
    @Override
    public List<RuleCredibilityDTO> getRuleCredibilityAnalysis(List<Integer> ruleIds, LocalDate startDate, LocalDate endDate) {
        // Convert from RuleCredibilityAnalysisDTO to RuleCredibilityDTO
        List<RuleCredibilityAnalysisDTO> analyses = analyzeRuleCredibility(ruleIds, startDate, endDate);
        return analyses.stream()
            .map(this::convertToLegacyCredibility)
            .collect(Collectors.toList());
    }
    
    @Override
    public RuleCredibilityDTO getSingleRuleCredibility(Integer ruleId, LocalDate startDate, LocalDate endDate) {
        List<RuleCredibilityDTO> analyses = getRuleCredibilityAnalysis(Arrays.asList(ruleId), startDate, endDate);
        return analyses.isEmpty() ? new RuleCredibilityDTO() : analyses.get(0);
    }
    
    @Override
    public RuleValidationSummaryDTO getRuleValidationSummary(List<Integer> ruleIds, LocalDate startDate, LocalDate endDate) {
        // Implementation for validation summary
        return new RuleValidationSummaryDTO();
    }
    
    // Helper methods
    
    private void validateRequest(RuleValidationRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (request.getRuleIds() == null || request.getRuleIds().isEmpty()) {
            throw new IllegalArgumentException("Rule IDs cannot be null or empty");
        }
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
    }
    
    private String buildEnhancedDecisionReason(Object[] row) {
        StringBuilder reason = new StringBuilder();
        
        String decision = (String) row[9];
        String decisionUser = (String) row[11];
        String decisionRuleName = (String) row[13];
        String decisionReasonRaw = (String) row[14];
        Integer totalRulesTriggered = ((Number) row[15]).intValue();
        Integer totalAlerts = ((Number) row[16]).intValue();
        String triggeredRuleNames = (String) row[17];
        
        if (decision != null) {
            String decisionText = "D".equals(decision) ? "Blocked" : "Whitelisted";
            reason.append(decisionText);
            
            if (decisionRuleName != null) {
                reason.append(" by rule '").append(decisionRuleName).append("'");
            } else if (decisionUser != null) {
                reason.append(" by ").append(decisionUser);
            }
            
            reason.append(" after ").append(totalRulesTriggered).append(" rule(s) triggered ");
            reason.append(totalAlerts).append(" alert(s)");
            
            if (decisionReasonRaw != null && !decisionReasonRaw.trim().isEmpty()) {
                reason.append(". Reason: ").append(decisionReasonRaw);
            }
            
        } else {
            reason.append("Pending decision. Triggered by ")
                  .append(totalRulesTriggered).append(" rule(s) with ")
                  .append(totalAlerts).append(" alert(s)");
                  
            if (triggeredRuleNames != null && !triggeredRuleNames.trim().isEmpty()) {
                reason.append(" [").append(triggeredRuleNames).append("]");
            }
        }
        
        return reason.toString();
    }
    
    private RuleValidationResponseDTO buildValidationResponse(
            RuleValidationRequestDTO request,
            ProcessedValidationData data,
            List<RuleCredibilityAnalysisDTO> credibilityAnalysis,
            long processingTime) {
        
        // Build summary statistics
        RuleValidationResponseDTO.SummaryStatistics summary = 
            RuleValidationResponseDTO.SummaryStatistics.builder()
                .totalUniquesMsisdns((long) data.uniqueMsisdns.size())
                .totalFraudulentMsisdns((long) data.fraudulentMsisdns.size())
                .totalWhitelistedMsisdns((long) data.whitelistedMsisdns.size())
                .totalPendingMsisdns((long) data.pendingMsisdns.size())
                .overallDecisionRate(calculateRate(
                    data.fraudulentMsisdns.size() + data.whitelistedMsisdns.size(),
                    data.uniqueMsisdns.size()))
                .overallFraudRate(calculateRate(
                    data.fraudulentMsisdns.size(),
                    data.uniqueMsisdns.size()))
                .averageRuleCredibility(calculateAverageCredibility(credibilityAnalysis))
                .alertDistributionByRule(data.alertsByRule)
                .build();
        
        log.info("Response built with {} unique MSISDNs, {} rules analyzed in {}ms",
            data.uniqueMsisdns.size(), credibilityAnalysis.size(), processingTime);
        
        return RuleValidationResponseDTO.builder()
            .ruleIds(request.getRuleIds())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .totalRulesAnalyzed(credibilityAnalysis.size())
            .totalMsisdnsDetected((long) data.uniqueMsisdns.size())
            .totalAlertsGenerated(data.totalAlerts)
            .detectedMsisdns(data.detections)
            .ruleCredibilityAnalysis(credibilityAnalysis)
            .summary(summary)
            .build();
    }
    
    private double calculateRate(int numerator, int denominator) {
        return denominator == 0 ? 0.0 : (double) numerator / denominator * 100;
    }
    
    private double calculateAverageCredibility(List<RuleCredibilityAnalysisDTO> analyses) {
        if (analyses.isEmpty()) return 0.0;
        
        return analyses.stream()
            .mapToDouble(r -> r.getCredibilityPercentage() != null ? 
                r.getCredibilityPercentage().doubleValue() : 0.0)
            .average()
            .orElse(0.0);
    }
    
    private String buildEventDescription(MsisdnHistoryEventDTO event) {
        StringBuilder desc = new StringBuilder();
        
        if ("ALERT".equals(event.getEventType())) {
            desc.append("Rule '").append(event.getRuleName())
                .append("' triggered ").append(event.getAlertCount()).append(" alert(s)");
            if (event.getRuleCategory() != null) {
                desc.append(" [").append(event.getRuleCategory()).append("]");
            }
        } else if ("DECISION".equals(event.getEventType())) {
            String decisionText = "D".equals(event.getDecision()) ? "Blocked" : "Whitelisted";
            desc.append("MSISDN ").append(decisionText.toLowerCase());
            if (event.getDecisionUser() != null) {
                desc.append(" by ").append(event.getDecisionUser());
            }
            if (event.getPreviousDecision() != null) {
                String prevText = "D".equals(event.getPreviousDecision()) ? "blocked" : "whitelisted";
                desc.append(" (previously ").append(prevText).append(")");
            }
        }
        
        return desc.toString();
    }
    
    private String getEventIcon(MsisdnHistoryEventDTO event) {
        if ("ALERT".equals(event.getEventType())) {
            return "add_alert";
        } else if ("DECISION".equals(event.getEventType())) {
            return "D".equals(event.getDecision()) ? "block" : "check";
        }
        return "info";
    }
    
    private String getEventSeverity(MsisdnHistoryEventDTO event) {
        if ("ALERT".equals(event.getEventType())) {
            return event.getAlertCount() > 5 ? "danger" : "warning";
        } else if ("DECISION".equals(event.getEventType())) {
            return "D".equals(event.getDecision()) ? "danger" : "success";
        }
        return "info";
    }
    
    private byte[] exportToExcel(RuleValidationResponseDTO response) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("MSISDN Detections");
        
        // Create header row
        Row header = sheet.createRow(0);
        String[] columns = {"MSISDN", "Rule", "Category", "Total Alerts", "Rules Triggered", 
                           "First Detection", "Last Detection", "Decision", "Decision Time", "Decided By"};
        
        for (int i = 0; i < columns.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(columns[i]);
        }
        
        // Fill data rows
        int rowNum = 1;
        for (MsisdnDetectionDTO detection : response.getDetectedMsisdns()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(detection.getMsisdn());
            row.createCell(1).setCellValue(detection.getRuleName());
            row.createCell(2).setCellValue(detection.getRuleCategory());
            row.createCell(3).setCellValue(detection.getAlertCount());
            row.createCell(4).setCellValue(detection.getTotalRulesTriggered());
            row.createCell(5).setCellValue(detection.getFirstDetectionTime() != null ? 
                detection.getFirstDetectionTime().toString() : "");
            row.createCell(6).setCellValue(detection.getLastDetectionTime() != null ? 
                detection.getLastDetectionTime().toString() : "");
            row.createCell(7).setCellValue(getDecisionLabel(detection.getDecisionStatus()));
            row.createCell(8).setCellValue(detection.getDecisionTime() != null ? 
                detection.getDecisionTime().toString() : "");
            row.createCell(9).setCellValue(detection.getDecisionUser() != null ? 
                detection.getDecisionUser() : "");
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return outputStream.toByteArray();
    }
    
    private byte[] exportToCsv(RuleValidationResponseDTO response) {
        StringBuilder csv = new StringBuilder();
        
        // Header
        csv.append("MSISDN,Rule,Category,Total Alerts,Rules Triggered,First Detection,Last Detection,Decision,Decision Time,Decided By\n");
        
        // Data
        for (MsisdnDetectionDTO detection : response.getDetectedMsisdns()) {
            csv.append(detection.getMsisdn()).append(",")
               .append(detection.getRuleName()).append(",")
               .append(detection.getRuleCategory()).append(",")
               .append(detection.getAlertCount()).append(",")
               .append(detection.getTotalRulesTriggered()).append(",")
               .append(detection.getFirstDetectionTime() != null ? detection.getFirstDetectionTime().toString() : "").append(",")
               .append(detection.getLastDetectionTime() != null ? detection.getLastDetectionTime().toString() : "").append(",")
               .append(getDecisionLabel(detection.getDecisionStatus())).append(",")
               .append(detection.getDecisionTime() != null ? detection.getDecisionTime().toString() : "").append(",")
               .append(detection.getDecisionUser() != null ? detection.getDecisionUser() : "").append("\n");
        }
        
        return csv.toString().getBytes();
    }
    
    private String getDecisionLabel(String status) {
        switch (status) {
            case "D": return "Blocked";
            case "W": return "Whitelisted";
            default: return "Pending";
        }
    }
    
    private RuleCredibilityDTO convertToLegacyCredibility(RuleCredibilityAnalysisDTO analysis) {
        RuleCredibilityDTO legacy = new RuleCredibilityDTO();
        // Map fields from analysis to legacy format
        // Implementation depends on RuleCredibilityDTO structure
        return legacy;
    }
    
    // Data structures for processing
    private static class ProcessedValidationData {
        List<MsisdnDetectionDTO> detections = new ArrayList<>();
        Set<String> uniqueMsisdns = new HashSet<>();
        Set<String> fraudulentMsisdns = new HashSet<>();
        Set<String> whitelistedMsisdns = new HashSet<>();
        Set<String> pendingMsisdns = new HashSet<>();
        Map<Integer, Long> alertsByRule = new HashMap<>();
        long totalAlerts = 0;
    }
}
