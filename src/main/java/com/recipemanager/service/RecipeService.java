package com.recipemanager.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.recipemanager.dto.IngredientResponse;
import com.recipemanager.dto.RecipeDetailResponse;
import com.recipemanager.dto.RecipeRequest;
import com.recipemanager.dto.RecipeResponse;
import com.recipemanager.model.Ingredient;
import com.recipemanager.model.Recipe;
import com.recipemanager.repository.IngredientRepository;
import com.recipemanager.repository.RecipeRepository;

@Service
public class RecipeService {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    // ========== GET ==========

    /**
     * Alle Rezepte (id, title, createdAt)
     */
    public List<RecipeResponse> getAllRecipes() {
        return recipeRepository.findAll()
                .stream()
                .map(recipe -> new RecipeResponse(
                        recipe.getId(),
                        recipe.getTitle(),
                        recipe.getCreatedAt()))
                .collect(Collectors.toList());
    }

    /**
     * Ein Rezept mit allen Details + Zutaten
     */
    @SuppressWarnings("null")
    public Optional<RecipeDetailResponse> getRecipeById(Long id) {
        return recipeRepository.findById(id)
                .map(recipe -> {
                    List<IngredientResponse> ingredients = recipe.getIngredients()
                            .stream()
                            .map(ing -> new IngredientResponse(
                                    ing.getId(),
                                    ing.getTitle(),
                                    ing.getAmount(),
                                    ing.getUnit()))
                            .collect(Collectors.toList());

                    return new RecipeDetailResponse(
                            recipe.getId(),
                            recipe.getTitle(),
                            recipe.getDescription(),
                            recipe.getCreatedAt(),
                            ingredients);
                });
    }

    // ========== CREATE ==========

    /**
     * Neues Rezept mit Zutaten erstellen
     */
    @SuppressWarnings("null")
    public Recipe createRecipe(RecipeRequest request) {
        Recipe recipe = new Recipe(request.getTitle(), request.getDescription());
        Recipe savedRecipe = recipeRepository.save(recipe);

        if (request.getIngredients() != null && !request.getIngredients().isEmpty()) {
            List<Ingredient> ingredients = request.getIngredients()
                    .stream()
                    .map(ingRequest -> new Ingredient(
                            ingRequest.getTitle(),
                            ingRequest.getAmount(),
                            ingRequest.getUnit(),
                            savedRecipe))
                    .collect(Collectors.toList());

            ingredientRepository.saveAll(ingredients);
            savedRecipe.setIngredients(ingredients);
        }

        return savedRecipe;
    }

    // ========== UPDATE ==========

    /**
     * Rezept mit Zutaten aktualisieren
     */
    public Recipe updateRecipe(Long id, RecipeRequest request) {
        @SuppressWarnings("null")
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Rezept mit ID " + id + " nicht gefunden"));

        recipe.setTitle(request.getTitle());
        recipe.setDescription(request.getDescription());

        if (request.getIngredients() != null) {
            recipe.getIngredients().clear();

            List<Ingredient> newIngredients = request.getIngredients()
                    .stream()
                    .map(ingRequest -> new Ingredient(
                            ingRequest.getTitle(),
                            ingRequest.getAmount(),
                            ingRequest.getUnit(),
                            recipe))
                    .collect(Collectors.toList());

            recipe.setIngredients(newIngredients);
        }

        return recipeRepository.save(recipe);
    }

    // ========== DELETE ==========

    @SuppressWarnings("null")
    public void deleteRecipe(Long id) {
        recipeRepository.deleteById(id);
    }
}
