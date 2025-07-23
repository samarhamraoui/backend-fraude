package com.example.backend.entities.dto;

import com.example.backend.entities.RepRapports;
import com.example.backend.entities.SubModule;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubModuleReportDTO {
    private Integer order;
    private RepRapportDTO repRapports;
}
