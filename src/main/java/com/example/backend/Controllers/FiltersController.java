package com.example.backend.Controllers;

import com.example.backend.entities.CategoriesFraude;
import com.example.backend.entities.FiltresFraude;
import com.example.backend.services.FilterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/filters")
@Tag(name = "Filters API", description = "Endpoints Filters Fraude")
public class FiltersController {
    @Autowired
    private FilterService filtersFraudeService;

    @Operation(summary = "Get all Filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = FiltresFraude.class)))
    })
    @GetMapping
    public ResponseEntity<List<FiltresFraude>> getAll() {
        List<FiltresFraude> list = filtersFraudeService.getAllFilters();
        return ResponseEntity.ok(list);
    }
}
