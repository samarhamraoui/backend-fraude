package com.example.backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import javax.validation.constraints.Pattern;
import java.util.List;

@Entity(name = "Flow")
@Table(schema = "etl", name = "etl_flows")
@DynamicUpdate
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Flow {
    @Id
    private Long id;

    @Column(name = "name")
    @Pattern(regexp = "([a-zA-Z_]*[0-9]*)", message = "Flow name must not contain special characters or whitespaces !!")
    private String flowName;

    @Pattern(regexp = "([a-zA-Z_]*[0-9]*)", message = "Table name must not contain special characters or whitespaces !!")
    private String table_name;

    private Long status;

    @ManyToOne
    @JoinColumn(name = "id_type_flow", referencedColumnName = "id")
    private EtlTypeFlow flow_type;

    @OneToOne
    @JoinColumn(name = "id_proc", referencedColumnName = "id")
    @JsonIgnore
    private EtlProc proc;
}
