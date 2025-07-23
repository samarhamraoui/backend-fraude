package com.example.backend.entities;

import java.io.Serializable;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SubModuleReportId implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long subModuleId;
    private Integer reportId;
    @Column(name = "instance_id")
    private Long instanceId;
}
