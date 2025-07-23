package com.example.backend.entities;

import com.example.backend.services.AuditLogListener;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "Blocage")
@Table(name = "blocage",schema = "fraude_management")
@EntityListeners(AuditLogListener.class)
public class Blocage implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String msisdn;
    private String type;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
