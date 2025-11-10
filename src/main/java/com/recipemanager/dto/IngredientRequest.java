package com.recipemanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import com.recipemanager.enums.Unit;

public class IngredientRequest {
    @NotBlank(message = "Zutat-Name ist erforderlich")
    private String title;

    @Positive(message = "Menge muss positiv sein")
    private Double amount; 

    @NotNull(message = "Einheit ist erforderlich")
    private Unit unit;

    // Getter/Setter
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }
}
