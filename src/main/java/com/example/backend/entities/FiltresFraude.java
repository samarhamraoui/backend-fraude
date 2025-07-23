package com.example.backend.entities;

import com.example.backend.services.AuditLogListener;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity(name = "filtres_fraudes")
@Table(name = "filtres_fraudes",schema = "tableref")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditLogListener.class)
public class FiltresFraude {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer id;
    private String filtre;
}
