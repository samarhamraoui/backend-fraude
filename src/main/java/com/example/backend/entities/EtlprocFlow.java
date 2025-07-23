package com.example.backend.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
@Entity(name = "EtlprocFlow")
@Table(schema = "etl", name = "etl_proc_flow")
@DynamicUpdate
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EtlprocFlow {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_proc", referencedColumnName = "id")
    private EtlProc proc;

    @ManyToOne
    @JoinColumn(name = "id_flow", referencedColumnName = "id")
    private Flow flow;

    private String query_filters;
}
