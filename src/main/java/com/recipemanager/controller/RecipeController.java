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

import com.recipemanager.dto.RecipeRequest;
import com.recipemanager.dto.RecipeResponse;
import com.recipemanager.dto.RecipeDetailResponse;
import com.recipemanager.model.Recipe;
import com.recipemanager.service.RecipeService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    @Autowired
    private RecipeService recipeService;

    // GET /api/recipes
    @GetMapping
    public ResponseEntity<List<RecipeResponse>> getAllRecipes() {
        List<RecipeResponse> recipes = recipeService.getAllRecipes();
        return ResponseEntity.ok(recipes);
    }

    // GET /api/recipes/{id}
    @GetMapping("/{id}")
    public ResponseEntity<RecipeDetailResponse> getRecipeById(@PathVariable Long id) {
        return recipeService.getRecipeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/recipes
    @PostMapping
    public ResponseEntity<Recipe> createRecipe(@Valid @RequestBody RecipeRequest request) {
        Recipe recipe = recipeService.createRecipe(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(recipe);
    }

    // PUT /api/recipes/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Recipe> updateRecipe(
            @PathVariable Long id,
            @Valid @RequestBody RecipeRequest request) {
        try {
            Recipe recipe = recipeService.updateRecipe(id, request);
            return ResponseEntity.ok(recipe);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /api/recipes/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable Long id) {
        recipeService.deleteRecipe(id);
        return ResponseEntity.noContent().build();
    }
}
