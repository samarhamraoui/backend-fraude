package com.example.backend.entities.dto;

import lombok.Data;

import java.util.Map;
@Data
public class KpiRequest {
    private Long userId;
    private Map<String, Object> params;
}
