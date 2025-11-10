package com.recipemanager.enums;

public enum Unit {
    G("Gramm"),
    ML("Milliliter"),
    KG("Kilogramm"),
    L("Liter"),
    TL("Teelöffel"),
    EL("Esslöffel"),
    PRISE("Prise"),
    MESSERSPITZE("Messerspitze"),
    TASSE("Tasse"),
    GLAS("Glas");

    private final String displayName;

    Unit(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
