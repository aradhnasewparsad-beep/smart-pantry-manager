package com.smartpantry.manager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class SuggestedRecipesActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private RecipeListAdapter adapter;
    private View tvNoSuggestions;
    private List<Recipe> suggestedRecipes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        dbHelper = new DatabaseHelper(this);

        recyclerView = findViewById(R.id.recyclerViewSuggestedRecipes);
        tvNoSuggestions = findViewById(R.id.tvNoSuggestions);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeListAdapter(suggestedRecipes, recipe -> {
            Intent intent = new Intent(SuggestedRecipesActivity.this, RecipeDetailActivity.class);
            intent.putExtra("recipe_id", recipe.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        setupBottomNavigation();
        loadSuggestedRecipes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSuggestedRecipes();
    }

    /**
     * Runs the strict-matching rule (Section 2.3) against the current
     * pantry and displays only recipes the user can make right now.
     */
    private void loadSuggestedRecipes() {
        List<Recipe> allRecipes = dbHelper.getAllRecipesWithIngredients();
        List<PantryItem> pantryItems = dbHelper.getAllPantryItemsAsList();

        List<Recipe> matched = RecipeMatcher.getSuggestedRecipes(allRecipes, pantryItems);

        suggestedRecipes.clear();
        suggestedRecipes.addAll(matched);
        adapter.setRecipes(suggestedRecipes);

        tvNoSuggestions.setVisibility(suggestedRecipes.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(suggestedRecipes.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_suggested);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_pantry) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_suggested) {
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }
}
