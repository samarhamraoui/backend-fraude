package com.example.backend.Controllers;

import com.example.backend.entities.DecisionFraude;
import com.example.backend.services.DecisionFraudeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/decisions")
@Tag(name = "DecisionFraude Controller", description = "APIs for managing fraud decisions")
public class DecisionFraudeController {
    @Autowired
    private DecisionFraudeService decisionFraudeService;

    @Operation(summary = "Get all fraud decisions", description = "Fetch all records from the decision_fraude table")
    @GetMapping("/all")
    public List<DecisionFraude> getAllDecisions() {
        return decisionFraudeService.getAllDecisions();
    }


    @Operation(
            summary = "Get fraud decisions between dates",
            description = "Fetch all decisions made between start and end dates"
    )
    @GetMapping("/between")
    public List<DecisionFraude> getDecisionsBetweenDates(
            @RequestParam("start")
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime startDate,
            @RequestParam("end")
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime endDate) {
        Timestamp startTs = Timestamp.valueOf(startDate);
        Timestamp endTs = Timestamp.valueOf(endDate);

        return decisionFraudeService.getDecisionsBetweenDates(startTs, endTs);
    }


    @Operation(
            summary = "Update a fraud decision",
            description = "Updates an existing fraud decision and logs the modifying user"
    )
    @PutMapping("/{id}")
    public ResponseEntity<DecisionFraude> updateDecision(
            @PathVariable Integer id,
            @RequestBody DecisionFraude decisionFraude,
            HttpServletRequest request) {
        DecisionFraude updatedDecision = decisionFraudeService.updateDecision(id, decisionFraude, request);
        return ResponseEntity.ok(updatedDecision);
    }

}
