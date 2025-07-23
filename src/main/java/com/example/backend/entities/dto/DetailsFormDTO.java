package com.example.backend.entities.dto;

import lombok.Data;

@Data
public class DetailsFormDTO {
    private String type;
    private Integer idDetail;
    private Integer idPrincipal;
    private String value;
}
