package com.example.backend.entities;

import com.example.backend.services.AuditLogListener;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity(name = "offres")
@Table(schema = "tableref",name = "offres")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditLogListener.class)
public class Offre {
    @Id
    @Column(name="id_offre")
    private String idOffre;
    @Column(name="nom_offre")
    @JsonProperty("name")
    private String nomOffre;
    @Column(name="type_offre")
    private String typeOffre;
}
