package com.mycasa.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class Finance extends AppCompatActivity {

    // ===== UI =====
    private ImageButton btnBack;
    private FloatingActionButton btnAddTransaction;

    private MaterialButtonToggleGroup togglePeriod;
    private MaterialButton btnWeek, btnMonth, btnYear;

    // ===== State =====
    private String selectedPeriod = "month"; // week | month | year

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finance);

        // ===== Bind Views =====
        btnBack = findViewById(R.id.btnBack);
        btnAddTransaction = findViewById(R.id.btnAddTransaction);

        togglePeriod = findViewById(R.id.togglePeriod);
        btnWeek = findViewById(R.id.btnWeek);
        btnMonth = findViewById(R.id.btnMonth);
        btnYear = findViewById(R.id.btnYear);

        // ===== Safety check =====
        if (btnAddTransaction == null) {
            Toast.makeText(this, "❌ btnAddTransaction = NULL", Toast.LENGTH_LONG).show();
            return;
        }

        // ===== Back =====
        btnBack.setOnClickListener(v -> finish());

        // ===== Floating Action Button (+) =====
        btnAddTransaction.setOnClickListener(v -> {
            Toast.makeText(this, "✅ פלוס נלחץ", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Add_Transaction.class));
        });

        // ===== Default selection =====
        togglePeriod.check(R.id.btnMonth);
        selectedPeriod = "month";

        // ===== Period toggle =====
        togglePeriod.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;

            if (checkedId == R.id.btnWeek) {
                selectedPeriod = "week";
            } else if (checkedId == R.id.btnMonth) {
                selectedPeriod = "month";
            } else if (checkedId == R.id.btnYear) {
                selectedPeriod = "year";
            }

            onPeriodChanged(selectedPeriod);
        });
    }

    // ===============================
    // Period changed (placeholder)
    // ===============================
    private void onPeriodChanged(String period) {
        // TODO:
        // - טעינת נתונים
        // - חישוב הכנסות / הוצאות
        // - עדכון UI
    }
}
