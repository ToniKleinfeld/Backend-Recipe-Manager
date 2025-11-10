package com.recipemanager.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.recipemanager.dto.IngredientRequest;
import com.recipemanager.model.Ingredient;
import com.recipemanager.model.Recipe;
import com.recipemanager.repository.IngredientRepository;
import com.recipemanager.repository.RecipeRepository;

@Service
public class IngredientService {

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    // ========== GET ==========

    /**
     * Alle Zutaten eines Rezepts finden
     * 
     * @param recipeId Die ID des Rezepts
     * @return Liste aller Zutaten
     */
    public List<Ingredient> getIngredientsByRecipeId(Long recipeId) {
        return ingredientRepository.findByRecipeId(recipeId);
    }

    // ========== CREATE ==========

    /**
     * Neue Zutat zu einem Rezept hinzufügen
     * 
     * @param recipeId Die ID des Rezepts
     * @param request  IngredientRequest mit Daten
     * @return Die gespeicherte Zutat
     * @throws IllegalArgumentException wenn Rezept nicht existiert
     */
    public Ingredient createIngredient(Long recipeId, IngredientRequest request) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Rezept mit ID " + recipeId + " nicht gefunden"));

        Ingredient ingredient = new Ingredient(
                request.getTitle(),
                request.getAmount(),
                request.getUnit(),
                recipe);

        return ingredientRepository.save(ingredient);
    }

    // ========== UPDATE ==========

    /**
     * Eine Zutat aktualisieren
     * 
     * @param id      Die ID der Zutat
     * @param request Die neuen Daten
     * @return Die aktualisierte Zutat
     * @throws IllegalArgumentException wenn Zutat nicht existiert
     */
    public Ingredient updateIngredient(Long id, IngredientRequest request) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Zutat mit ID " + id + " nicht gefunden"));

        ingredient.setTitle(request.getTitle());
        ingredient.setAmount(request.getAmount());
        ingredient.setUnit(request.getUnit());

        return ingredientRepository.save(ingredient);
    }

    // ========== DELETE ==========

    /**
     * Eine Zutat löschen
     * 
     * @param id Die ID der Zutat
     * @throws IllegalArgumentException wenn Zutat nicht existiert
     */
    public void deleteIngredient(Long id) {
        if (!ingredientRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Zutat mit ID " + id + " nicht gefunden");
        }

        ingredientRepository.deleteById(id);
    }

}
