package com.example.backend.entities;

import com.example.backend.services.AuditLogListener;
import com.fasterxml.jackson.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity(name = "KpiMetadata")
@Table(schema = "fraude_management",name = "kpi_metadata")
@JsonIdentityInfo(scope = Module.class,resolver = EntityIdResolver.class,generator = ObjectIdGenerators.PropertyGenerator.class,property = "id")
@JsonIgnoreProperties({"group_module","hibernateLazyInitializer", "handler"})
@EntityListeners(AuditLogListener.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class KpiMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String query;

    private String description;
    private String type;
}
