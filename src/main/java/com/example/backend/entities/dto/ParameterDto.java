package com.example.backend.entities.dto;

import com.example.backend.entities.Flow;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ParameterDto {
    private Long id;
    private Integer vegal;
    private Integer vmax;
    private Integer vmin;
    private Flow flow;
}
