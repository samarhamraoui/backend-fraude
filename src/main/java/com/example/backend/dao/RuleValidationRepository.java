package com.example.backend.dao;

import com.example.backend.entities.dto.MsisdnDetectionDTO;
import com.example.backend.entities.dto.RuleCredibilityAnalysisDTO;
import com.example.backend.entities.dto.RuleCredibilityDTO;
import com.example.backend.entities.dto.RuleValidationDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class RuleValidationRepository implements IRuleValidationRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<RuleValidationDTO> getRuleValidationData(List<Integer> ruleIds, LocalDate startDate,
                                                         LocalDate endDate, String msisdn,
                                                         String decisionStatus, Pageable pageable) {

        // Build IN clause for rule IDs
        String ruleIdsString = ruleIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ")
                .append("    a.msisdn, ")
                .append("    a.id_regle, ")
                .append("    r.nom as rule_name, ")
                .append("    r.description as rule_description, ")
                .append("    MIN(a.date_detection) as first_date_detection, ")
                .append("    MAX(a.date_detection) as last_date_detection, ")
                .append("    d.decision as decision_status, ").append("    d.date_decision, ")
                .append("    d.nom_utilisateur as decision_user, ")
                .append("    COUNT(*) as alert_count ")
                .append("FROM stat.alerte_fraude_seq a ")
                .append("LEFT JOIN tableref.regles_fraudes r ON a.id_regle = r.id ")
                .append("LEFT JOIN ( ")
                .append("    SELECT d1.msisdn, d1.id_regle, d1.decision, d1.date_decision, d1.nom_utilisateur ")
                .append("    FROM stat.decision_fraude d1 ")
                .append("    INNER JOIN ( ")
                .append("        SELECT msisdn, id_regle, MAX(date_decision) as max_date ")
                .append("        FROM stat.decision_fraude ")
                .append("        GROUP BY msisdn, id_regle ")
                .append("    ) d2 ON d1.msisdn = d2.msisdn AND d1.id_regle = d2.id_regle AND d1.date_decision = d2.max_date ")
                .append(") d ON a.msisdn = d.msisdn AND a.id_regle = d.id_regle ")
                .append("WHERE a.id_regle IN (").append(ruleIdsString).append(") ")
                .append("AND a.date_detection BETWEEN ?1 AND ?2 ");

        int paramIndex = 3;
        if (msisdn != null && !msisdn.trim().isEmpty()) {
            queryBuilder.append("AND a.msisdn = ?").append(paramIndex).append(" ");
            paramIndex++;
        }

        if (decisionStatus != null && !decisionStatus.trim().isEmpty()) {
            if ("null".equalsIgnoreCase(decisionStatus)) {
                queryBuilder.append("AND d.decision IS NULL ");
            } else {
                queryBuilder.append("AND d.decision = ?").append(paramIndex).append(" ");
                paramIndex++;
            }
        }

        queryBuilder.append("GROUP BY a.msisdn, a.id_regle, r.nom, r.description, d.decision, d.date_decision, d.nom_utilisateur ")
                .append("ORDER BY MAX(a.date_detection) DESC ");

        Query query = entityManager.createNativeQuery(queryBuilder.toString());
        query.setParameter(1, Timestamp.valueOf(startDate.atStartOfDay()));
        query.setParameter(2, Timestamp.valueOf(endDate.atTime(23, 59, 59)));

        paramIndex = 3;
        if (msisdn != null && !msisdn.trim().isEmpty()) {
            query.setParameter(paramIndex, msisdn);
            paramIndex++;
        }

        if (decisionStatus != null && !decisionStatus.trim().isEmpty() && !"null".equalsIgnoreCase(decisionStatus)) {
            query.setParameter(paramIndex, decisionStatus);
        }
        // Get total count for pagination
        StringBuilder countQueryBuilder = new StringBuilder();
        countQueryBuilder.append("SELECT COUNT(*) FROM (")
                .append("SELECT 1 ")
                .append("FROM stat.alerte_fraude_seq a ")
                .append("LEFT JOIN tableref.regles_fraudes r ON a.id_regle = r.id ")
                .append("LEFT JOIN ( ")
                .append("    SELECT d1.msisdn, d1.id_regle, d1.decision, d1.date_decision, d1.nom_utilisateur ")
                .append("    FROM stat.decision_fraude d1 ")
                .append("    INNER JOIN ( ")
                .append("        SELECT msisdn, id_regle, MAX(date_decision) as max_date ")
                .append("        FROM stat.decision_fraude ")
                .append("        GROUP BY msisdn, id_regle ")
                .append("    ) d2 ON d1.msisdn = d2.msisdn AND d1.id_regle = d2.id_regle AND d1.date_decision = d2.max_date ")
                .append(") d ON a.msisdn = d.msisdn AND a.id_regle = d.id_regle ")
                .append("WHERE a.id_regle IN (").append(ruleIdsString).append(") ")
                .append("AND a.date_detection BETWEEN ?1 AND ?2 ");

        paramIndex = 3;
        if (msisdn != null && !msisdn.trim().isEmpty()) {
            countQueryBuilder.append("AND a.msisdn = ?").append(paramIndex).append(" ");
            paramIndex++;
        }

        if (decisionStatus != null && !decisionStatus.trim().isEmpty()) {
            if ("null".equalsIgnoreCase(decisionStatus)) {
                countQueryBuilder.append("AND d.decision IS NULL ");
            } else {
                countQueryBuilder.append("AND d.decision = ?").append(paramIndex).append(" ");
            }
        }

        countQueryBuilder.append("GROUP BY a.msisdn, a.id_regle, r.nom, r.description, d.decision, d.date_decision, d.nom_utilisateur")
                .append(") as subquery");

        Query countQuery = entityManager.createNativeQuery(countQueryBuilder.toString());

        countQuery.setParameter(1, Timestamp.valueOf(startDate.atStartOfDay()));
        countQuery.setParameter(2, Timestamp.valueOf(endDate.atTime(23, 59, 59)));

        paramIndex = 3;
        if (msisdn != null && !msisdn.trim().isEmpty()) {
            countQuery.setParameter(paramIndex, msisdn);
            paramIndex++;
        }

        if (decisionStatus != null && !decisionStatus.trim().isEmpty() && !"null".equalsIgnoreCase(decisionStatus)) {
            countQuery.setParameter(paramIndex, decisionStatus);
        }
        // Apply pagination
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        long totalElements = ((Number) countQuery.getSingleResult()).longValue();

        List<RuleValidationDTO> dtoList = results.stream().map(row -> {
            RuleValidationDTO dto = new RuleValidationDTO();
            dto.setMsisdn((String) row[0]);
            dto.setRuleId(((Number) row[1]).intValue());
            dto.setRuleName((String) row[2]);
            dto.setRuleDescription((String) row[3]);
            dto.setFirstDateDetection(row[4] != null ? ((Timestamp) row[4]).toLocalDateTime() : null);
            dto.setLastDateDetection(row[5] != null ? ((Timestamp) row[5]).toLocalDateTime() : null);
            dto.setDecisionStatus((String) row[6]);
            dto.setDateDecision(row[7] != null ? ((Timestamp) row[7]).toLocalDateTime() : null);
            dto.setDecisionUser((String) row[8]);
            dto.setAlertCount(((Number) row[9]).longValue());
            return dto;
        }).collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, totalElements);
    }

    @Override
    public List<RuleCredibilityDTO> getRuleCredibilityAnalysis(List<Integer> ruleIds, LocalDate startDate, LocalDate endDate) {
        // Build IN clause for rule IDs
        String ruleIdsString = ruleIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        String credibilityQuery =
                "SELECT " +
                        "    r.id as rule_id, " +
                        "    r.nom as rule_name, " +
                        "    r.description as rule_description, " +
                        "    COUNT(DISTINCT a.msisdn) as unique_msisdns, " +
                        "    COUNT(*) as total_alerts, " +
                        "    MIN(a.date_detection) as first_alert, " +
                        "    MAX(a.date_detection) as last_alert, " +
                        "    COUNT(DISTINCT d.id) as total_decisions, " +
                        "    COUNT(DISTINCT CASE WHEN d.decision = 'D' THEN d.id END) as blocked_decisions, " +
                        "    COUNT(DISTINCT CASE WHEN d.decision = 'W' THEN d.id END) as whitelisted_decisions, " +
                        "    MAX(d.date_decision) as last_decision, " +
                        "    AVG(CASE WHEN d.date_decision IS NOT NULL AND a.date_detection IS NOT NULL " +
                        "         THEN EXTRACT(EPOCH FROM (d.date_decision - a.date_detection))/3600.0 END) as avg_decision_time_hours " + "FROM tableref.regles_fraudes r " +
                        "LEFT JOIN stat.alerte_fraude_seq a ON r.id = a.id_regle " +
                        "    AND a.date_detection BETWEEN ?1 AND ?2 " +
                        "LEFT JOIN ( " +
                        "    SELECT d1.msisdn, d1.id_regle, d1.decision, d1.date_decision, d1.id " +
                        "    FROM stat.decision_fraude d1 " +
                        "    INNER JOIN ( " +
                        "        SELECT msisdn, id_regle, MAX(date_decision) as max_date " +
                        "        FROM stat.decision_fraude " +
                        "        GROUP BY msisdn, id_regle " +
                        "    ) d2 ON d1.msisdn = d2.msisdn AND d1.id_regle = d2.id_regle AND d1.date_decision = d2.max_date " +
                        ") d ON a.msisdn = d.msisdn AND a.id_regle = d.id_regle " +
                        "WHERE r.id IN (" + ruleIdsString + ") " +
                        "GROUP BY r.id, r.nom, r.description " +
                        "ORDER BY r.id";

        Query query = entityManager.createNativeQuery(credibilityQuery);
        query.setParameter(1, Timestamp.valueOf(startDate.atStartOfDay()));
        query.setParameter(2, Timestamp.valueOf(endDate.atTime(23, 59, 59)));

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        return results.stream().map(row -> {
            RuleCredibilityDTO dto = new RuleCredibilityDTO();
            dto.setRuleId(((Number) row[0]).intValue());
            dto.setRuleName((String) row[1]);
            dto.setRuleDescription((String) row[2]);
            dto.setPeriodStart(startDate.atStartOfDay());
            dto.setPeriodEnd(endDate.atTime(23, 59, 59));

            Long uniqueMsisdns = row[3] != null ? ((Number) row[3]).longValue() : 0L;
            Long totalAlerts = row[4] != null ? ((Number) row[4]).longValue() : 0L;
            LocalDateTime firstAlert = row[5] != null ? ((Timestamp) row[5]).toLocalDateTime() : null;
            LocalDateTime lastAlert = row[6] != null ? ((Timestamp) row[6]).toLocalDateTime() : null;
            Long totalDecisions = row[7] != null ? ((Number) row[7]).longValue() : 0L;
            Long blockedDecisions = row[8] != null ? ((Number) row[8]).longValue() : 0L;
            Long whitelistedDecisions = row[9] != null ? ((Number) row[9]).longValue() : 0L;
            LocalDateTime lastDecision = row[10] != null ? ((Timestamp) row[10]).toLocalDateTime() : null;
            Double avgDecisionTimeHours = row[11] != null ? ((Number) row[11]).doubleValue() : null;

            dto.setUniqueMsisdns(uniqueMsisdns);
            dto.setTotalAlerts(totalAlerts);
            dto.setFirstAlert(firstAlert);
            dto.setLastAlert(lastAlert);
            dto.setTotalDecisions(totalDecisions);
            dto.setBlockedDecisions(blockedDecisions);
            dto.setWhitelistedDecisions(whitelistedDecisions);
            dto.setPendingAlerts(totalAlerts - totalDecisions);
            dto.setLastDecision(lastDecision);
            dto.setAverageDecisionTimeHours(avgDecisionTimeHours);

            // Calculate metrics
            dto.setAlertsPerDay(daysBetween > 0 ? (double) totalAlerts / daysBetween : 0.0);

            if (totalAlerts > 0) {
                dto.setDecisionRate(BigDecimal.valueOf((double) totalDecisions / totalAlerts * 100)
                        .setScale(2, RoundingMode.HALF_UP));
            } else {
                dto.setDecisionRate(BigDecimal.ZERO);
            }

            if (totalDecisions > 0) {
                dto.setBlockingRate(BigDecimal.valueOf((double) blockedDecisions / totalDecisions * 100)
                        .setScale(2, RoundingMode.HALF_UP));
                dto.setFalsePositiveRate(BigDecimal.valueOf((double) whitelistedDecisions / totalDecisions * 100)
                        .setScale(2, RoundingMode.HALF_UP));
            } else {
                dto.setBlockingRate(BigDecimal.ZERO);
                dto.setFalsePositiveRate(BigDecimal.ZERO);
            }

            // Calculate credibility score
            dto.setCredibilityScore(calculateCredibilityScore(dto));

            return dto;
        }).collect(Collectors.toList());
    }

    private String calculateCredibilityScore(RuleCredibilityDTO dto) {
        BigDecimal decisionRate = dto.getDecisionRate() != null ? dto.getDecisionRate() : BigDecimal.ZERO;
        BigDecimal falsePositiveRate = dto.getFalsePositiveRate() != null ? dto.getFalsePositiveRate() : BigDecimal.ZERO;
        Long totalAlerts = dto.getTotalAlerts() != null ? dto.getTotalAlerts() : 0L;

        // Scoring logic based on decision rate, false positive rate, and alert volume
        int score = 0;

        // Decision rate scoring (0-40 points)
        if (decisionRate.compareTo(BigDecimal.valueOf(80)) >= 0) score += 40;
        else if (decisionRate.compareTo(BigDecimal.valueOf(60)) >= 0) score += 30;
        else if (decisionRate.compareTo(BigDecimal.valueOf(40)) >= 0) score += 20;
        else if (decisionRate.compareTo(BigDecimal.valueOf(20)) >= 0) score += 10;

        // False positive rate scoring (0-30 points) - lower is better
        if (falsePositiveRate.compareTo(BigDecimal.valueOf(10)) <= 0) score += 30;
        else if (falsePositiveRate.compareTo(BigDecimal.valueOf(20)) <= 0) score += 20;
        else if (falsePositiveRate.compareTo(BigDecimal.valueOf(35)) <= 0) score += 10;
        else if (falsePositiveRate.compareTo(BigDecimal.valueOf(50)) <= 0) score += 5;

        // Alert volume scoring (0-30 points)
        if (totalAlerts >= 100) score += 30;
        else if (totalAlerts >= 50) score += 20;
        else if (totalAlerts >= 20) score += 15;
        else if (totalAlerts >= 10) score += 10;
        else if (totalAlerts >= 5) score += 5;

        // Determine credibility level
        if (score >= 85) return "EXCELLENT";
        else if (score >= 70) return "GOOD";
        else if (score >= 50) return "AVERAGE";
        else return "POOR";
    }

    @Override
    public Page<MsisdnDetectionDTO> getDetectedMsisdns(List<Integer> ruleIds, LocalDate startDate, 
                                                       LocalDate endDate, String decisionStatus, 
                                                       Pageable pageable) {
        
        // Build IN clause for rule IDs
        String ruleIdsString = ruleIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ")
                .append("    a.msisdn, ")
                .append("    a.id_regle, ")
                .append("    r.nom as rule_name, ")
                .append("    c.nom as category_name, ")
                .append("    MIN(a.date_detection) as first_detection, ")
                .append("    MAX(a.date_detection) as last_detection, ")
                .append("    COUNT(DISTINCT a.id) as alert_count, ")
                .append("    d.decision, ")
                .append("    d.date_decision, ")
                .append("    d.nom_utilisateur as decision_user ")
                .append("FROM stat.alerte_fraude_seq a ")
                .append("LEFT JOIN tableref.regles_fraudes r ON a.id_regle = r.id ")
                .append("LEFT JOIN tableref.categories_fraudes c ON r.id_categorie = c.id ")
                .append("LEFT JOIN ( ")
                .append("    SELECT d1.msisdn, d1.id_regle, d1.decision, d1.date_decision, ")
                .append("           d1.nom_utilisateur ")
                .append("    FROM stat.decision_fraude d1 ")
                .append("    INNER JOIN ( ")
                .append("        SELECT msisdn, id_regle, MAX(date_decision) as max_date ")
                .append("        FROM stat.decision_fraude ")
                .append("        GROUP BY msisdn, id_regle ")
                .append("    ) d2 ON d1.msisdn = d2.msisdn AND d1.id_regle = d2.id_regle ")
                .append("        AND d1.date_decision = d2.max_date ")
                .append(") d ON a.msisdn = d.msisdn AND a.id_regle = d.id_regle ")
                .append("WHERE a.id_regle IN (").append(ruleIdsString).append(") ")
                .append("AND a.date_detection BETWEEN ?1 AND ?2 ");

        int paramIndex = 3;
        if (decisionStatus != null && !decisionStatus.trim().isEmpty()) {
            if ("null".equalsIgnoreCase(decisionStatus) || "pending".equalsIgnoreCase(decisionStatus)) {
                queryBuilder.append("AND d.decision IS NULL ");
            } else {
                queryBuilder.append("AND d.decision = ?").append(paramIndex).append(" ");
                paramIndex++;
            }
        }

        queryBuilder.append("GROUP BY a.msisdn, a.id_regle, r.nom, c.nom, ")
                .append("         d.decision, d.date_decision, d.nom_utilisateur ")
                .append("ORDER BY MAX(a.date_detection) DESC ");

        // Count query
        StringBuilder countQueryBuilder = new StringBuilder();
        countQueryBuilder.append("SELECT COUNT(DISTINCT CONCAT(a.msisdn, '-', a.id_regle)) ")
                .append("FROM stat.alerte_fraude_seq a ")
                .append("LEFT JOIN ( ")
                .append("    SELECT d1.msisdn, d1.id_regle, d1.decision ")
                .append("    FROM stat.decision_fraude d1 ")
                .append("    INNER JOIN ( ")
                .append("        SELECT msisdn, id_regle, MAX(date_decision) as max_date ")
                .append("        FROM stat.decision_fraude ")
                .append("        GROUP BY msisdn, id_regle ")
                .append("    ) d2 ON d1.msisdn = d2.msisdn AND d1.id_regle = d2.id_regle ")
                .append("        AND d1.date_decision = d2.max_date ")
                .append(") d ON a.msisdn = d.msisdn AND a.id_regle = d.id_regle ")
                .append("WHERE a.id_regle IN (").append(ruleIdsString).append(") ")
                .append("AND a.date_detection BETWEEN ?1 AND ?2 ");

        if (decisionStatus != null && !decisionStatus.trim().isEmpty()) {
            if ("null".equalsIgnoreCase(decisionStatus) || "pending".equalsIgnoreCase(decisionStatus)) {
                countQueryBuilder.append("AND d.decision IS NULL ");
            } else {
                countQueryBuilder.append("AND d.decision = ?3 ");
            }
        }

        // Create and execute queries
        Query query = entityManager.createNativeQuery(queryBuilder.toString());
        Query countQuery = entityManager.createNativeQuery(countQueryBuilder.toString());

        // Set parameters
        query.setParameter(1, Timestamp.valueOf(startDate.atStartOfDay()));
        query.setParameter(2, Timestamp.valueOf(endDate.atTime(23, 59, 59)));
        
        countQuery.setParameter(1, Timestamp.valueOf(startDate.atStartOfDay()));
        countQuery.setParameter(2, Timestamp.valueOf(endDate.atTime(23, 59, 59)));

        if (decisionStatus != null && !decisionStatus.trim().isEmpty() && 
            !"null".equalsIgnoreCase(decisionStatus) && !"pending".equalsIgnoreCase(decisionStatus)) {
            query.setParameter(3, decisionStatus);
            countQuery.setParameter(3, decisionStatus);
        }

        // Apply pagination
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        // Execute queries
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        long totalElements = ((Number) countQuery.getSingleResult()).longValue();

        // Map results to DTOs
        List<MsisdnDetectionDTO> dtoList = results.stream().map(row -> {
            return MsisdnDetectionDTO.builder()
                    .msisdn((String) row[0])
                    .ruleId(((Number) row[1]).intValue())
                    .ruleName((String) row[2])
                    .ruleCategory((String) row[3])
                    .firstDetectionTime(row[4] != null ? ((Timestamp) row[4]).toLocalDateTime() : null)
                    .lastDetectionTime(row[5] != null ? ((Timestamp) row[5]).toLocalDateTime() : null)
                    .alertCount(((Number) row[6]).intValue())
                    .decisionStatus((String) row[7])
                    .decisionTime(row[8] != null ? ((Timestamp) row[8]).toLocalDateTime() : null)
                    .decisionUser((String) row[9])
                    .detectionSource(null) // Not available in database
                    .decisionReason(null) // Not available in database
                    .build();
        }).collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, totalElements);
    }

    @Override
    public List<RuleCredibilityAnalysisDTO> calculateRuleCredibility(List<Integer> ruleIds, 
                                                                     LocalDate startDate, 
                                                                     LocalDate endDate) {
        
        // Build IN clause for rule IDs
        String ruleIdsString = ruleIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        String query = "SELECT " +
                "    r.id as rule_id, " +
                "    r.nom as rule_name, " +
                "    r.description as rule_description, " +
                "    c.nom as category_name, " +
                "    COUNT(DISTINCT a.msisdn) as total_msisdns, " +
                "    COUNT(DISTINCT a.msisdn) as unique_msisdns, " +
                "    COUNT(*) as total_alerts, " +
                "    COUNT(DISTINCT CASE WHEN d.decision IS NOT NULL THEN a.msisdn END) as total_decisions, " +
                "    COUNT(DISTINCT CASE WHEN d.decision = 'D' THEN a.msisdn END) as fraudulent_msisdns, " +
                "    COUNT(DISTINCT CASE WHEN d.decision = 'W' THEN a.msisdn END) as whitelisted_msisdns, " +
                "    COUNT(DISTINCT CASE WHEN d.decision IS NULL THEN a.msisdn END) as pending_msisdns, " +
                "    MIN(a.date_detection) as first_alert, " +
                "    MAX(a.date_detection) as last_alert, " +
                "    AVG(CASE WHEN d.date_decision IS NOT NULL AND a.date_detection IS NOT NULL " +
                "        THEN EXTRACT(EPOCH FROM (d.date_decision - a.date_detection))/3600.0 END) as avg_decision_hours " +
                "FROM tableref.regles_fraudes r " +
                "LEFT JOIN tableref.categories_fraudes c ON r.id_categorie = c.id " +
                "LEFT JOIN stat.alerte_fraude_seq a ON r.id = a.id_regle " +
                "    AND a.date_detection BETWEEN ?1 AND ?2 " +
                "LEFT JOIN ( " +
                "    SELECT d1.msisdn, d1.id_regle, d1.decision, d1.date_decision " +
                "    FROM stat.decision_fraude d1 " +
                "    INNER JOIN ( " +
                "        SELECT msisdn, id_regle, MAX(date_decision) as max_date " +
                "        FROM stat.decision_fraude " +
                "        GROUP BY msisdn, id_regle " +
                "    ) d2 ON d1.msisdn = d2.msisdn AND d1.id_regle = d2.id_regle " +
                "        AND d1.date_decision = d2.max_date " +
                ") d ON a.msisdn = d.msisdn AND a.id_regle = d.id_regle " +
                "WHERE r.id IN (" + ruleIdsString + ") " +
                "GROUP BY r.id, r.nom, r.description, c.nom " +
                "ORDER BY r.id";

        Query nativeQuery = entityManager.createNativeQuery(query);
        nativeQuery.setParameter(1, Timestamp.valueOf(startDate.atStartOfDay()));
        nativeQuery.setParameter(2, Timestamp.valueOf(endDate.atTime(23, 59, 59)));

        @SuppressWarnings("unchecked")
        List<Object[]> results = nativeQuery.getResultList();
        
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        return results.stream().map(row -> {
            RuleCredibilityAnalysisDTO dto = new RuleCredibilityAnalysisDTO();
            
            // Basic rule info
            dto.setRuleId(((Number) row[0]).intValue());
            dto.setRuleName((String) row[1]);
            dto.setRuleDescription((String) row[2]);
            dto.setRuleCategory((String) row[3]);
            
            // Period
            dto.setPeriodStart(startDate);
            dto.setPeriodEnd(endDate);
            
            // Metrics
            Long totalMsisdns = row[4] != null ? ((Number) row[4]).longValue() : 0L;
            Long uniqueMsisdns = row[5] != null ? ((Number) row[5]).longValue() : 0L;
            Long totalAlerts = row[6] != null ? ((Number) row[6]).longValue() : 0L;
            Long totalDecisions = row[7] != null ? ((Number) row[7]).longValue() : 0L;
            Long fraudulentMsisdns = row[8] != null ? ((Number) row[8]).longValue() : 0L;
            Long whitelistedMsisdns = row[9] != null ? ((Number) row[9]).longValue() : 0L;
            Long pendingMsisdns = row[10] != null ? ((Number) row[10]).longValue() : 0L;
            
            dto.setTotalMsisdnsDetected(totalMsisdns);
            dto.setUniqueMsisdnsDetected(uniqueMsisdns);
            dto.setTotalAlerts(totalAlerts);
            dto.setTotalDecisions(totalDecisions);
            dto.setFraudulentMsisdns(fraudulentMsisdns);
            dto.setWhitelistedMsisdns(whitelistedMsisdns);
            dto.setPendingMsisdns(pendingMsisdns);
            
            // Time metrics
            dto.setFirstAlert(row[11] != null ? ((Timestamp) row[11]).toLocalDateTime() : null);
            dto.setLastAlert(row[12] != null ? ((Timestamp) row[12]).toLocalDateTime() : null);
            dto.setAverageDecisionTimeHours(row[13] != null ? 
                BigDecimal.valueOf(((Number) row[13]).doubleValue()).setScale(2, RoundingMode.HALF_UP) : null);
            
            // Calculate rates
            dto.setAverageAlertsPerDay(daysBetween > 0 ? 
                BigDecimal.valueOf((double) totalAlerts / daysBetween).setScale(2, RoundingMode.HALF_UP) : 
                BigDecimal.ZERO);
            
            dto.setDecisionRate(totalMsisdns > 0 ? 
                BigDecimal.valueOf((double) totalDecisions / totalMsisdns * 100).setScale(2, RoundingMode.HALF_UP) : 
                BigDecimal.ZERO);
            
            // Calculate credibility
            dto.calculateCredibility();
            
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<MsisdnDetectionDTO> getMsisdnHistory(String msisdn, LocalDate startDate, LocalDate endDate) {
        
        String query = "SELECT " +
                "    a.msisdn, " +
                "    a.id_regle, " +
                "    r.nom as rule_name, " +
                "    c.nom as category_name, " +
                "    a.date_detection, " +
                "    a.date_detection as last_detection, " +
                "    1 as alert_count, " +
                "    d.decision, " +
                "    d.date_decision, " +
                "    d.nom_utilisateur as decision_user " +
                "FROM stat.alerte_fraude_seq a " +
                "LEFT JOIN tableref.regles_fraudes r ON a.id_regle = r.id " +
                "LEFT JOIN tableref.categories_fraudes c ON r.id_categorie = c.id " +
                "LEFT JOIN stat.decision_fraude d ON a.msisdn = d.msisdn " +
                "    AND a.id_regle = d.id_regle " +
                "    AND a.date_detection <= d.date_decision " +
                "WHERE a.msisdn = ?1 " +
                "AND a.date_detection BETWEEN ?2 AND ?3 " +
                "ORDER BY a.date_detection DESC";

        Query nativeQuery = entityManager.createNativeQuery(query);
        nativeQuery.setParameter(1, msisdn);
        nativeQuery.setParameter(2, Timestamp.valueOf(startDate.atStartOfDay()));
        nativeQuery.setParameter(3, Timestamp.valueOf(endDate.atTime(23, 59, 59)));

        @SuppressWarnings("unchecked")
        List<Object[]> results = nativeQuery.getResultList();

        return results.stream().map(row -> {
            return MsisdnDetectionDTO.builder()
                    .msisdn((String) row[0])
                    .ruleId(((Number) row[1]).intValue())
                    .ruleName((String) row[2])
                    .ruleCategory((String) row[3])
                    .firstDetectionTime(row[4] != null ? ((Timestamp) row[4]).toLocalDateTime() : null)
                    .lastDetectionTime(row[5] != null ? ((Timestamp) row[5]).toLocalDateTime() : null)
                    .alertCount(((Number) row[6]).intValue())
                    .decisionStatus((String) row[7])
                    .decisionTime(row[8] != null ? ((Timestamp) row[8]).toLocalDateTime() : null)
                    .decisionUser((String) row[9])
                    .detectionSource(null) // Not available in database
                    .decisionReason(null) // Not available in database
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public String calculateCredibilityScore(Long totalAlerts, Long totalDecisions, 
                                          Long whitelistedDecisions, Long pendingAlerts) {
        
        // Handle null values
        totalAlerts = totalAlerts != null ? totalAlerts : 0L;
        totalDecisions = totalDecisions != null ? totalDecisions : 0L;
        whitelistedDecisions = whitelistedDecisions != null ? whitelistedDecisions : 0L;
        pendingAlerts = pendingAlerts != null ? pendingAlerts : 0L;
        
        // Calculate decision rate
        BigDecimal decisionRate = totalAlerts > 0 ? 
            BigDecimal.valueOf((double) totalDecisions / totalAlerts * 100) : BigDecimal.ZERO;
        
        // Calculate false positive rate
        BigDecimal falsePositiveRate = totalDecisions > 0 ? 
            BigDecimal.valueOf((double) whitelistedDecisions / totalDecisions * 100) : BigDecimal.ZERO;
        
        // Scoring logic
        int score = 0;
        
        // Decision rate scoring (0-40 points)
        if (decisionRate.compareTo(BigDecimal.valueOf(80)) >= 0) score += 40;
        else if (decisionRate.compareTo(BigDecimal.valueOf(60)) >= 0) score += 30;
        else if (decisionRate.compareTo(BigDecimal.valueOf(40)) >= 0) score += 20;
        else if (decisionRate.compareTo(BigDecimal.valueOf(20)) >= 0) score += 10;
        
        // False positive rate scoring (0-30 points) - lower is better
        if (falsePositiveRate.compareTo(BigDecimal.valueOf(10)) <= 0) score += 30;
        else if (falsePositiveRate.compareTo(BigDecimal.valueOf(20)) <= 0) score += 20;
        else if (falsePositiveRate.compareTo(BigDecimal.valueOf(35)) <= 0) score += 10;
        else if (falsePositiveRate.compareTo(BigDecimal.valueOf(50)) <= 0) score += 5;
        
        // Alert volume scoring (0-30 points)
        if (totalAlerts >= 100) score += 30;
        else if (totalAlerts >= 50) score += 20;
        else if (totalAlerts >= 20) score += 15;
        else if (totalAlerts >= 10) score += 10;
        else if (totalAlerts >= 5) score += 5;
        
        // Determine credibility level
        if (score >= 85) return "EXCELLENT";
        else if (score >= 70) return "GOOD";
        else if (score >= 50) return "AVERAGE";
        else return "POOR";
    }
}
