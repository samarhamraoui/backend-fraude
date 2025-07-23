package com.example.backend.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KpiDTO {
    private Long kpiId;
    private String name;
    private String description;
    private String type;
    private List<Map<String, Object>> data;
    private String xColumn;
    private List<String> yColumns;
    private List<String> parameters;
}
