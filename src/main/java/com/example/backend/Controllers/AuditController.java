package com.example.backend.Controllers;

import com.example.backend.entities.dto.AuditResponse;
import com.example.backend.services.AuditLogListener;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
@Tag(name = "Audit Controller", description = "APIs for displaying all activity on dashboard")
public class AuditController {
    @Autowired
    private AuditLogListener auditLogListener;


    @Operation(summary = "Get all activity", description = "Retrieves aall dashboard activity.")
    @GetMapping("/between")
    public List<AuditResponse> getDecisionsBetweenDates(
            @RequestParam("start")
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime startDate,
            @RequestParam("end")
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime endDate) {

        return auditLogListener.getAuditList(startDate, endDate);
    }
}
