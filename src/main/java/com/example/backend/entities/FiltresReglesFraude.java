package com.example.backend.entities;

import com.example.backend.services.AuditLogListener;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity(name = "filtres_regles_fraudes")
@Table(schema = "tableref",name = "filtres_regles_fraudes")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditLogListener.class)
public class FiltresReglesFraude {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "date_modif")
    private Timestamp dateModif;

    @Column(name = "nom_utlisateur")
    private String nomUtlisateur;

    @Column(name = "inegal", length = 100)
    private String inegal;

    @Column(name = "vdef", length = 100)
    private String vdef;
    @Column(name = "vegal", length = 100)
    private String vegal;
    @Column(name = "vlike", length = 100)
    private String vlike;
    @Column(name = "vnotlike", length = 100)
    private String vnotlike;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_filtre", referencedColumnName = "id")
    private FiltresFraude filtreFraude;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_regle", referencedColumnName = "id")
    @JsonIgnore
    private ReglesFraude regle;

}
