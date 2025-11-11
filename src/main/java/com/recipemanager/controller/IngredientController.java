package com.recipemanager.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recipemanager.dto.IngredientRequest;
import com.recipemanager.dto.IngredientResponse;
import com.recipemanager.model.Ingredient;
import com.recipemanager.service.IngredientService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/recipes/{recipeId}/ingredients")
public class IngredientController {

    @Autowired
    private IngredientService ingredientService;

    // ========== GET ==========

    /**
     * Alle Zutaten eines Rezepts abrufen
     * GET /api/recipes/5/ingredients
     * 
     * @param recipeId Die ID des Rezepts
     * @return Array von IngredientResponse
     */
    @GetMapping
    public ResponseEntity<List<IngredientResponse>> getIngredientsByRecipe(
            @PathVariable Long recipeId) {
        List<Ingredient> ingredients = ingredientService.getIngredientsByRecipeId(recipeId);

        List<IngredientResponse> responses = ingredients.stream()
                .map(ing -> new IngredientResponse(
                        ing.getId(),
                        ing.getTitle(),
                        ing.getAmount(),
                        ing.getUnit()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    // ========== CREATE ==========

    /**
     * Neue Zutat zu einem Rezept hinzufügen
     * POST /api/recipes/5/ingredients
     * 
     * @param recipeId Die ID des Rezepts
     * @param request  Die Zutat-Daten
     * @return Die neue Zutat mit Status 201 Created
     */
    @PostMapping
    public ResponseEntity<IngredientResponse> createIngredient(
            @PathVariable Long recipeId,
            @Valid @RequestBody IngredientRequest request) {
        try {
            Ingredient ingredient = ingredientService.createIngredient(recipeId, request);

            // ✅ Konvertiere zu DTO
            IngredientResponse response = new IngredientResponse(
                    ingredient.getId(),
                    ingredient.getTitle(),
                    ingredient.getAmount(),
                    ingredient.getUnit());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ========== PATCH ==========

    /**
     * Eine Zutat aktualisieren (PATCH - Partial Update)
     * PATCH /api/recipes/5/ingredients/1
     * 
     * @param recipeId     Die ID des Rezepts
     * @param ingredientId Die ID der Zutat
     * @param request      Die zu ändernden Daten
     * @return Die aktualisierte Zutat oder 404
     */
    @PatchMapping("/{ingredientId}")
    public ResponseEntity<IngredientResponse> updateIngredient(
            @PathVariable Long recipeId,
            @PathVariable Long ingredientId,
            @Valid @RequestBody IngredientRequest request) {
        try {
            Ingredient ingredient = ingredientService.updateIngredient(ingredientId, request);

            // ✅ Konvertiere zu DTO
            IngredientResponse response = new IngredientResponse(
                    ingredient.getId(),
                    ingredient.getTitle(),
                    ingredient.getAmount(),
                    ingredient.getUnit());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ========== DELETE ==========

    /**
     * Eine Zutat löschen
     * DELETE /api/recipes/5/ingredients/1
     * 
     * @param recipeId     Die ID des Rezepts (für Route)
     * @param ingredientId Die ID der Zutat
     * @return Status 204 No Content oder 404
     */
    @DeleteMapping("/{ingredientId}")
    public ResponseEntity<Void> deleteIngredient(
            @PathVariable Long recipeId,
            @PathVariable Long ingredientId) {
        try {
            ingredientService.deleteIngredient(ingredientId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}