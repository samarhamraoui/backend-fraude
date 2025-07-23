package com.example.backend.entities;

import com.example.backend.services.AuditLogListener;
import com.fasterxml.jackson.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity(name = "module")
@Table(schema = "fraude_management",name = "module")
@JsonIdentityInfo(scope = Module.class,resolver = EntityIdResolver.class,generator = ObjectIdGenerators.PropertyGenerator.class,property = "id")
@JsonIgnoreProperties({"group_module","hibernateLazyInitializer", "handler"})
@EntityListeners(AuditLogListener.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Module implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String moduleName;

    private String permission;

    private String icon;

    @Column(name = "sort_order")
    private Integer order;

    @OneToMany(mappedBy = "module", cascade = { CascadeType.REMOVE, CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH })
    private List<SubModule> list_sub_modules;

    @ManyToMany(mappedBy = "module_groups", fetch = FetchType.LAZY,cascade = { CascadeType.DETACH, CascadeType.PERSIST, CascadeType.MERGE ,CascadeType.REMOVE})
    @JsonIgnore
    private List<Group> group_module;
}
