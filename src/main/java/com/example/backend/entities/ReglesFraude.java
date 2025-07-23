package com.example.backend.entities;

import com.example.backend.services.AuditLogListener;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;


@Entity(name = "regles_fraudes")
@Table(schema = "tableref",name = "regles_fraudes")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditLogListener.class)
public class ReglesFraude implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "date_modif")
    private Timestamp dateModif;

    private String description;

    private String etat;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "regle", cascade = CascadeType.ALL, orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE)
    //@JsonIgnore
    private List<ParametresReglesFraude> liste_parameters;

    @OneToMany(mappedBy = "regle", cascade = CascadeType.ALL, orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE)
    //@JsonIgnore
    private List<FiltresReglesFraude> liste_filters;

    @Column(name = "nom_utilisateur")
    private String nomUtilisateur;

    private String nom;

    private Integer time_window;
    private String type;
    @ManyToOne
    @JoinColumn(name = "id_categorie", referencedColumnName = "id")
    private CategoriesFraude categorie;

    @ManyToOne
    @JoinColumn(name = "id_flux", referencedColumnName = "id")
    private Flow flux;
}
