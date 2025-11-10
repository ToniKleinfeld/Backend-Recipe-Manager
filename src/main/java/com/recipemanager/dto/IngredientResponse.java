package com.recipemanager.dto;

import com.recipemanager.enums.Unit;

public class IngredientResponse {

    private Long id;
    private String title;
    private Double amount;
    private Unit unit;

    public IngredientResponse(Long id, String title, Double amount, Unit unit) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.unit = unit;
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
