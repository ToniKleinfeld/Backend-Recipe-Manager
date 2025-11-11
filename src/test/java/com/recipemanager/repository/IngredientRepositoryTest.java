package com.recipemanager.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.recipemanager.enums.Unit;
import com.recipemanager.model.Ingredient;
import com.recipemanager.model.Recipe;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class IngredientRepositoryTest {

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    private Recipe testRecipe;

    @BeforeEach
    void setUp() {
        ingredientRepository.deleteAll();
        recipeRepository.deleteAll();

        testRecipe = new Recipe("Pasta", "Lecker");
        testRecipe = recipeRepository.save(testRecipe);
    }

    // ========== Custom Query Tests ==========

    @Test
    void testFindByRecipeId_ShouldReturnAllIngredientsForRecipe() {
        // Arrange
        Ingredient ing1 = new Ingredient("Mehl", 200.0, Unit.G, testRecipe);
        Ingredient ing2 = new Ingredient("Eier", 3.0, Unit.GLAS, testRecipe);
        ingredientRepository.saveAll(List.of(ing1, ing2));

        List<Ingredient> result = ingredientRepository.findByRecipeId(testRecipe.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(i -> i.getTitle().equals("Mehl")));
        assertTrue(result.stream().anyMatch(i -> i.getTitle().equals("Eier")));
    }

    @Test
    void testFindByRecipeId_EmptyList() {

        List<Ingredient> result = ingredientRepository.findByRecipeId(testRecipe.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void testFindByRecipeId_OnlyReturnsForSpecificRecipe() {

        Recipe otherRecipe = new Recipe("Pizza", "Auch lecker");
        otherRecipe = recipeRepository.save(otherRecipe);

        Ingredient ing1 = new Ingredient("Mehl", 200.0, Unit.G, testRecipe);
        Ingredient ing2 = new Ingredient("Tomaten", 500.0, Unit.G, otherRecipe);
        ingredientRepository.saveAll(List.of(ing1, ing2));

        List<Ingredient> pastaIngredients = ingredientRepository.findByRecipeId(testRecipe.getId());
        List<Ingredient> pizzaIngredients = ingredientRepository.findByRecipeId(otherRecipe.getId());

        assertEquals(1, pastaIngredients.size());
        assertEquals(1, pizzaIngredients.size());
        assertEquals("Mehl", pastaIngredients.get(0).getTitle());
        assertEquals("Tomaten", pizzaIngredients.get(0).getTitle());
    }

    @Test
    void testFindByTitleAndRecipeId_ShouldReturnIngredient() {
        Ingredient ing = new Ingredient("Mehl", 200.0, Unit.G, testRecipe);
        ingredientRepository.save(ing);

        Ingredient result = ingredientRepository.findByTitleAndRecipeId("Mehl", testRecipe.getId());

        assertNotNull(result);
        assertEquals("Mehl", result.getTitle());
        assertEquals(200.0, result.getAmount());
    }

    @Test
    void testFindByTitleAndRecipeId_NotFound() {
        Ingredient result = ingredientRepository.findByTitleAndRecipeId("Nicht vorhanden", testRecipe.getId());

        assertNull(result);
    }
}
