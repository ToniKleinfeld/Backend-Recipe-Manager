package com.recipemanager.dto;

import java.time.LocalDateTime;
import java.util.List;

public class RecipeDetailResponse {

    private Long id;
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private List<IngredientResponse> ingredients; // ← Nested DTOs!

    public RecipeDetailResponse(Long id, String title, String description,
            LocalDateTime createdAt, List<IngredientResponse> ingredients) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
        this.ingredients = ingredients;
    }

    // Getter/Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<IngredientResponse> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<IngredientResponse> ingredients) {
        this.ingredients = ingredients;
    }
}