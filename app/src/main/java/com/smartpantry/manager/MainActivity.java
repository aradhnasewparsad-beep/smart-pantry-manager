package com.smartpantry.manager;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

        loadPantryItems();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPantryItems();
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

        if (existingItem != null) {
            etName.setText(existingItem.getName());
            etQuantity.setText(String.valueOf(existingItem.getQuantity()));
            etUnit.setText(existingItem.getUnit());
            etExpiry.setText(existingItem.getExpiryDate());
        }

        String title = existingItem == null ? "Add Pantry Item" : "Edit Pantry Item";

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String quantityStr = etQuantity.getText().toString().trim();
                    String unit = etUnit.getText().toString().trim();
                    String expiry = etExpiry.getText().toString().trim();

                    if (name.isEmpty() || quantityStr.isEmpty()) {
                        Toast.makeText(this, "Name and quantity are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double quantity;
                    try {
                        quantity = Double.parseDouble(quantityStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Quantity must be a number", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (existingItem == null) {
                        dbHelper.addPantryItem(name, quantity, unit, expiry);
                    } else {
                        dbHelper.updatePantryItem(existingItem.getId(), name, quantity, unit, expiry);
                    }
                    loadPantryItems();
                })
                .setNegativeButton("Cancel", null)
                .show();
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