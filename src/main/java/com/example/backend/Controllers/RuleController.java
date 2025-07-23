package com.example.backend.Controllers;

import com.example.backend.entities.FiltresReglesFraude;
import com.example.backend.entities.ParametresReglesFraude;
import com.example.backend.entities.ReglesFraude;
import com.example.backend.entities.dto.FilterDto;
import com.example.backend.entities.dto.ParameterDto;
import com.example.backend.entities.dto.RuleDTO;
import com.example.backend.entities.dto.UserDto;
import com.example.backend.services.RuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rules")
@Tag(name = "Controller for rules management", description = "API for rules management.")
public class RuleController {

    @Autowired
    private RuleService ruleService;

    @Operation(
            summary = "Get All Rules",
            description = "Get All Rules with it's params and filters."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rules Fetched successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    @GetMapping()
    public ResponseEntity<?> getAllRules() {
        try {
            List<ReglesFraude> reglesFraudeList = ruleService.getAllRules();
            return ResponseEntity.ok(reglesFraudeList);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
            summary = "Create a new rule",
            description = "Adds a new ReglesFraude with associated parameters and filters. Must reference existing categories/flows in DB."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Rule created successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request or validation error")
    })
    @PostMapping
    public ResponseEntity<?> createRule(@RequestBody RuleDTO dto) {
        try {
            ReglesFraude saved = ruleService.createRule(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
            summary = "Edit an existing rule",
            description = "Updates an existing ReglesFraude record by ID, replacing parameters/filters with new ones."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rule updated successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request or rule not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> editRule(@PathVariable Integer id, @RequestBody RuleDTO dto) {
        try {
            ReglesFraude updated = ruleService.editRule(id, dto,true);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @Operation(
            summary = "Edit an existing rule without affecting parameters and Filters",
            description = "Updates an existing ReglesFraude record by ID, without replacing parameters/filters with new ones."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rule updated successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request or rule not found")
    })
    @PutMapping("/only/{id}")
    public ResponseEntity<?> editRuleOnly(@PathVariable Integer id, @RequestBody RuleDTO dto) {
        try {
            ReglesFraude updated = ruleService.editRule(id, dto,false);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @Operation(summary = "Delete rule", description = "Deletes a rule by their unique ID.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Rule deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Rule not found"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Integer id) {
        ruleService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }


    @Operation(summary = "Delete Parameter", description = "Deletes a Parameter.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Parameter deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Parameter not found"
            )
    })
    @DeleteMapping("/{ruleId}/parameters/{paramId}")
    public ResponseEntity<Void> deleteParameter(@PathVariable Integer ruleId,@PathVariable Long paramId) {
        ruleService.deleteRuleParameter(ruleId,paramId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete Filter", description = "Deletes a Filter.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Filter deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Filter not found"
            )
    })
    @DeleteMapping("/{ruleId}/filters/{filterId}")
    public ResponseEntity<Void> deleteFilter(@PathVariable Integer ruleId,@PathVariable Integer filterId) {
        ruleService.deleteRuleFilter(ruleId,filterId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update rule parameter", description = "Update rule parameter.")
    @PutMapping("/parameter")
    public ResponseEntity<ParametresReglesFraude> editParameter( @RequestBody ParameterDto dto) {
        return ResponseEntity.ok(ruleService.editRuleParameter(dto));
    }

    @Operation(summary = "Update rule filter", description = "Update rule filter.")
    @PutMapping("filter")
    public ResponseEntity<FiltresReglesFraude> editFilter(@RequestBody FilterDto dto) {
        return ResponseEntity.ok(ruleService.editRuleFilter(dto));
    }


}
