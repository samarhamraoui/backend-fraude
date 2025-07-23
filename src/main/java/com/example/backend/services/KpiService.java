package com.example.backend.services;

import com.example.backend.dao.KpiRepository;
import com.example.backend.entities.KpiMetadata;
import com.example.backend.entities.dto.KpiDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class KpiService {
    private static final Pattern PARAM_PATTERN = Pattern.compile("\\{(\\w+)\\}");
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private KpiRepository kpiRepository;

    public KpiDTO getKpiDTO(Long kpiId, Long userId,Map<String, Object> params) {
        Optional<KpiMetadata> kpiOpt = kpiRepository.findById(kpiId);
        if (!kpiOpt.isPresent()) {
            throw new RuntimeException("KPI not found");
        }

        KpiMetadata kpi = kpiOpt.get();
        if (!kpi.getUserId().equals(userId)) {
            throw new RuntimeException("KPI not found for this user");
        }
        // Extract parameter names from query
        List<String> paramNames = extractParameterNames(kpi.getQuery());
        validateParameters(paramNames, params);
        Map<String, Object> convertedParams = convertParameters(params);
        String safeQuery = createSafeQuery(kpi.getQuery(), paramNames);

        List<Map<String, Object>> queryResults = namedParameterJdbcTemplate.queryForList(
                safeQuery,
                new MapSqlParameterSource(convertedParams)
        );

        String xCol = null;
        List<String> yCols = new ArrayList<>();

        if (!queryResults.isEmpty()) {
            Set<String> columns = queryResults.get(0).keySet();
            for (String col : columns) {
                if (col.toLowerCase().startsWith("x_")) {
                    xCol = col;
                } else {
                    yCols.add(col);
                }
            }
        }
        KpiDTO dto = new KpiDTO();
        dto.setKpiId(kpi.getId());
        dto.setName(kpi.getName());
        dto.setDescription(kpi.getDescription());
        dto.setType(kpi.getType());
        dto.setData(queryResults);
        dto.setXColumn(xCol);
        dto.setYColumns(yCols);
        dto.setParameters(paramNames);

        return dto;
    }

    private List<String> extractParameterNames(String query) {
        Matcher matcher = PARAM_PATTERN.matcher(query);
        List<String> params = new ArrayList<>();
        while (matcher.find()) {
            params.add(matcher.group(1));
        }
        return params.stream().distinct().collect(Collectors.toList());
    }

    private void validateParameters(List<String> requiredParams, Map<String, Object> receivedParams) {
        if (!receivedParams.keySet().containsAll(requiredParams)) {
            throw new IllegalArgumentException("Missing parameters. Required: " + requiredParams);
        }
    }

    private Map<String, Object> convertParameters(Map<String, Object> rawParams) {
        Map<String, Object> converted = new HashMap<>();
        rawParams.forEach((key, value) -> {
            if (value instanceof String) {
                try {
                    converted.put(key, LocalDateTime.parse((String) value));
                } catch (DateTimeParseException e) {
                    converted.put(key, value);
                }
            } else {
                converted.put(key, value);
            }
        });
        return converted;
    }

    private String createSafeQuery(String originalQuery, List<String> paramNames) {
        String safeQuery = originalQuery;
        for (String param : paramNames) {
            safeQuery = safeQuery.replace("{" + param + "}", ":" + param);
        }
        return safeQuery;
    }

    public List<KpiMetadata> getUserKpis(Long userId) {
        return kpiRepository.findByUserId(userId);
    }
}
