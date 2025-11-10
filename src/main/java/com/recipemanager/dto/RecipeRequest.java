package com.recipemanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RecipeRequest {

    @NotBlank(message = "Title darf nicht leer sein")
    @Size(min = 3, max = 50, message = "Title muss zwischen 3 und 50 Zeichen sein")
    private String title;

    @Size(max = 5000, message = "Description darf max. 5000 Zeichen sein")
    private String description;

    // Getter/Setter
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
}
