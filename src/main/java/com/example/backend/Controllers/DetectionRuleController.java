package com.example.backend.Controllers;

import com.example.backend.entities.DetectionRule;
import com.example.backend.services.DetectionRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/detection-rules")
@Tag(name = "Detection Rule Controller", description = "APIs for managing detection rules")
public class DetectionRuleController {

    @Autowired
    private DetectionRuleService detectionRuleService;

    @Operation(summary = "Add a new detection rule")
    @PostMapping
    public ResponseEntity<DetectionRule> addDetectionRule(@RequestBody DetectionRule detectionRule) {
        return ResponseEntity.ok(detectionRuleService.addDetectionRule(detectionRule));
    }

    @Operation(summary = "Edit an existing detection rule")
    @PutMapping
    public ResponseEntity<DetectionRule> editDetectionRule(@RequestBody DetectionRule detectionRule) {
        return ResponseEntity.ok(detectionRuleService.editDetectionRule(detectionRule));
    }

    @Operation(summary = "Delete a detection rule by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDetectionRule(@PathVariable("id") Long idRule) {
        detectionRuleService.deleteDetectionRule(idRule);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get a detection rule by ID")
    @GetMapping("/{id}")
    public ResponseEntity<DetectionRule> getDetectionRuleById(@PathVariable("id") Long idRule) {
        return ResponseEntity.ok(detectionRuleService.getDetectionRuleById(idRule));
    }

    @Operation(summary = "Get all detection rules")
    @GetMapping
    public ResponseEntity<List<DetectionRule>> getAllDetectionRules() {
        return ResponseEntity.ok(detectionRuleService.getAllDetectionRules());
    }
}
