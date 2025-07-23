package com.example.backend.entities;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;
@Data
@Entity(name = "BlocageLog")
@Table(name = "blocage_log",schema = "fraude_management")
public class BlocageLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action;
    private String username;
    private Timestamp dateAction;
}
