package com.example.backend.entities;

import com.example.backend.services.AuditLogListener;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import java.sql.Timestamp;
@Entity
@Table(schema = "tableref",name = "list_details_appelant")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditLogListener.class)
public class DetailsListAppelant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "date_modif")
    private Timestamp dateModif;

    @Column(name="nom_utilisateur")
    private String nomUtilisateur;

    @ManyToOne
    @JoinColumn(name = "id_hotlist", referencedColumnName = "id", nullable = false)
    @JsonBackReference
    private ListAppelant listAppelant;

    private String hotlistnumber;
}
