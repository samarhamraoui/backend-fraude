package com.example.backend.entities.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RepRapportDTO {
        private Integer id;
        private String title;
        private String name;
}
