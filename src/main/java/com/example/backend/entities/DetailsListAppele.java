package com.example.backend.entities;


import com.example.backend.services.AuditLogListener;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(schema = "tableref",name = "list_details_appele")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditLogListener.class)
public class DetailsListAppele {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Timestamp dateModif;

    @Column(name="nom_utilisateur")
    private String nomUtilisateur;

    @ManyToOne
    @JoinColumn(name = "id_hotlist", referencedColumnName = "id", nullable = false)
    @JsonBackReference
    private ListAppele listAppele;

    private String hotlistnumber;
}
