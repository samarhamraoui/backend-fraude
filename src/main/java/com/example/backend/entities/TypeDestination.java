package com.example.backend.entities;

import com.example.backend.services.AuditLogListener;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;
@Entity(name = "type_destination")
@Table(schema = "tableref",name = "type_destination")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditLogListener.class)
public class TypeDestination {
    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE)
    private Integer id;

    @Column(name="date_modif")
    private Timestamp dateModif;

    private String description;

    @Column(name="nom_utilisateur")
    private String nomUtilisateur;

    @Column(name="type_dest")
    @JsonProperty("name")
    private String typeDest;
}
