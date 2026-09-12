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

    private RecyclerView recyclerViewAlmostThere;
    private RecipeListAdapter almostThereAdapter;
    private View divider;
    private View tvAlmostThereTitle;
    private View tvAlmostThereSubtitle;
    private List<Recipe> almostThereRecipes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipes);

        dbHelper = new DatabaseHelper(this);

        recyclerView = findViewById(R.id.recyclerViewSuggestedRecipes);
        tvNoSuggestions = findViewById(R.id.tvNoSuggestions);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeListAdapter(suggestedRecipes, this::openRecipeDetail);
        recyclerView.setAdapter(adapter);

        recyclerViewAlmostThere = findViewById(R.id.recyclerViewAlmostThere);
        divider = findViewById(R.id.divider);
        tvAlmostThereTitle = findViewById(R.id.tvAlmostThereTitle);
        tvAlmostThereSubtitle = findViewById(R.id.tvAlmostThereSubtitle);

        recyclerViewAlmostThere.setLayoutManager(new LinearLayoutManager(this));
        almostThereAdapter = new RecipeListAdapter(almostThereRecipes, this::openRecipeDetail);
        recyclerViewAlmostThere.setAdapter(almostThereAdapter);

        setupBottomNavigation();
        loadSuggestedRecipes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSuggestedRecipes();
    }

    private void openRecipeDetail(Recipe recipe) {
        Intent intent = new Intent(SuggestedRecipesActivity.this, RecipeDetailActivity.class);
        intent.putExtra("recipe_id", recipe.getId());
        startActivity(intent);
    }

    /**
     * Runs the strict-matching rule (Section 2.3) against the current
     * pantry and displays only recipes the user can make right now,
     * plus a separate "Almost There" bonus list (Section 8) for recipes
     * missing exactly one ingredient.
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

        List<Recipe> almostThere = RecipeMatcher.getAlmostThereRecipes(allRecipes, pantryItems);
        almostThereRecipes.clear();
        almostThereRecipes.addAll(almostThere);
        almostThereAdapter.setRecipes(almostThereRecipes);

        int almostThereVisibility = almostThereRecipes.isEmpty() ? View.GONE : View.VISIBLE;
        divider.setVisibility(almostThereVisibility);
        tvAlmostThereTitle.setVisibility(almostThereVisibility);
        tvAlmostThereSubtitle.setVisibility(almostThereVisibility);
        recyclerViewAlmostThere.setVisibility(almostThereVisibility);
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