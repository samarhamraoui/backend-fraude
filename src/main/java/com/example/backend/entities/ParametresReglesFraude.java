package com.example.backend.entities;

import com.example.backend.services.AuditLogListener;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity(name = "parametres_regles_fraudes")
@Table(schema = "tableref",name = "parametres_regles_fraudes")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditLogListener.class)
public class ParametresReglesFraude {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name="date_modif")
    private Timestamp dateModif;

    @Column(name="nom_utilisateur")
    private String nomUtilisateur;

    private Integer vegal;

    private Integer vmax;

    private Integer vmin;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="id_parametre",referencedColumnName="id")
    private Flow flow;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="id_regle",referencedColumnName="id")
    @JsonIgnore
    private ReglesFraude regle;
}
