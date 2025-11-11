package com.recipemanager.service;

import com.recipemanager.dto.RecipeRequest;
import com.recipemanager.dto.RecipeDetailResponse;
import com.recipemanager.model.Recipe;
import com.recipemanager.repository.RecipeRepository;
import com.recipemanager.repository.IngredientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RecipeServiceTest {

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @BeforeEach
    void setUp() {
        recipeRepository.deleteAll();
        ingredientRepository.deleteAll();
    }

    // ========== GET Tests ==========

    @Test
    void testGetAllRecipes_ShouldReturnEmptyList() {
        // Act
        var recipes = recipeService.getAllRecipes();

        // Assert
        assertTrue(recipes.isEmpty());
    }

    @Test
    void testGetAllRecipes_ShouldReturnMultipleRecipes() {
        // Arrange
        recipeRepository.save(new Recipe("Pasta", "Desc1"));
        recipeRepository.save(new Recipe("Pizza", "Desc2"));

        // Act
        var recipes = recipeService.getAllRecipes();

        // Assert
        assertEquals(2, recipes.size());
        assertTrue(recipes.stream().anyMatch(r -> r.getTitle().equals("Pasta")));
        assertTrue(recipes.stream().anyMatch(r -> r.getTitle().equals("Pizza")));
    }

    @Test
    void testGetRecipeById_ShouldReturnRecipeWithDetails() {
        // Arrange
        Recipe saved = recipeRepository.save(new Recipe("Risotto", "Mit Champignons"));

        // Act
        // ✅ Jetzt Optional<RecipeDetailResponse> statt Optional<Recipe>
        Optional<RecipeDetailResponse> result = recipeService.getRecipeById(saved.getId());

        // Assert
        assertTrue(result.isPresent());
        RecipeDetailResponse detail = result.get();
        assertEquals("Risotto", detail.getTitle());
        assertEquals("Mit Champignons", detail.getDescription());
        assertEquals(saved.getId(), detail.getId());
        assertNotNull(detail.getCreatedAt());
        assertNotNull(detail.getIngredients()); // ← DTO hat Ingredients!
    }

    @Test
    void testGetRecipeById_NotFound() {
        // Act
        // ✅ Jetzt Optional<RecipeDetailResponse> statt Optional<Recipe>
        Optional<RecipeDetailResponse> result = recipeService.getRecipeById(999L);

        // Assert
        assertTrue(result.isEmpty());
    }

    // ========== CREATE Tests ==========

    @Test
    void testCreateRecipe_ShouldSaveAndReturnRecipe() {
        // Arrange
        RecipeRequest request = new RecipeRequest();
        request.setTitle("Lasagne");
        request.setDescription("Italienisch");

        // Act
        Recipe saved = recipeService.createRecipe(request);

        // Assert
        assertNotNull(saved.getId());
        assertEquals("Lasagne", saved.getTitle());
        assertTrue(recipeRepository.existsById(saved.getId()));
    }

    // ========== UPDATE Tests ==========

    @Test
    void testUpdateRecipe_ShouldModifyRecipe() {
        // Arrange
        Recipe original = recipeRepository.save(new Recipe("Spaghetti", "Bolognese"));

        RecipeRequest request = new RecipeRequest();
        request.setTitle("Spaghetti Aglio e Olio");
        request.setDescription("Neue Beschreibung");

        // Act
        Recipe updated = recipeService.updateRecipe(original.getId(), request);

        // Assert
        assertEquals("Spaghetti Aglio e Olio", updated.getTitle());
        assertEquals("Neue Beschreibung", updated.getDescription());
    }

    @Test
    void testUpdateRecipe_NotFound() {
        // Arrange
        RecipeRequest request = new RecipeRequest();
        request.setTitle("Test");
        request.setDescription("Test");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            recipeService.updateRecipe(999L, request);
        });
    }

    // ========== DELETE Tests ==========

    @Test
    void testDeleteRecipe_ShouldRemoveFromDatabase() {
        // Arrange
        Recipe saved = recipeRepository.save(new Recipe("Raclette", "Schweizer Käse"));
        Long id = saved.getId();

        // Act
        recipeService.deleteRecipe(id);

        // Assert
        assertFalse(recipeRepository.existsById(id));
    }
}