package com.smartpantry.manager;

import java.util.List;

public class Recipe {
    private long id;
    private String name;
    private String steps;
    private List<RecipeIngredient> ingredients;

    public Recipe(long id, String name, String steps, List<RecipeIngredient> ingredients) {
        this.id = id;
        this.name = name;
        this.steps = steps;
        this.ingredients = ingredients;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSteps() {
        return steps;
    }

    public List<RecipeIngredient> getIngredients() {
        return ingredients;
    }
}