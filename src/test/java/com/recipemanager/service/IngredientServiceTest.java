package com.recipemanager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.recipemanager.dto.IngredientRequest;
import com.recipemanager.enums.Unit;
import com.recipemanager.model.Ingredient;
import com.recipemanager.model.Recipe;
import com.recipemanager.repository.IngredientRepository;
import com.recipemanager.repository.RecipeRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class IngredientServiceTest {

    @Autowired
    private IngredientService ingredientService;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    private Recipe testRecipe;

    @BeforeEach
    void setUp() {
        ingredientRepository.deleteAll();
        recipeRepository.deleteAll();

        // Erstelle Test-Rezept
        testRecipe = new Recipe("Pasta Carbonara", "Italienische Pasta");
        testRecipe = recipeRepository.save(testRecipe);
    }

    // ========== GET Tests ==========

    @Test
    void testGetIngredientsByRecipeId_ShouldReturnEmptyList() {
        // Act
        List<Ingredient> ingredients = ingredientService.getIngredientsByRecipeId(testRecipe.getId());

        // Assert
        assertTrue(ingredients.isEmpty());
    }

    @Test
    void testGetIngredientsByRecipeId_ShouldReturnAllIngredients() {
        // Arrange
        Ingredient ing1 = new Ingredient("Mehl", 200.0, Unit.G, testRecipe);
        Ingredient ing2 = new Ingredient("Eier", 3.0, Unit.GLAS, testRecipe);
        Ingredient ing3 = new Ingredient("Speck", null, Unit.PRISE, testRecipe);
        ingredientRepository.saveAll(List.of(ing1, ing2, ing3));

        // Act
        List<Ingredient> ingredients = ingredientService.getIngredientsByRecipeId(testRecipe.getId());

        // Assert
        assertEquals(3, ingredients.size());
        assertTrue(ingredients.stream().anyMatch(i -> i.getTitle().equals("Mehl")));
        assertTrue(ingredients.stream().anyMatch(i -> i.getTitle().equals("Eier")));
        assertTrue(ingredients.stream().anyMatch(i -> i.getTitle().equals("Speck")));
    }

    @Test
    void testGetIngredientsByRecipeId_OnlyReturnsForSpecificRecipe() {
        // Arrange
        Recipe otherRecipe = new Recipe("Pizza", "Italienisch");
        otherRecipe = recipeRepository.save(otherRecipe);

        Ingredient ing1 = new Ingredient("Mehl", 200.0, Unit.G, testRecipe);
        Ingredient ing2 = new Ingredient("Tomaten", 500.0, Unit.G, otherRecipe);
        ingredientRepository.saveAll(List.of(ing1, ing2));

        // Act
        List<Ingredient> pastaIngredients = ingredientService.getIngredientsByRecipeId(testRecipe.getId());
        List<Ingredient> pizzaIngredients = ingredientService.getIngredientsByRecipeId(otherRecipe.getId());

        // Assert
        assertEquals(1, pastaIngredients.size());
        assertEquals(1, pizzaIngredients.size());
        assertEquals("Mehl", pastaIngredients.get(0).getTitle());
        assertEquals("Tomaten", pizzaIngredients.get(0).getTitle());
    }

    // ========== CREATE Tests ==========

    @Test
    void testCreateIngredient_ShouldSaveAndReturnIngredient() {
        // Arrange
        IngredientRequest request = new IngredientRequest();
        request.setTitle("Olivenöl");
        request.setAmount(50.0);
        request.setUnit(Unit.ML);

        // Act
        Ingredient saved = ingredientService.createIngredient(testRecipe.getId(), request);

        // Assert
        assertNotNull(saved.getId());
        assertEquals("Olivenöl", saved.getTitle());
        assertEquals(50.0, saved.getAmount());
        assertEquals(Unit.ML, saved.getUnit());
        assertEquals(testRecipe.getId(), saved.getRecipe().getId());
        assertTrue(ingredientRepository.existsById(saved.getId()));
    }

    @Test
    void testCreateIngredient_WithoutAmount_ShouldSucceed() {
        // Arrange
        IngredientRequest request = new IngredientRequest();
        request.setTitle("Salz");
        request.setAmount(null); // ← Optional
        request.setUnit(Unit.PRISE);

        // Act
        Ingredient saved = ingredientService.createIngredient(testRecipe.getId(), request);

        // Assert
        assertNotNull(saved.getId());
        assertEquals("Salz", saved.getTitle());
        assertNull(saved.getAmount());
        assertEquals(Unit.PRISE, saved.getUnit());
    }

    @Test
    void testCreateIngredient_RecipeNotFound() {
        // Arrange
        IngredientRequest request = new IngredientRequest();
        request.setTitle("Mehl");
        request.setAmount(200.0);
        request.setUnit(Unit.G);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            ingredientService.createIngredient(999L, request);
        });
    }

    // ========== UPDATE Tests ==========

    @Test
    void testUpdateIngredient_ShouldModifyIngredient() {
        // Arrange
        Ingredient original = new Ingredient("Mehl", 200.0, Unit.G, testRecipe);
        original = ingredientRepository.save(original);

        IngredientRequest request = new IngredientRequest();
        request.setTitle("Mehl Premium");
        request.setAmount(300.0);
        request.setUnit(Unit.G);

        // Act
        Ingredient updated = ingredientService.updateIngredient(original.getId(), request);

        // Assert
        assertEquals(original.getId(), updated.getId());
        assertEquals("Mehl Premium", updated.getTitle());
        assertEquals(300.0, updated.getAmount());
    }

    @Test
    void testUpdateIngredient_ChangeUnit() {
        // Arrange
        Ingredient original = new Ingredient("Mehl", 200.0, Unit.G, testRecipe);
        original = ingredientRepository.save(original);

        IngredientRequest request = new IngredientRequest();
        request.setTitle("Mehl");
        request.setAmount(0.5);
        request.setUnit(Unit.KG);

        // Act
        Ingredient updated = ingredientService.updateIngredient(original.getId(), request);

        // Assert
        assertEquals(0.5, updated.getAmount());
        assertEquals(Unit.KG, updated.getUnit());
    }

    @Test
    void testUpdateIngredient_SetAmountToNull() {
        // Arrange
        Ingredient original = new Ingredient("Mehl", 200.0, Unit.G, testRecipe);
        original = ingredientRepository.save(original);

        IngredientRequest request = new IngredientRequest();
        request.setTitle("Mehl");
        request.setAmount(null); // ← Auf null setzen
        request.setUnit(Unit.PRISE);

        // Act
        Ingredient updated = ingredientService.updateIngredient(original.getId(), request);

        // Assert
        assertNull(updated.getAmount());
        assertEquals(Unit.PRISE, updated.getUnit());
    }

    @Test
    void testUpdateIngredient_NotFound() {
        // Arrange
        IngredientRequest request = new IngredientRequest();
        request.setTitle("Mehl");
        request.setAmount(200.0);
        request.setUnit(Unit.G);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            ingredientService.updateIngredient(999L, request);
        });
    }

    // ========== DELETE Tests ==========

    @Test
    void testDeleteIngredient_ShouldRemoveFromDatabase() {
        // Arrange
        Ingredient ingredient = new Ingredient("Mehl", 200.0, Unit.G, testRecipe);
        ingredient = ingredientRepository.save(ingredient);
        Long id = ingredient.getId();

        // Act
        ingredientService.deleteIngredient(id);

        // Assert
        assertFalse(ingredientRepository.existsById(id));
    }

    @Test
    void testDeleteIngredient_ShouldNotDeleteRecipe() {
        // Arrange
        Ingredient ingredient = new Ingredient("Mehl", 200.0, Unit.G, testRecipe);
        ingredient = ingredientRepository.save(ingredient);

        // Act
        ingredientService.deleteIngredient(ingredient.getId());

        // Assert: Rezept sollte noch existieren
        assertTrue(recipeRepository.existsById(testRecipe.getId()));
    }

    @Test
    void testDeleteIngredient_NotFound() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            ingredientService.deleteIngredient(999L);
        });
    }

    @Test
    void testDeleteIngredient_MultipleIngredients_OnlyDeletesOne() {
        // Arrange
        Ingredient ing1 = new Ingredient("Mehl", 200.0, Unit.G, testRecipe);
        Ingredient ing2 = new Ingredient("Eier", 3.0, Unit.GLAS, testRecipe);
        ing1 = ingredientRepository.save(ing1);
        ing2 = ingredientRepository.save(ing2);

        // Act
        ingredientService.deleteIngredient(ing1.getId());

        // Assert
        assertFalse(ingredientRepository.existsById(ing1.getId()));
        assertTrue(ingredientRepository.existsById(ing2.getId()));
        assertEquals(1, ingredientRepository.findByRecipeId(testRecipe.getId()).size());
    }
}
