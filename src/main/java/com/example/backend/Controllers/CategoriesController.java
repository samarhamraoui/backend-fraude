package com.example.backend.Controllers;

import com.example.backend.entities.CategoriesFraude;
import com.example.backend.services.CategoriesFraudeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categories Fraude API", description = "Endpoints for managing CategoriesFraude")
public class CategoriesController {
    @Autowired
    private CategoriesFraudeService categoriesFraudeService;

    @Operation(summary = "Get all categories")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = CategoriesFraude.class)))
    })
    @GetMapping
    public ResponseEntity<List<CategoriesFraude>> getAll() {
        List<CategoriesFraude> list = categoriesFraudeService.getCategories();
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Get category by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = CategoriesFraude.class))),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoriesFraude> getOne(
            @Parameter(description="ID of the category")
            @PathVariable Integer id) {
        CategoriesFraude cat = categoriesFraudeService.getCategory(id);
        return ResponseEntity.ok(cat);
    }

    @Operation(summary = "Create a new category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Category created",
                    content = @Content(schema = @Schema(implementation = CategoriesFraude.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<CategoriesFraude> create(@RequestBody CategoriesFraude category) {
        CategoriesFraude created = categoriesFraudeService.addCategory(category);
        return ResponseEntity.status(201).body(created);
    }

    @Operation(summary = "Update an existing category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = CategoriesFraude.class))),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CategoriesFraude> update(
            @Parameter(description="ID of the category")
            @PathVariable Integer id,
            @RequestBody CategoriesFraude category) {

        CategoriesFraude updated = categoriesFraudeService.updateCategory(id, category);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete a category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No content (deleted)"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description="ID of the category")
            @PathVariable Integer id) {

        categoriesFraudeService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
