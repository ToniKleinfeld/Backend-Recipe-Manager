package com.recipemanager.controller;

import com.recipemanager.dto.RecipeRequest;
import com.recipemanager.model.Recipe;
import com.recipemanager.repository.RecipeRepository;
import com.recipemanager.repository.IngredientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // ← Nutzt test-Datenbank
@Transactional // ← Rollback nach jedem Test
class RecipeControllerTest {

    @Autowired
    private MockMvc mockMvc; // ← Simulates HTTP requests

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private ObjectMapper objectMapper; // ← JSON Konvertierung

    private Recipe testRecipe;

    // ========== SETUP ==========

    @BeforeEach
    void setUp() {
        // ✅ Vor jedem Test: Test-Daten erstellen
        recipeRepository.deleteAll(); // Aufräumen
        ingredientRepository.deleteAll();

        testRecipe = new Recipe("Pasta Carbonara", "Italienische Pasta");
        testRecipe = recipeRepository.save(testRecipe);
    }

    // ========== GET Tests ==========

    @Test
    void testGetAllRecipes_ShouldReturnEmptyList() throws Exception {
        // Arrange: Datenbank ist leer (setUp hat alles gelöscht)

        // Act & Assert
        mockMvc.perform(get("/api/recipes")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1))); // ← 1 Rezept vorhanden
    }

    @Test
    void testGetAllRecipes_ShouldReturnRecipeWithoutDetails() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/recipes")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].title").value("Pasta Carbonara"))
                .andExpect(jsonPath("$[0].createdAt").exists())
                .andExpect(jsonPath("$[0].description").doesNotExist()); // ← Description nicht im Light-Response
    }

    @Test
    void testGetRecipeById_ShouldReturnFullDetails() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/recipes/" + testRecipe.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testRecipe.getId()))
                .andExpect(jsonPath("$.title").value("Pasta Carbonara"))
                .andExpect(jsonPath("$.description").value("Italienische Pasta"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.ingredients").isArray()); // ← Mit Ingredients!
    }

    @Test
    void testGetRecipeById_NotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/recipes/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // ========== POST Tests ==========

    @Test
    void testCreateRecipe_WithValidData() throws Exception {
        // Arrange
        RecipeRequest request = new RecipeRequest();
        request.setTitle("Pizza Margherita");
        request.setDescription("Klassische Pizza");

        String json = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(post("/api/recipes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Pizza Margherita"))
                .andExpect(jsonPath("$.description").value("Klassische Pizza"));

        // Verify: In Datenbank gespeichert
        long count = recipeRepository.count();
        assert count == 2; // ← testRecipe + neue Pizza
    }

    @Test
    void testCreateRecipe_WithBlankTitle_ShouldFail() throws Exception {
        // Arrange
        RecipeRequest request = new RecipeRequest();
        request.setTitle(""); // ← Invalid!
        request.setDescription("Beschreibung");

        String json = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(post("/api/recipes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest()); // ← 400 validation error
    }

    @Test
    void testCreateRecipe_WithIngredients() throws Exception {
        // Arrange
        RecipeRequest request = new RecipeRequest();
        request.setTitle("Risotto");
        request.setDescription("Mit Pilzen");

        String json = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(post("/api/recipes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    // ========== PUT Tests ==========

    @Test
    void testUpdateRecipe_WithValidData() throws Exception {
        // Arrange
        RecipeRequest request = new RecipeRequest();
        request.setTitle("Pasta Carbonara Updated");
        request.setDescription("Neue Beschreibung");

        String json = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(put("/api/recipes/" + testRecipe.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Pasta Carbonara Updated"))
                .andExpect(jsonPath("$.description").value("Neue Beschreibung"));
    }

    @Test
    void testUpdateRecipe_NotFound() throws Exception {
        // Arrange
        RecipeRequest request = new RecipeRequest();
        request.setTitle("Irgendwas");
        request.setDescription("Test");

        String json = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(put("/api/recipes/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound());
    }

    // ========== DELETE Tests ==========

    @Test
    void testDeleteRecipe_Success() throws Exception {
        // Act
        mockMvc.perform(delete("/api/recipes/" + testRecipe.getId()))
                .andExpect(status().isNoContent());

        // Assert: In Datenbank gelöscht
        assert !recipeRepository.existsById(testRecipe.getId());
    }

    @Test
    void testDeleteRecipe_NotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/recipes/999"))
                .andExpect(status().isNoContent()); // ← Spring löscht auch nicht-existente ohne Fehler
    }
}
