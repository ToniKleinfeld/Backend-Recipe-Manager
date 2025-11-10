package com.recipemanager.repository;

import com.recipemanager.model.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    List<Ingredient> findByRecipeId(Long recipeId);

    Ingredient findByTitleAndRecipeId(String title, Long recipeId);
}
