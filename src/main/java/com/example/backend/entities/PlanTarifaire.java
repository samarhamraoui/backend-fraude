package com.example.backend.entities;

import com.example.backend.services.AuditLogListener;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity(name = "plan_tarifaire")
@Table(schema = "tableref",name = "plan_tarifaire")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditLogListener.class)
public class PlanTarifaire {
    @Id
    @Column(name="code_plan_tarifaire")
    private String codePlanTarifaire;

    @Column(name="date_modif")
    private Timestamp dateModif;

    @Column(name="nom_utilisateur")
    private String nomUtilisateur;

    @Column(name="plan_tarifaire")
    @JsonProperty("name")
    private String planTarifaire;

    private String type;
}
