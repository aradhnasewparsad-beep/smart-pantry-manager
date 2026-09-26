package com.smartpantry.manager;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PantryAdapter.OnItemActionListener {

    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private PantryAdapter adapter;
    private TextView tvEmptyState;
    private List<PantryItem> pantryList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        recyclerView = findViewById(R.id.recyclerViewPantry);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        FloatingActionButton fabAddItem = findViewById(R.id.fabAddItem);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PantryAdapter(pantryList, this);
        recyclerView.setAdapter(adapter);

        fabAddItem.setOnClickListener(v -> showAddEditDialog(null));

        setupBottomNavigation();
        loadPantryItems();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPantryItems();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_pantry);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_pantry) {
                return true;
            } else if (id == R.id.nav_suggested) {
                startActivity(new Intent(this, SuggestedRecipesActivity.class));
                overridePendingTransition(0, 0);
                finish();
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

    private void loadPantryItems() {
        pantryList.clear();
        Cursor cursor = dbHelper.getAllPantryItems();
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PANTRY_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PANTRY_NAME));
                double qty = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PANTRY_QUANTITY));
                String unit = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PANTRY_UNIT));
                String expiry = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PANTRY_EXPIRY));
                pantryList.add(new PantryItem(id, name, qty, unit, expiry));
            } while (cursor.moveToNext());
        }
        cursor.close();

        adapter.setItems(pantryList);
        tvEmptyState.setVisibility(pantryList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showAddEditDialog(PantryItem existingItem) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 24);

        EditText etName = new EditText(this);
        etName.setHint("Ingredient name");
        layout.addView(etName);

        EditText etQuantity = new EditText(this);
        etQuantity.setHint("Quantity (e.g. 2)");
        etQuantity.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etQuantity);

        EditText etUnit = new EditText(this);
        etUnit.setHint("Unit (e.g. pcs, g, ml)");
        layout.addView(etUnit);

        EditText etExpiry = new EditText(this);
        etExpiry.setHint("Expiry date (optional, e.g. 2026-09-15)");
        layout.addView(etExpiry);

        TextView tvError = new TextView(this);
        tvError.setTextColor(Color.parseColor("#D32F2F"));
        tvError.setPadding(0, 24, 0, 0);
        tvError.setVisibility(View.GONE);
        layout.addView(tvError);

        if (existingItem != null) {
            etName.setText(existingItem.getName());
            etQuantity.setText(String.valueOf(existingItem.getQuantity()));
            etUnit.setText(existingItem.getUnit());
            etExpiry.setText(existingItem.getExpiryDate());
        }

        String title = existingItem == null ? "Add Pantry Item" : "Edit Pantry Item";

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(layout)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            android.widget.Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener(v -> {
                String name = etName.getText().toString().trim();
                String quantityStr = etQuantity.getText().toString().trim();
                String unit = etUnit.getText().toString().trim();
                String expiry = etExpiry.getText().toString().trim();

                if (name.isEmpty() || quantityStr.isEmpty()) {
                    tvError.setText("Name and quantity are required");
                    tvError.setVisibility(View.VISIBLE);
                    return;
                }

                double quantity;
                try {
                    quantity = Double.parseDouble(quantityStr);
                } catch (NumberFormatException e) {
                    tvError.setText("Quantity must be a number");
                    tvError.setVisibility(View.VISIBLE);
                    return;
                }

                if (existingItem == null) {
                    dbHelper.addPantryItem(name, quantity, unit, expiry);
                } else {
                    dbHelper.updatePantryItem(existingItem.getId(), name, quantity, unit, expiry);
                }
                loadPantryItems();
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    @Override
    public void onEdit(PantryItem item) {
        showAddEditDialog(item);
    }

    @Override
    public void onDelete(PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Item")
                .setMessage("Delete " + item.getName() + " from your pantry?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deletePantryItem(item.getId());
                    loadPantryItems();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}