package com.example.backend.entities.dto;

import com.example.backend.entities.FiltresFraude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FilterDto {
    private Integer id;
    private String inegal;
    private String vdef;
    private String vegal;
    private String vlike;
    private String vnotlike;
    private FiltresFraude filtreFraude;
}
