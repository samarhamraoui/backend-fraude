package com.example.backend.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log",schema = "fraude_management")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String tableName;
    private LocalDateTime modifiedAt;
    private String action;
    private String affectedRowId;
    private String sqlQuery;

    public AuditLog(String tableName, String  affectedRowId, Long userId, String action, LocalDateTime modifiedAt,String sqlQuery) {
        this.tableName = tableName;
        this.affectedRowId = affectedRowId;
        this.userId = userId;
        this.action = action;
        this.modifiedAt = modifiedAt;
        this.sqlQuery = sqlQuery;
    }
}
