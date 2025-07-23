package com.example.backend.entities;

import com.example.backend.services.AuditLogListener;
import com.example.backend.utils.DurationStringConverter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.DurationDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.DurationSerializer;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Duration;

@Entity(name = "detection_rules")
@Table(name = "detection_rules", schema = "tab_temp")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
//@EntityListeners(AuditLogListener.class)
public class DetectionRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rule_id", nullable = false)
    private Long ruleId;

    @Column(name = "rule_name", nullable = false)
    private String ruleName;

    @Column(name = "description")
    private String description;

    @Column(name = "start_offset")
    @Convert(converter = DurationStringConverter.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Duration startOffset;

    @Column(name = "end_offset")
    @Convert(converter = DurationStringConverter.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Duration endOffset;

    @Column(name = "execution_interval")
    @Convert(converter = DurationStringConverter.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Duration executionInterval;

    @Column(name = "min_mo_call_count")
    private Integer minMoCallCount;

    @Column(name = "max_mo_call_count")
    private Integer maxMoCallCount;

    @Column(name = "min_mo_total_duration")
    private Integer minMoTotalDuration;

    @Column(name = "max_mo_total_duration")
    private Integer maxMoTotalDuration;

    @Column(name = "min_mo_inter_call_count")
    private Integer minMoInterCallCount;

    @Column(name = "max_mo_inter_call_count")
    private Integer maxMoInterCallCount;

    @Column(name = "min_mo_inter_total_duration")
    private Integer minMoInterTotalDuration;

    @Column(name = "max_mo_inter_total_duration")
    private Integer maxMoInterTotalDuration;

    @Column(name = "min_mo_called_variation")
    private BigDecimal minMoCalledVariation;

    @Column(name = "max_mo_called_variation")
    private BigDecimal maxMoCalledVariation;

    @Column(name = "min_smsmo_call_count")
    private Integer minSmsMoCallCount;

    @Column(name = "max_smsmo_call_count")
    private Integer maxSmsMoCallCount;

    @Column(name = "min_smsmo_inter_call_count")
    private Integer minSmsMoInterCallCount;

    @Column(name = "max_smsmo_inter_call_count")
    private Integer maxSmsMoInterCallCount;

    @Column(name = "min_smsmo_called_variation")
    private BigDecimal minSmsMoCalledVariation;

    @Column(name = "max_smsmo_called_variation")
    private BigDecimal maxSmsMoCalledVariation;

    @Column(name = "min_mt_call_count")
    private Integer minMtCallCount;

    @Column(name = "max_mt_call_count")
    private Integer maxMtCallCount;

    @Column(name = "min_mt_total_duration")
    private Integer minMtTotalDuration;

    @Column(name = "max_mt_total_duration")
    private Integer maxMtTotalDuration;

    @Column(name = "min_mt_inter_call_count")
    private Integer minMtInterCallCount;

    @Column(name = "max_mt_inter_call_count")
    private Integer maxMtInterCallCount;

    @Column(name = "min_mt_inter_total_duration")
    private Integer minMtInterTotalDuration;

    @Column(name = "max_mt_inter_total_duration")
    private Integer maxMtInterTotalDuration;

    @Column(name = "min_mt_called_variation")
    private BigDecimal minMtCalledVariation;

    @Column(name = "max_mt_called_variation")
    private BigDecimal maxMtCalledVariation;

    @Column(name = "min_smsmt_call_count")
    private Integer minSmsMtCallCount;

    @Column(name = "max_smsmt_call_count")
    private Integer maxSmsMtCallCount;

    @Column(name = "min_smsmt_inter_call_count")
    private Integer minSmsMtInterCallCount;

    @Column(name = "max_smsmt_inter_call_count")
    private Integer maxSmsMtInterCallCount;

    @Column(name = "min_smsmt_called_variation")
    private BigDecimal minSmsMtCalledVariation;

    @Column(name = "max_smsmt_called_variation")
    private BigDecimal maxSmsMtCalledVariation;

    @Column(name = "min_distinct_locations")
    private Integer minDistinctLocations;

    @Column(name = "max_distinct_locations")
    private Integer maxDistinctLocations;

    @Column(name = "min_distinct_imeis")
    private Integer minDistinctImeis;

    @Column(name = "max_distinct_imeis")
    private Integer maxDistinctImeis;

    @Column(name = "min_distinct_imsis")
    private Integer minDistinctImsis;

    @Column(name = "max_distinct_imsis")
    private Integer maxDistinctImsis;
}
