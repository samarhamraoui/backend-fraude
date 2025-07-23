package com.example.backend.entities;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
@Entity(name = "EtlProc")
@Table(schema = "etl", name = "etl_procs")
@DynamicUpdate
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EtlProc {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String name;
    private String proc_type;
    private String query_join;
    private Boolean time_consideration;
    private Boolean is_table;
}
