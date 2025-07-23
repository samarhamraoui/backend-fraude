package com.example.backend.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "RepRapports")
@Table(schema = "etl", name = "rep_rapports")
@DynamicUpdate
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
//@EntityListeners(AuditLogListener.class)
public class RepRapports {
    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE)
    private Integer id;
    private String title;
    private String name;

    @OneToMany(mappedBy = "repRapports", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubModuleReport> subModuleReports = new ArrayList<>();

    public void addSubModule(SubModule subModule, Integer order) {
        SubModuleReport subModuleReport = new SubModuleReport();
        subModuleReport.setRepRapports(this);
        subModuleReport.setSubModule(subModule);
        subModuleReport.setOrder(order);
        //subModuleReport.setId(new SubModuleReportId(subModule.getId(), this.id));

        this.subModuleReports.add(subModuleReport);
    }

    public void removeSubModule(SubModule subModule) {
        this.subModuleReports.removeIf(smr ->
                smr.getSubModule().getId().equals(subModule.getId()));
    }
}
