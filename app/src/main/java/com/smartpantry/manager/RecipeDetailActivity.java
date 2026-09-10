package com.smartpantry.manager;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class RecipeDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        long recipeId = getIntent().getLongExtra("recipe_id", -1);

        TextView tvName = findViewById(R.id.tvDetailRecipeName);
        TextView tvIngredients = findViewById(R.id.tvDetailIngredients);
        TextView tvSteps = findViewById(R.id.tvDetailSteps);

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        List<Recipe> allRecipes = dbHelper.getAllRecipesWithIngredients();

        Recipe found = null;
        for (Recipe recipe : allRecipes) {
            if (recipe.getId() == recipeId) {
                found = recipe;
                break;
            }
        }

        if (found != null) {
            tvName.setText(found.getName());
            tvSteps.setText(found.getSteps());

            StringBuilder ingredientsText = new StringBuilder();
            for (RecipeIngredient ingredient : found.getIngredients()) {
                ingredientsText.append("• ")
                        .append(ingredient.getName())
                        .append(" - ")
                        .append(formatQuantity(ingredient.getQuantity()))
                        .append(" ")
                        .append(ingredient.getUnit())
                        .append("\n");
            }
            tvIngredients.setText(ingredientsText.toString().trim());
        } else {
            tvName.setText("Recipe not found");
        }
    }

    /**
     * Displays whole numbers without a trailing ".0" (e.g. "3" instead of "3.0"),
     * but keeps decimals when they matter (e.g. "0.5").
     */
    private String formatQuantity(double quantity) {
        if (quantity == Math.floor(quantity)) {
            return String.valueOf((long) quantity);
        }
        return String.valueOf(quantity);
    }
}