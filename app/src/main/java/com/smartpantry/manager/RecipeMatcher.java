package com.smartpantry.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements the assignment's "Strict-Matching Rule" (Section 2.3).
 * A recipe is only "suggested" if every ingredient it needs is present
 * in the pantry in at least the required quantity.
 *
 * Handles simple real-world messiness:
 *  - case differences ("Tomato" vs "tomato")
 *  - basic singular/plural differences ("tomato" vs "tomatoes")
 *  - common unit differences within the same measurement type
 *    (e.g. grams vs kilograms, millilitres vs litres)
 *
 * This is intentionally NOT a full NLP solution - just enough naive
 * normalization to avoid breaking on trivial text/unit differences,
 * as required by the brief.
 */
public class RecipeMatcher {

    /**
     * Returns only the recipes the user can make right now: every
     * required ingredient must be present in at least the required
     * quantity. No partial matches allowed.
     */
    public static List<Recipe> getSuggestedRecipes(List<Recipe> allRecipes, List<PantryItem> pantryItems) {
        Map<String, Double> pantryStock = buildPantryStock(pantryItems);
        List<Recipe> suggested = new ArrayList<>();

        for (Recipe recipe : allRecipes) {
            if (missingIngredientCount(recipe, pantryStock) == 0) {
                suggested.add(recipe);
            }
        }
        return suggested;
    }

    /**
     * Optional stretch feature (Section 8): recipes missing exactly
     * ONE ingredient. Kept clearly separate from the strict list above.
     */
    public static List<Recipe> getAlmostThereRecipes(List<Recipe> allRecipes, List<PantryItem> pantryItems) {
        Map<String, Double> pantryStock = buildPantryStock(pantryItems);
        List<Recipe> almostThere = new ArrayList<>();

        for (Recipe recipe : allRecipes) {
            if (missingIngredientCount(recipe, pantryStock) == 1) {
                almostThere.add(recipe);
            }
        }
        return almostThere;
    }

    /**
     * Counts how many of a recipe's ingredients are NOT sufficiently
     * covered by the pantry. 0 = fully makeable.
     */
    private static int missingIngredientCount(Recipe recipe, Map<String, Double> pantryStock) {
        int missing = 0;
        for (RecipeIngredient required : recipe.getIngredients()) {
            String key = normalizeName(required.getName());
            double haveQty = pantryStock.getOrDefault(key, 0.0);
            double needQty = toBaseQuantity(required.getQuantity(), required.getUnit());

            if (haveQty < needQty) {
                missing++;
            }
        }
        return missing;
    }

    /**
     * Collapses the pantry into normalizedName -> total quantity
     * (converted to a common base unit), so duplicate/near-duplicate
     * pantry entries combine correctly.
     */
    private static Map<String, Double> buildPantryStock(List<PantryItem> pantryItems) {
        Map<String, Double> stock = new HashMap<>();
        for (PantryItem item : pantryItems) {
            String key = normalizeName(item.getName());
            double baseQty = toBaseQuantity(item.getQuantity(), item.getUnit());
            stock.merge(key, baseQty, Double::sum);
        }
        return stock;
    }

    /**
     * Naive ingredient-name normalization: lowercase, trim, and strip
     * common plural endings so "tomato" and "tomatoes" are treated as
     * the same ingredient.
     */
    static String normalizeName(String rawName) {
        if (rawName == null) return "";
        String name = rawName.trim().toLowerCase();

        if (name.endsWith("ies") && name.length() > 4) {
            name = name.substring(0, name.length() - 3) + "y";       // berries -> berry
        } else if (name.endsWith("oes") && name.length() > 4) {
            name = name.substring(0, name.length() - 2);             // tomatoes -> tomato
        } else if (name.endsWith("es") && name.length() > 4) {
            name = name.substring(0, name.length() - 2);             // dishes -> dish
        } else if (name.endsWith("s") && !name.endsWith("ss") && name.length() > 3) {
            name = name.substring(0, name.length() - 1);             // eggs -> egg
        }
        return name;
    }

    /**
     * Converts a quantity to a common base unit so different-but-compatible
     * units can be compared fairly (e.g. a recipe needing 500 g of flour
     * is satisfied by a pantry entry of 0.5 kg).
     * Weight -> grams, Volume -> millilitres, anything else (pcs, clove,
     * whole, unrecognized) is treated as a plain count and left as-is.
     */
    static double toBaseQuantity(double quantity, String unit) {
        if (unit == null) return quantity;
        String u = unit.trim().toLowerCase();

        switch (u) {
            // weight -> grams
            case "g":
            case "gram":
            case "grams":
                return quantity;
            case "kg":
            case "kilogram":
            case "kilograms":
                return quantity * 1000;

            // volume -> millilitres
            case "ml":
            case "millilitre":
            case "millilitres":
                return quantity;
            case "l":
            case "litre":
            case "litres":
                return quantity * 1000;
            case "tsp":
            case "teaspoon":
            case "teaspoons":
                return quantity * 5;
            case "tbsp":
            case "tablespoon":
            case "tablespoons":
                return quantity * 15;
            case "cup":
            case "cups":
                return quantity * 250;

            // count-based units: no conversion needed
            default:
                return quantity;
        }
    }
}