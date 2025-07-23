package com.example.backend.Controllers;

import com.example.backend.entities.CategoriesFraude;
import com.example.backend.entities.SubModuleReport;
import com.example.backend.entities.dto.SubModuleReportDTO;
import com.example.backend.services.ReportsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports Controller", description = "APIs for Reports and managing reports")
public class ReportsController {
    @Autowired
    private ReportsService reportsService;

    @Operation(summary = "Get all submodule reports", description = "Retrieves all reorts linked to a submodule.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = CategoriesFraude.class))),
            @ApiResponse(responseCode = "404", description = "No Reports found")
    })
    @GetMapping("/submodules/{id}")
    public ResponseEntity<List<SubModuleReportDTO>> getAllReportsBySubModuleId(
            @Parameter(description="Sub Module ID")
            @PathVariable Long id) {
        List<SubModuleReportDTO> result = reportsService.getAllReportsBySubModuleId(id);
        return ResponseEntity.ok(result);
    }
}
