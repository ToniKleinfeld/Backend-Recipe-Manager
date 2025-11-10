package com.recipemanager.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recipemanager.dto.IngredientRequest;
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
     * @return Liste aller Zutaten mit Status 200
     */
    @GetMapping
    public ResponseEntity<List<Ingredient>> getIngredientsByRecipe(
            @PathVariable Long recipeId) {
        List<Ingredient> ingredients = ingredientService.getIngredientsByRecipeId(recipeId);
        return ResponseEntity.ok(ingredients);
    }

    // ========== CREATE ==========

    /**
     * Neue Zutat zu einem Rezept hinzufügen
     * POST /api/recipes/5/ingredients
     * Body: {"title": "Mehl", "amount": 200.0, "unit": "G"}
     * 
     * @param recipeId Die ID des Rezepts
     * @param request  Die Zutat-Daten
     * @return Die neue Zutat mit Status 201 Created
     */
    @PostMapping
    public ResponseEntity<Ingredient> createIngredient(
            @PathVariable Long recipeId,
            @Valid @RequestBody IngredientRequest request) {
        try {
            Ingredient ingredient = ingredientService.createIngredient(recipeId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ingredient);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ========== UPDATE ==========

    /**
     * Eine Zutat aktualisieren
     * PUT /api/recipes/5/ingredients/1
     * Body: {"title": "Mehl Premium", "amount": 300.0, "unit": "G"}
     * 
     * @param recipeId     Die ID des Rezepts (für Route, aber nicht verwendet)
     * @param ingredientId Die ID der Zutat
     * @param request      Die neuen Daten
     * @return Die aktualisierte Zutat oder 404
     */
    @PutMapping("/{ingredientId}")
    public ResponseEntity<Ingredient> updateIngredient(
            @PathVariable Long recipeId,
            @PathVariable Long ingredientId,
            @Valid @RequestBody IngredientRequest request) {
        try {
            Ingredient ingredient = ingredientService.updateIngredient(ingredientId, request);
            return ResponseEntity.ok(ingredient);
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