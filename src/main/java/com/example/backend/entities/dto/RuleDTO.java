package com.example.backend.entities.dto;

import com.example.backend.entities.CategoriesFraude;
import com.example.backend.entities.Flow;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RuleDTO {
    private String nom;
    private String description;
    private String etat;
    private String type;
    private CategoriesFraude categorie;
    private Flow flux;
    private List<ParameterDto> liste_parameters;
    private List<FilterDto> liste_filters;
}
