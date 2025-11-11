package com.recipemanager.controller;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.recipemanager.dto.IngredientRequest;
import com.recipemanager.enums.Unit;
import com.recipemanager.model.Ingredient;
import com.recipemanager.model.Recipe;
import com.recipemanager.repository.IngredientRepository;
import com.recipemanager.repository.RecipeRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class IngredientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Recipe testRecipe;
    private Ingredient testIngredient;

    @BeforeEach
    void setUp() {
        ingredientRepository.deleteAll();
        recipeRepository.deleteAll();

        // Erstelle Test-Rezept
        testRecipe = new Recipe("Pasta Carbonara", "Italienische Pasta");
        testRecipe = recipeRepository.save(testRecipe);

        // Erstelle Test-Zutat
        testIngredient = new Ingredient("Mehl", 200.0, Unit.G, testRecipe);
        testIngredient = ingredientRepository.save(testIngredient);
    }

    // ========== GET Tests ==========

    @Test
    void testGetIngredientsByRecipe_ShouldReturnList() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/recipes/" + testRecipe.getId() + "/ingredients")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Mehl"))
                .andExpect(jsonPath("$[0].amount").value(200.0))
                .andExpect(jsonPath("$[0].unit").value("G"));
    }

    @Test
    void testGetIngredientsByRecipe_EmptyList() throws Exception {
        // Arrange: Neue Rezept ohne Zutaten
        Recipe emptyRecipe = new Recipe("Salat", "Grüner Salat");
        emptyRecipe = recipeRepository.save(emptyRecipe);

        // Act & Assert
        mockMvc.perform(get("/api/recipes/" + emptyRecipe.getId() + "/ingredients")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetIngredientsByRecipe_MultipleIngredients() throws Exception {
        // Arrange: Mehrere Zutaten hinzufügen
        Ingredient ing2 = new Ingredient("Eier", 3.0, Unit.GLAS, testRecipe);
        Ingredient ing3 = new Ingredient("Speck", null, Unit.PRISE, testRecipe);
        ingredientRepository.saveAll(java.util.List.of(ing2, ing3));

        // Act & Assert
        mockMvc.perform(get("/api/recipes/" + testRecipe.getId() + "/ingredients")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].title", hasItems("Mehl", "Eier", "Speck")));
    }

    // ========== CREATE Tests ==========

    @Test
    void testCreateIngredient_WithValidData() throws Exception {
        // Arrange
        IngredientRequest request = new IngredientRequest();
        request.setTitle("Olivenöl");
        request.setAmount(50.0);
        request.setUnit(Unit.ML);

        String json = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(post("/api/recipes/" + testRecipe.getId() + "/ingredients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Olivenöl"))
                .andExpect(jsonPath("$.amount").value(50.0))
                .andExpect(jsonPath("$.unit").value("ML"));

        // Verify: In Datenbank gespeichert
        assert ingredientRepository.count() == 2;
    }

    @Test
    void testCreateIngredient_WithoutAmount_ShouldSucceed() throws Exception {
        // Arrange: Amount ist null (optional)
        IngredientRequest request = new IngredientRequest();
        request.setTitle("Salz");
        request.setAmount(null);
        request.setUnit(Unit.PRISE);

        String json = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(post("/api/recipes/" + testRecipe.getId() + "/ingredients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Salz"))
                .andExpect(jsonPath("$.amount").doesNotExist());
    }

    @Test
    void testCreateIngredient_WithBlankTitle_ShouldFail() throws Exception {
        // Arrange
        IngredientRequest request = new IngredientRequest();
        request.setTitle(""); // ← Invalid!
        request.setAmount(100.0);
        request.setUnit(Unit.G);

        String json = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(post("/api/recipes/" + testRecipe.getId() + "/ingredients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateIngredient_WithoutUnit_ShouldFail() throws Exception {
        // Arrange
        IngredientRequest request = new IngredientRequest();
        request.setTitle("Mehl");
        request.setAmount(200.0);
        request.setUnit(null); // ← Invalid!

        String json = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(post("/api/recipes/" + testRecipe.getId() + "/ingredients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateIngredient_RecipeNotFound() throws Exception {
        // Arrange
        IngredientRequest request = new IngredientRequest();
        request.setTitle("Mehl");
        request.setAmount(200.0);
        request.setUnit(Unit.G);

        String json = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(post("/api/recipes/999/ingredients") // ← Rezept existiert nicht
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound());
    }

    // ========== UPDATE Tests (geändert von PUT zu PATCH) ==========

    @Test
    void testUpdateIngredient_WithValidData() throws Exception {
        // Arrange
        IngredientRequest request = new IngredientRequest();
        request.setTitle("Mehl Premium");
        request.setAmount(300.0);
        request.setUnit(Unit.G);

        String json = objectMapper.writeValueAsString(request);

        // Act & Assert (patch statt put!)
        mockMvc.perform(patch("/api/recipes/" + testRecipe.getId() + "/ingredients/" + testIngredient.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Mehl Premium"))
                .andExpect(jsonPath("$.amount").value(300.0));
    }

    @Test
    void testUpdateIngredient_ChangeUnit() throws Exception {
        // Arrange
        IngredientRequest request = new IngredientRequest();
        request.setTitle("Mehl");
        request.setAmount(0.5);
        request.setUnit(Unit.KG);

        String json = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(patch("/api/recipes/" + testRecipe.getId() + "/ingredients/" + testIngredient.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(0.5))
                .andExpect(jsonPath("$.unit").value("KG"));
    }

    @Test
    void testUpdateIngredient_NotFound() throws Exception {
        // Arrange
        IngredientRequest request = new IngredientRequest();
        request.setTitle("Mehl");
        request.setAmount(200.0);
        request.setUnit(Unit.G);

        String json = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(patch("/api/recipes/" + testRecipe.getId() + "/ingredients/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound());
    }

    // ========== DELETE Tests ==========

    @Test
    void testDeleteIngredient_Success() throws Exception {
        // Act
        mockMvc.perform(delete("/api/recipes/" + testRecipe.getId() + "/ingredients/" + testIngredient.getId()))
                .andExpect(status().isNoContent());

        // Assert: In Datenbank gelöscht
        assert !ingredientRepository.existsById(testIngredient.getId());
    }

    @Test
    void testDeleteIngredient_NotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/recipes/" + testRecipe.getId() + "/ingredients/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteIngredient_ShouldNotDeleteRecipe() throws Exception {
        // Act
        mockMvc.perform(delete("/api/recipes/" + testRecipe.getId() + "/ingredients/" + testIngredient.getId()))
                .andExpect(status().isNoContent());

        // Assert: Rezept sollte noch existieren!
        assert recipeRepository.existsById(testRecipe.getId());
    }
}
