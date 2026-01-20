package com.mycasa.app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

public class Add_Transaction extends AppCompatActivity {

    private String selectedCategory = null;

    private EditText etTitle;
    private EditText etAmount;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        // ===== Bind views =====
        etTitle = findViewById(R.id.etTitle);
        etAmount = findViewById(R.id.etAmount);
        btnSave = findViewById(R.id.btnSave);

        // ===== Categories RecyclerView =====
        RecyclerView rvCategories = findViewById(R.id.rvCategories);

        List<String> categories = Arrays.asList(
                "🛒 קניות",
                "🏠 דיור",
                "🍔 אוכל",
                "🚗 תחבורה",
                "❤️ בריאות",
                "⚡ חשבונות"
        );

        rvCategories.setLayoutManager(
                new LinearLayoutManager(
                        this,
                        LinearLayoutManager.HORIZONTAL,
                        false
                )
        );

        CategoryChipsAdapter adapter =
                new CategoryChipsAdapter(
                        categories,
                        category -> selectedCategory = category
                );

        rvCategories.setAdapter(adapter);

        // ===== Save =====
        btnSave.setOnClickListener(v -> saveTransaction());
    }

    private void saveTransaction() {

        String title = etTitle.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError("חובה להזין כותרת");
            return;
        }

        if (amountStr.isEmpty()) {
            etAmount.setError("חובה להזין סכום");
            return;
        }

        if (selectedCategory == null) {
            Toast.makeText(this, "יש לבחור קטגוריה", Toast.LENGTH_SHORT).show();
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            etAmount.setError("סכום לא תקין");
            return;
        }

        // ✅ כאן בהמשך תיכנס שמירה ל־Firebase
        Toast.makeText(
                this,
                "נשמר:\n" + title + "\n" + selectedCategory + "\n₪" + amount,
                Toast.LENGTH_LONG
        ).show();

        finish();
    }
}
