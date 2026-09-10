package com.smartpantry.manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    public static final String TABLE_PANTRY = "pantry_items";
    public static final String TABLE_RECIPES = "recipes";
    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";

    // Pantry columns
    public static final String COL_PANTRY_ID = "id";
    public static final String COL_PANTRY_NAME = "name";
    public static final String COL_PANTRY_QUANTITY = "quantity";
    public static final String COL_PANTRY_UNIT = "unit";
    public static final String COL_PANTRY_EXPIRY = "expiry_date";

    // Recipe columns
    public static final String COL_RECIPE_ID = "id";
    public static final String COL_RECIPE_NAME = "name";
    public static final String COL_RECIPE_STEPS = "steps";

    // Recipe ingredient columns
    public static final String COL_RI_ID = "id";
    public static final String COL_RI_RECIPE_ID = "recipe_id";
    public static final String COL_RI_INGREDIENT_NAME = "ingredient_name";
    public static final String COL_RI_QUANTITY = "required_quantity";
    public static final String COL_RI_UNIT = "unit";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createPantryTable = "CREATE TABLE " + TABLE_PANTRY + " (" +
                COL_PANTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_PANTRY_NAME + " TEXT NOT NULL, " +
                COL_PANTRY_QUANTITY + " REAL NOT NULL, " +
                COL_PANTRY_UNIT + " TEXT, " +
                COL_PANTRY_EXPIRY + " TEXT" +
                ")";
        db.execSQL(createPantryTable);

        String createRecipesTable = "CREATE TABLE " + TABLE_RECIPES + " (" +
                COL_RECIPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RECIPE_NAME + " TEXT NOT NULL, " +
                COL_RECIPE_STEPS + " TEXT" +
                ")";
        db.execSQL(createRecipesTable);

        String createRecipeIngredientsTable = "CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " (" +
                COL_RI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RI_RECIPE_ID + " INTEGER NOT NULL, " +
                COL_RI_INGREDIENT_NAME + " TEXT NOT NULL, " +
                COL_RI_QUANTITY + " REAL NOT NULL, " +
                COL_RI_UNIT + " TEXT, " +
                "FOREIGN KEY(" + COL_RI_RECIPE_ID + ") REFERENCES " + TABLE_RECIPES + "(" + COL_RECIPE_ID + ")" +
                ")";
        db.execSQL(createRecipeIngredientsTable);

        // Pre-load the recipe collection on first run (Section 2.2)
        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);
    }
    // ---------- PANTRY CRUD METHODS ----------

    public long addPantryItem(String name, double quantity, String unit, String expiryDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PANTRY_NAME, name);
        values.put(COL_PANTRY_QUANTITY, quantity);
        values.put(COL_PANTRY_UNIT, unit);
        values.put(COL_PANTRY_EXPIRY, expiryDate);
        long id = db.insert(TABLE_PANTRY, null, values);
        db.close();
        return id;
    }

    public Cursor getAllPantryItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_PANTRY, null, null, null, null, null, COL_PANTRY_NAME + " ASC");
    }

    public int updatePantryItem(int id, String name, double quantity, String unit, String expiryDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PANTRY_NAME, name);
        values.put(COL_PANTRY_QUANTITY, quantity);
        values.put(COL_PANTRY_UNIT, unit);
        values.put(COL_PANTRY_EXPIRY, expiryDate);
        int rows = db.update(TABLE_PANTRY, values, COL_PANTRY_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    public void deletePantryItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PANTRY, COL_PANTRY_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    /**
     * Convenience method used by the matching logic: loads every
     * pantry item as a List<PantryItem> instead of a raw Cursor.
     */
    public List<PantryItem> getAllPantryItemsAsList() {
        List<PantryItem> list = new ArrayList<>();
        Cursor cursor = getAllPantryItems();
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PANTRY_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_PANTRY_NAME));
                double qty = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PANTRY_QUANTITY));
                String unit = cursor.getString(cursor.getColumnIndexOrThrow(COL_PANTRY_UNIT));
                String expiry = cursor.getString(cursor.getColumnIndexOrThrow(COL_PANTRY_EXPIRY));
                list.add(new PantryItem(id, name, qty, unit, expiry));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }
    // ---------- RECIPE METHODS ----------

    public long addRecipe(String name, String steps) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_RECIPE_NAME, name);
        values.put(COL_RECIPE_STEPS, steps);
        long id = db.insert(TABLE_RECIPES, null, values);
        db.close();
        return id;
    }

    public void addRecipeIngredient(long recipeId, String ingredientName, double quantity, String unit) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_RI_RECIPE_ID, recipeId);
        values.put(COL_RI_INGREDIENT_NAME, ingredientName);
        values.put(COL_RI_QUANTITY, quantity);
        values.put(COL_RI_UNIT, unit);
        db.insert(TABLE_RECIPE_INGREDIENTS, null, values);
        db.close();
    }

    public Cursor getAllRecipes() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_RECIPES, null, null, null, null, null, COL_RECIPE_NAME + " ASC");
    }

    public Cursor getRecipeIngredients(long recipeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_RECIPE_INGREDIENTS, null, COL_RI_RECIPE_ID + " = ?",
                new String[]{String.valueOf(recipeId)}, null, null, null);
    }

    public int getRecipeCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_RECIPES, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    /**
     * Loads every recipe together with its full ingredient list, ready
     * to hand to RecipeMatcher. Used by the Suggested Recipes screen.
     */
    public List<Recipe> getAllRecipesWithIngredients() {
        List<Recipe> recipes = new ArrayList<>();
        Cursor recipeCursor = getAllRecipes();

        if (recipeCursor.moveToFirst()) {
            do {
                long id = recipeCursor.getLong(recipeCursor.getColumnIndexOrThrow(COL_RECIPE_ID));
                String name = recipeCursor.getString(recipeCursor.getColumnIndexOrThrow(COL_RECIPE_NAME));
                String steps = recipeCursor.getString(recipeCursor.getColumnIndexOrThrow(COL_RECIPE_STEPS));

                List<RecipeIngredient> ingredients = new ArrayList<>();
                Cursor ingredientCursor = getRecipeIngredients(id);
                if (ingredientCursor.moveToFirst()) {
                    do {
                        String ingName = ingredientCursor.getString(
                                ingredientCursor.getColumnIndexOrThrow(COL_RI_INGREDIENT_NAME));
                        double ingQty = ingredientCursor.getDouble(
                                ingredientCursor.getColumnIndexOrThrow(COL_RI_QUANTITY));
                        String ingUnit = ingredientCursor.getString(
                                ingredientCursor.getColumnIndexOrThrow(COL_RI_UNIT));
                        ingredients.add(new RecipeIngredient(ingName, ingQty, ingUnit));
                    } while (ingredientCursor.moveToNext());
                }
                ingredientCursor.close();

                recipes.add(new Recipe(id, name, steps, ingredients));
            } while (recipeCursor.moveToNext());
        }
        recipeCursor.close();
        return recipes;
    }

    // ---------- SEED DATA ----------

    /**
     * Inserts a starter collection of 18 recipes (Section 2.2 requires
     * 15-20). Ingredient names/units deliberately overlap with common
     * pantry items so the strict-matching rule has real data to work
     * with. Uses the SQLiteDatabase passed in from onCreate directly -
     * calling getWritableDatabase() from inside onCreate can deadlock,
     * so this does NOT reuse the addRecipe()/addRecipeIngredient() helpers.
     */
    private void seedRecipes(SQLiteDatabase db) {
        insertRecipe(db, "Tomato Pasta",
                "1. Boil pasta until al dente.\n2. Saute garlic in olive oil.\n3. Add chopped tomatoes and simmer.\n4. Toss pasta through the sauce and serve.",
                new Object[][]{
                        {"pasta", 200, "g"}, {"tomato", 3, "pcs"}, {"garlic clove", 2, "pcs"}, {"olive oil", 2, "tbsp"}
                });

        insertRecipe(db, "Garlic Fried Rice",
                "1. Heat oil and fry minced garlic until golden.\n2. Add cooked rice and stir-fry.\n3. Season with salt and pepper, serve hot.",
                new Object[][]{
                        {"rice", 300, "g"}, {"garlic clove", 3, "pcs"}, {"olive oil", 1, "tbsp"}, {"salt", 1, "tsp"}
                });

        insertRecipe(db, "Scrambled Eggs on Toast",
                "1. Whisk eggs with a splash of milk.\n2. Scramble gently in butter over low heat.\n3. Serve on toasted bread.",
                new Object[][]{
                        {"egg", 3, "pcs"}, {"milk", 30, "ml"}, {"butter", 1, "tbsp"}, {"bread", 2, "pcs"}
                });

        insertRecipe(db, "Grilled Cheese Sandwich",
                "1. Butter two slices of bread.\n2. Add cheese between them.\n3. Grill both sides until golden and cheese melts.",
                new Object[][]{
                        {"bread", 2, "pcs"}, {"cheese", 60, "g"}, {"butter", 1, "tbsp"}
                });

        insertRecipe(db, "Vegetable Stir Fry",
                "1. Chop all vegetables.\n2. Stir-fry in oil over high heat.\n3. Add soy sauce and cook 2 more minutes.",
                new Object[][]{
                        {"carrot", 2, "pcs"}, {"bell pepper", 1, "pcs"}, {"onion", 1, "pcs"}, {"soy sauce", 2, "tbsp"}, {"olive oil", 1, "tbsp"}
                });

        insertRecipe(db, "Chicken and Rice Bowl",
                "1. Cook rice.\n2. Pan-fry chicken breast with salt and pepper.\n3. Slice chicken and serve over rice.",
                new Object[][]{
                        {"chicken breast", 250, "g"}, {"rice", 200, "g"}, {"salt", 1, "tsp"}, {"olive oil", 1, "tbsp"}
                });

        insertRecipe(db, "Potato and Onion Hash",
                "1. Dice potatoes and onion.\n2. Fry in oil until golden and tender.\n3. Season and serve.",
                new Object[][]{
                        {"potato", 3, "pcs"}, {"onion", 1, "pcs"}, {"olive oil", 2, "tbsp"}, {"salt", 1, "tsp"}
                });

        insertRecipe(db, "Banana Oat Pancakes",
                "1. Mash banana and mix with oats and egg.\n2. Cook spoonfuls on a lightly oiled pan until golden on both sides.",
                new Object[][]{
                        {"banana", 1, "pcs"}, {"oats", 100, "g"}, {"egg", 1, "pcs"}
                });

        insertRecipe(db, "Peanut Butter Banana Toast",
                "1. Toast the bread.\n2. Spread peanut butter.\n3. Top with sliced banana.",
                new Object[][]{
                        {"bread", 2, "pcs"}, {"peanut butter", 30, "g"}, {"banana", 1, "pcs"}
                });

        insertRecipe(db, "Tomato and Egg Soup",
                "1. Simmer chopped tomato in water until soft.\n2. Whisk in beaten egg while stirring.\n3. Season with salt and serve.",
                new Object[][]{
                        {"tomato", 2, "pcs"}, {"egg", 2, "pcs"}, {"salt", 1, "tsp"}
                });

        insertRecipe(db, "Spinach and Cheese Omelette",
                "1. Whisk eggs.\n2. Pour into a hot pan, add spinach and cheese.\n3. Fold and cook until set.",
                new Object[][]{
                        {"egg", 3, "pcs"}, {"spinach", 50, "g"}, {"cheese", 40, "g"}
                });

        insertRecipe(db, "Bean and Rice Burrito Bowl",
                "1. Cook rice.\n2. Warm beans with a pinch of cumin.\n3. Combine in a bowl and top with cheese.",
                new Object[][]{
                        {"rice", 200, "g"}, {"beans", 200, "g"}, {"cheese", 40, "g"}, {"cumin", 1, "tsp"}
                });

        insertRecipe(db, "Honey Garlic Chicken",
                "1. Pan-sear chicken breast.\n2. Add minced garlic and honey, simmer until glazed.\n3. Serve hot.",
                new Object[][]{
                        {"chicken breast", 250, "g"}, {"garlic clove", 2, "pcs"}, {"honey", 2, "tbsp"}
                });

        insertRecipe(db, "Lemon Butter Pasta",
                "1. Boil pasta.\n2. Melt butter with lemon juice.\n3. Toss pasta in the sauce and season with salt.",
                new Object[][]{
                        {"pasta", 200, "g"}, {"butter", 2, "tbsp"}, {"lemon", 1, "pcs"}, {"salt", 1, "tsp"}
                });

        insertRecipe(db, "Carrot and Potato Soup",
                "1. Chop carrot, potato and onion.\n2. Simmer in water until soft.\n3. Blend until smooth and season.",
                new Object[][]{
                        {"carrot", 2, "pcs"}, {"potato", 2, "pcs"}, {"onion", 1, "pcs"}, {"salt", 1, "tsp"}
                });

        insertRecipe(db, "Yogurt and Honey Oats",
                "1. Combine oats and yogurt.\n2. Drizzle with honey.\n3. Top with sliced banana.",
                new Object[][]{
                        {"oats", 80, "g"}, {"yogurt", 150, "ml"}, {"honey", 1, "tbsp"}, {"banana", 1, "pcs"}
                });

        insertRecipe(db, "Cheesy Baked Beans on Toast",
                "1. Toast the bread.\n2. Warm the beans.\n3. Top toast with beans and grated cheese.",
                new Object[][]{
                        {"bread", 2, "pcs"}, {"beans", 200, "g"}, {"cheese", 40, "g"}
                });

        insertRecipe(db, "Ginger Soy Fried Rice",
                "1. Fry grated ginger in oil.\n2. Add rice and soy sauce, stir-fry until heated through.\n3. Serve immediately.",
                new Object[][]{
                        {"rice", 250, "g"}, {"ginger", 1, "tbsp"}, {"soy sauce", 2, "tbsp"}, {"olive oil", 1, "tbsp"}
                });
    }

    /**
     * Inserts one recipe row plus all of its ingredient rows.
     * ingredients format: {name (String), quantity (number), unit (String)}
     */
    private long insertRecipe(SQLiteDatabase db, String name, String steps, Object[][] ingredients) {
        ContentValues recipeValues = new ContentValues();
        recipeValues.put(COL_RECIPE_NAME, name);
        recipeValues.put(COL_RECIPE_STEPS, steps);
        long recipeId = db.insert(TABLE_RECIPES, null, recipeValues);

        for (Object[] ingredient : ingredients) {
            ContentValues ingredientValues = new ContentValues();
            ingredientValues.put(COL_RI_RECIPE_ID, recipeId);
            ingredientValues.put(COL_RI_INGREDIENT_NAME, (String) ingredient[0]);
            ingredientValues.put(COL_RI_QUANTITY, ((Number) ingredient[1]).doubleValue());
            ingredientValues.put(COL_RI_UNIT, (String) ingredient[2]);
            db.insert(TABLE_RECIPE_INGREDIENTS, null, ingredientValues);
        }
        return recipeId;
    }
}