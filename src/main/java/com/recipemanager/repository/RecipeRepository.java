package com.recipemanager.repository;

import com.recipemanager.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    // Django: Recipe.objects.filter(title__contains="Pasta")
    // Spring: findByTitleContaining("Pasta")
}
