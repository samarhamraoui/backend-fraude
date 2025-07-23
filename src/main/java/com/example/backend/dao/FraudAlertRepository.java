package com.example.backend.dao;

import com.example.backend.entities.dto.*;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class FraudAlertRepository {

    @PersistenceContext
    private EntityManager entityManager;

    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyMMddHHmmss");


    // Fetches fraud alerts summary with date filtering
    public List<FraudAlertResponseDTO> getFraudAlerts(String startDate, String endDate) {
        String summaryQuery = "SELECT msisdn, COUNT(DISTINCT id_regle) AS unique_rules, SUM(nombre) AS total_alerts,\n" +
                "                   MIN(date_detection) AS first_date_detection, MAX(date_detection) AS last_date_detection\n" +
                "            FROM (\n" +
                "                SELECT msisdn, id_regle, COUNT(*) AS nombre, date_detection\n" +
                "                FROM stat.alerte_fraude_seq\n" +
                "                WHERE date_detection BETWEEN :startDate AND :endDate\n" +
                "                GROUP BY msisdn, id_regle, date_detection\n" +
                "            ) a\n" +
                "            GROUP BY msisdn\n" +
                "            HAVING COUNT(*) > 1\n" +
                "            ORDER BY last_date_detection DESC";

        Query query = entityManager.createNativeQuery(summaryQuery);
        query.setParameter("startDate", Timestamp.valueOf(startDate + " 00:00:00"));
        query.setParameter("endDate", Timestamp.valueOf(endDate + " 23:59:59"));

        List<Object[]> results = query.getResultList();
        return results.stream().map(row -> new FraudAlertResponseDTO(
                (String) row[0],
                ((Number) row[1]).longValue(),
                ((Number) row[2]).longValue(),
                ((Timestamp) row[3]).toLocalDateTime(),
                ((Timestamp) row[4]).toLocalDateTime(),
                null
        )).collect(Collectors.toList());
    }

    // Fetches fraud alert details for a given msisdn and date range
    public List<FraudAlertDetailDTO> getFraudAlertDetails(String msisdn, LocalDate startDate, LocalDate endDate) {
        String detailsQuery = "SELECT a.nom, a.id, b.date_debut, b.date_fin, b.date_detection, b.nb_occurance, b.datee\n" +
                "            FROM (\n" +
                "                SELECT id_regle, msisdn,\n" +
                "                       MIN(date_debut) AS date_debut,\n" +
                "                       MAX(date_fin) AS date_fin,\n" +
                "                       MAX(date_detection) AS date_detection,\n" +
                "                       COUNT(*) AS nb_occurance,\n" +
                "                       MIN(date_debut) AS datee\n" +
                "                FROM stat.alerte_fraude_seq\n" +
                "                WHERE msisdn = :msisdn\n" +
                "                  AND date_detection BETWEEN :startDate AND :endDate\n" +
                "                GROUP BY id_regle, msisdn\n" +
                "            ) AS b,\n" +
                "            tableref.regles_fraudes AS a\n" +
                "            WHERE a.id = b.id_regle";

        Query query = entityManager.createNativeQuery(detailsQuery);
        query.setParameter("msisdn", msisdn);
        query.setParameter("startDate", Timestamp.valueOf(startDate.atStartOfDay()));
        query.setParameter("endDate", Timestamp.valueOf(endDate.atTime(23, 59, 59)));

        List<Object[]> results = query.getResultList();

        return results.stream().map(row -> new FraudAlertDetailDTO(
                (String) row[0],
                ((Number) row[1]).longValue(),
                (String) row[2],
                (String) row[3],
                ((Timestamp) row[4]).toLocalDateTime(),
                ((Number) row[5]).longValue(),
                (String) row[6]
        )).collect(Collectors.toList());
    }


    public List<WarningResponseDTO> getWarnings(String startDate, String endDate) {
        String warningsQuery = "SELECT r.ID as rule_id, r.nom as rule_name, c.nom_categorie AS category, f.name AS flow, " +
                "       d.first_date_detection, d.last_date_detection, r.date_modif AS createdAt " +
                "FROM tableref.regles_fraudes r " +
                "LEFT JOIN tableref.categories_fraudes c ON r.id_categorie = c.id " +
                "LEFT JOIN etl.etl_flows f ON r.id_flux = f.id " +
                "LEFT JOIN ( " +
                "    SELECT id_regle, " +
                "           MIN(date_detection) AS first_date_detection, " +
                "           MAX(date_detection) AS last_date_detection " +
                "    FROM stat.alerte_fraude_seq " +
                "    WHERE date_detection BETWEEN :startDate AND :endDate " +
                "    GROUP BY id_regle " +
                ") d ON r.ID = d.id_regle " +
                "WHERE r.ID <> 0 AND r.etat = 'A' " +
                "ORDER BY r.nom";

        Query query = entityManager.createNativeQuery(warningsQuery);
        query.setParameter("startDate", Timestamp.valueOf(startDate + " 00:00:00"));
        query.setParameter("endDate", Timestamp.valueOf(endDate + " 23:59:59"));
        List<Object[]> results = query.getResultList();
        return results.stream().map(row -> new WarningResponseDTO(
                ((Number) row[0]).longValue(), // rule_id
                (String) row[1], // rule_name
                (String) row[3], // Flow
                (String) row[2], // Category
                row[4] != null ? ((Timestamp) row[4]).toLocalDateTime() : null, // first_date_detection
                row[5] != null ? ((Timestamp) row[5]).toLocalDateTime() : null, // last_date_detection
                row[6] != null ? ((Timestamp) row[6]).toLocalDateTime() : null, // createdAt
                //getWarningDetails(((Number) row[0]).longValue(), startDate, endDate) // Additional details7
                new ArrayList<>()
        )).collect(Collectors.toList());
    }

    public List<WarningDetailDTO> getWarningDetails(Long ruleId, String startDate, String endDate) {
        try {
            String maxValueQuery = "SELECT max(valeur) FROM stat.regle_parametres_valeur_seq " +
                    "WHERE id_regle = :ruleId GROUP BY id_parametre, id_regle";

            List<Object[]> maxValueResult = entityManager.createNativeQuery(maxValueQuery)
                    .setParameter("ruleId", ruleId)
                    .getResultList();

            boolean hasSingleMaxValue = (maxValueResult != null && maxValueResult.size() == 1);

            String baseQuery = "SELECT " +
                    "    a.msisdn, " +
                    "    to_timestamp(min(substr(a.date_debut, 1, 8)), 'YYMMDDHH24MISS') AS start_date, " +
                    "    to_timestamp(max(substr(a.date_fin, 1, 8)), 'YYMMDDHH24MISS') AS end_date, " +
                    "    max(a.date_detection) AS date_detection, " +
                    "    count(*) AS total_alerts ";

            if (hasSingleMaxValue) {
                baseQuery += ", (SELECT max(valeur) FROM stat.regle_parametres_valeur_seq as b " +
                        "WHERE a.msisdn = b.msisdn AND id_regle = :ruleId " +
                        "GROUP BY id_parametre, id_regle) AS max_value ";
            } else {
                baseQuery += ", CAST(NULL AS DOUBLE PRECISION) AS max_value\n ";
            }

            baseQuery += " FROM stat.alerte_fraude_seq a " +
                    "WHERE id_regle = :ruleId " +
                    "AND date_detection BETWEEN :startDate AND :endDate " +
                    "GROUP BY a.msisdn " +
                    "ORDER BY date_detection DESC";

            Query query = entityManager.createNativeQuery(baseQuery);
            query.setParameter("ruleId", ruleId);
            query.setParameter("startDate", Timestamp.valueOf(startDate + " 00:00:00"));
            query.setParameter("endDate", Timestamp.valueOf(endDate + " 23:59:59"));

            List<Object[]> results = query.getResultList();

            return results.stream().map(row -> new WarningDetailDTO(
                    (String) row[0], // msisdn
                    ((Timestamp) row[1]).toLocalDateTime(),
                    ((Timestamp) row[2]).toLocalDateTime(),
                    ((Timestamp) row[3]).toLocalDateTime(),
                    ((Number) row[4]).longValue(),
                    row[5] != null ? ((Number) row[5]).doubleValue() : null
            )).collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error fetching warning details", e);
        }
    }

    public List<FraudAlertResponseDTO> getFraudAlertsWithDetails(String startDate, String endDate) {
        String queryStr = "SELECT a.msisdn, " +
                "COUNT(DISTINCT a.id_regle) AS unique_rules, " +
                "SUM(a.nombre) AS total_alerts, " +
                "MIN(a.date_detection) AS first_date_detection, " +
                "MAX(a.date_detection) AS last_date_detection, " +
                "r.nom AS rule_name, r.id AS rule_id, " +
                "b.date_debut, b.date_fin, b.date_detection, " +
                "b.nb_occurance, b.datee " +
                "FROM ( " +
                "    SELECT msisdn, id_regle, COUNT(*) AS nombre, date_detection " +
                "    FROM stat.alerte_fraude_seq " +
                "    WHERE date_detection BETWEEN :startDate AND :endDate " +
                "    GROUP BY msisdn, id_regle, date_detection " +
                ") a " +
                "JOIN (" +
                "    SELECT id_regle, msisdn, MIN(date_debut) AS date_debut, " +
                "    MAX(date_fin) AS date_fin, MAX(date_detection) AS date_detection, " +
                "    COUNT(*) AS nb_occurance, MIN(date_debut) AS datee " +
                "    FROM stat.alerte_fraude_seq " +
                "    WHERE date_detection BETWEEN :startDate AND :endDate " +
                "    GROUP BY id_regle, msisdn " +
                ") b ON a.msisdn = b.msisdn AND a.id_regle = b.id_regle " +
                "JOIN tableref.regles_fraudes r ON r.id = a.id_regle " +
                "GROUP BY a.msisdn, r.nom, r.id, b.date_debut, b.date_fin, b.date_detection, " +
                "b.nb_occurance, b.datee " +
                "ORDER BY last_date_detection DESC";

        Query query = entityManager.createNativeQuery(queryStr);
        query.setParameter("startDate", Timestamp.valueOf(startDate + " 00:00:00"));
        query.setParameter("endDate", Timestamp.valueOf(endDate + " 23:59:59"));

        List<Object[]> results = query.getResultList();

        // Map results to DTOs
        Map<String, FraudAlertResponseDTO> alertMap = new HashMap<>();

        for (Object[] row : results) {
            String msisdn = (String) row[0];

            FraudAlertResponseDTO alert = alertMap.computeIfAbsent(msisdn, k -> new FraudAlertResponseDTO(
                    msisdn,
                    ((Number) row[1]).longValue(),
                    ((Number) row[2]).longValue(),
                    ((Timestamp) row[3]).toLocalDateTime(),
                    ((Timestamp) row[4]).toLocalDateTime(),
                    new ArrayList<>()
            ));

            // Add details to the alert
            alert.getDetails().add(new FraudAlertDetailDTO(
                    (String) row[5], // rule name
                    ((Number) row[6]).longValue(), // rule ID
                    (String) row[7], // date_debut
                    (String) row[8], // date_fin
                    ((Timestamp) row[9]).toLocalDateTime(), // date_detection
                    ((Number) row[10]).longValue(), // nb_occurance
                    (String) row[11] // datee
            ));
        }

        return new ArrayList<>(alertMap.values());
    }



    public List<WarningResponseDTO> getWarningsV2(String startDate, String endDate, Pageable pageable) {
        String optimizedQuery = "WITH WarningDetails AS ( " +
                "    SELECT a.id_regle, a.msisdn, " +
                "           MIN(a.date_debut) AS start_date, " +
                "           MAX(a.date_fin) AS end_date, " +
                "           MAX(a.date_detection) AS date_detection" +
                "    FROM stat.alerte_fraude_seq a " +
                "    WHERE a.date_detection BETWEEN :startDate AND :endDate " +
                "    GROUP BY a.id_regle, a.msisdn " +
                ") " +
                "SELECT r.ID AS rule_id, r.nom AS rule_name, c.nom_categorie AS category, f.name AS flow, " +
                "       d.first_date_detection, d.last_date_detection, r.date_modif AS createdAt, " +
                "       wd.msisdn, wd.start_date, wd.end_date, wd.date_detection " +
                "FROM tableref.regles_fraudes r " +
                "LEFT JOIN tableref.categories_fraudes c ON r.id_categorie = c.id " +
                "LEFT JOIN etl.etl_flows f ON r.id_flux = f.id " +
                "LEFT JOIN ( " +
                "    SELECT id_regle, " +
                "           MIN(date_detection) AS first_date_detection, " +
                "           MAX(date_detection) AS last_date_detection " +
                "    FROM stat.alerte_fraude_seq " +
                "    WHERE date_detection BETWEEN :startDate AND :endDate " +
                "    GROUP BY id_regle " +
                ") d ON r.ID = d.id_regle " +
                "LEFT JOIN WarningDetails wd ON r.ID = wd.id_regle " +
                "WHERE r.ID <> 0 AND r.etat = 'A' " +
                "ORDER BY last_date_detection DESC " +
                "LIMIT :size OFFSET :offset";

        Query query = entityManager.createNativeQuery(optimizedQuery);
        query.setParameter("size", pageable.getPageSize());
        query.setParameter("offset", pageable.getOffset());
        query.setParameter("startDate", Timestamp.valueOf(startDate + " 00:00:00"));
        query.setParameter("endDate", Timestamp.valueOf(endDate + " 23:59:59"));

        List<Object[]> results = query.getResultList();

        return results.stream().map(row -> new WarningResponseDTO(
                ((Number) row[0]).longValue(), // rule_id
                (String) row[1],  // rule_name
                (String) row[3],  // flow
                (String) row[2],  // category
                row[4] != null ? ((Timestamp) row[4]).toLocalDateTime() : null, // first_date_detection
                row[5] != null ? ((Timestamp) row[5]).toLocalDateTime() : null, // last_date_detection
                row[6] != null ? ((Timestamp) row[6]).toLocalDateTime() : null, // createdAt
                new ArrayList<>()
        )).collect(Collectors.toList());
    }

    public List<WarningParamsDto> getWarningParams(String msisdn, Integer idRule) {
        String queryString =
                " SELECT b.name AS name, "
                        + "        a.msisdn AS msisdn, "
                        + "        MIN(a.date_debut) AS debut, "
                        + "         MAX(a.date_fin)   AS fin, "
                        + "        AVG(a.valeur) AS avgValeur, "
                        + "        MIN(a.valeur) AS minValeur, "
                        + "        MAX(a.valeur) AS maxValeur, "
                        + "        a.id_parametre, "
                        + "        a.id_regle "
                        + "   FROM stat.regle_parametres_valeur_seq AS a "
                        + "   LEFT JOIN etl.etl_flows AS b ON a.id_parametre = b.id "
                        + "  WHERE a.msisdn = :msisdn "
                        + "    AND a.id_regle = :idRule "
                        + "GROUP BY a.id_parametre, a.id_regle, b.name, a.msisdn";

        Query nativeQuery = entityManager.createNativeQuery(queryString);
        nativeQuery.setParameter("msisdn", msisdn);
        nativeQuery.setParameter("idRule", idRule);

        List<Object[]> results = nativeQuery.getResultList();

        List<WarningParamsDto> dtoList = new ArrayList<>();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyMMddHHmmss");
        for (Object[] row : results) {
            WarningParamsDto dto = new WarningParamsDto();
            dto.setName((String) row[0]);
            dto.setMsisdn((String) row[1]);
            String debutStr = (String) row[2];
            if (debutStr != null && !debutStr.isEmpty()) {
                LocalDateTime localDateTime = LocalDateTime.parse(debutStr, dtf);
                dto.setDebut(localDateTime);
            }

            String finStr = (String) row[3];
            if (finStr != null && !finStr.isEmpty()) {
                LocalDateTime localDateTime = LocalDateTime.parse(finStr, dtf);
                dto.setFin(localDateTime);
            }

            dto.setAvgValeur(toBigDecimal(row[4]));
            dto.setMinValeur(toLong(row[5]));
            dto.setMaxValeur(toLong(row[6]));
            dto.setId(toLong(row[7]));
            dto.setIdRegle(toLong(row[8]));
            dtoList.add(dto);
        }

        return dtoList;
    }

    private BigDecimal toBigDecimal(Object val) {
        if (val == null) return null;
        if (val instanceof BigDecimal) return (BigDecimal) val;
        if (val instanceof Number) return BigDecimal.valueOf(((Number) val).doubleValue());
        return null;
    }

    private Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).longValue();
        return null;
    }
}
