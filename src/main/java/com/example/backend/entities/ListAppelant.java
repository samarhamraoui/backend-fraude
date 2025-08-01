package com.example.backend.entities;

import com.example.backend.services.AuditLogListener;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@Entity(name = "list_appelant")
@Table(schema = "tableref",name = "list_appelant")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditLogListener.class)
public class ListAppelant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "date_modif")
    private Timestamp dateModif;
    @JsonProperty("name")
    private String nom;

    @Column(name="nom_utilisateur")
    private String nomUtilisateur;

    @OneToMany(mappedBy = "listAppelant", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<DetailsListAppelant> details;
}
