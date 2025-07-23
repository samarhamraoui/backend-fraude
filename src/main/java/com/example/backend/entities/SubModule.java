package com.example.backend.entities;

import com.example.backend.services.AuditLogListener;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.NonNull;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "sub_module")
@Table(schema = "fraude_management", name = "sub_module")
@JsonIdentityInfo(
        scope = SubModule.class,
        resolver = EntityIdResolver.class,
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@EntityListeners(AuditLogListener.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SubModule implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "submodulename")
    @NonNull
    private String subModuleName;

    @Column(name = "path")
    @NonNull
    private String path;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id")
    private Module module;

    private String permission;
    private String icon;

    @Column(name = "sort_order")
    private Integer order;

    // Already existing relationships
    @ManyToMany(mappedBy = "liste_submodule",
            cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.DETACH },
            fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Group> group;

    @OneToMany(mappedBy = "subModule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubModuleReport> subModuleReports = new ArrayList<>();

    public void addReport(RepRapports rapport, Integer order) {
        SubModuleReport subModuleReport = new SubModuleReport();
        subModuleReport.setSubModule(this);
        subModuleReport.setRepRapports(rapport);
        subModuleReport.setOrder(order);
        //subModuleReport.setId(new SubModuleReportId(this.id, rapport.getId()));

        this.subModuleReports.add(subModuleReport);
    }
    public void removeReport(RepRapports rapport) {
        this.subModuleReports.removeIf(smr ->
                smr.getRepRapports().getId().equals(rapport.getId()));
    }
}
