package com.example.backend.entities;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(
        name = "sub_module_report",
        schema = "fraude_management"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubModuleReport implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sort_order")
    private Integer order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_module_id", nullable = false)
    private SubModule subModule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private RepRapports repRapports;
}


