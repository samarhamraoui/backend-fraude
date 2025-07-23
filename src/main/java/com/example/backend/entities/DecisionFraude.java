package com.example.backend.entities;

import com.example.backend.services.AuditLogListener;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity(name = "DecisionFraude")
@Table(schema = "stat", name = "decision_fraude")
@DynamicUpdate
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditLogListener.class)
public class DecisionFraude {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer flag;

    private Integer etat;

    private String decision;

    private String msisdn;

    @Column(name="nom_utilisateur")
    private String nomUtilisateur;

    @Column(name="date_decision", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp dateDecision;

    @Column(name="date_modif")
    private Timestamp dateModif;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="id_regle",referencedColumnName="id")
    private ReglesFraude regle;
}
