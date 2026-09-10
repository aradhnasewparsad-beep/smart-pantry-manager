package com.smartpantry.manager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.materialswitch.MaterialSwitch;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "smart_pantry_prefs";
    private static final String KEY_EXPIRY_ALERTS = "expiry_alerts_enabled";
    private static final String KEY_METRIC_UNITS = "prefer_metric_units";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        MaterialSwitch switchExpiryAlerts = findViewById(R.id.switchExpiryAlerts);
        MaterialSwitch switchMetricUnits = findViewById(R.id.switchMetricUnits);

        switchExpiryAlerts.setChecked(prefs.getBoolean(KEY_EXPIRY_ALERTS, true));
        switchMetricUnits.setChecked(prefs.getBoolean(KEY_METRIC_UNITS, true));

        switchExpiryAlerts.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean(KEY_EXPIRY_ALERTS, isChecked).apply());

        switchMetricUnits.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean(KEY_METRIC_UNITS, isChecked).apply());

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_settings);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_pantry) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_suggested) {
                startActivity(new Intent(this, SuggestedRecipesActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_settings) {
                return true;
            }
            return false;
        });
    }
}