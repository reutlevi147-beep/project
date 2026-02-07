package com.mycasa.app;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Add_Transaction extends AppCompatActivity {

    // ===== UI =====
    private EditText etAmount, etTitle, etDate, etNotes;
    private MaterialButton btnSave, btnCancel;
    private MaterialButtonToggleGroup toggleType;
    private MaterialButton btnExpense, btnIncome;
    private RecyclerView rvCategories;

    // ===== State =====
    private String selectedCategoryId = null;
    private boolean isIncome = false; // default: הוצאה
    private Date selectedDate = new Date();

    // Firebase
    private FirebaseFirestore db;
    private String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        db = FirebaseFirestore.getInstance();
        groupId = AppSession.getGroupId();

        // ===== Bind =====
        etAmount = findViewById(R.id.etAmount);
        etTitle = findViewById(R.id.etTitle);
        etDate = findViewById(R.id.etDate);
        etNotes = findViewById(R.id.etNotes);

        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        toggleType = findViewById(R.id.toggleType);
        btnExpense = findViewById(R.id.btnExpense);
        btnIncome = findViewById(R.id.btnIncome);

        rvCategories = findViewById(R.id.rvCategories);

        // ===== Default =====
        toggleType.check(R.id.btnExpense);
        etDate.setText(formatDate(selectedDate));

        // ===== Toggle income / expense =====
        toggleType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;

            isIncome = checkedId == R.id.btnIncome;
            loadCategories();
        });

        // ===== Categories =====
        rvCategories.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        loadCategories();

        // ===== Date =====
        etDate.setOnClickListener(v -> openDatePicker());

        // ===== Actions =====
        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveTransaction());
    }

    // =========================
    // Categories
    // =========================
    private void loadCategories() {

        List<String> categories = isIncome
                ? FinanceCatalog.getIncomeCategoryIds()
                : FinanceCatalog.getExpenseCategoryIds();

        CategoryChipsAdapter adapter =
                new CategoryChipsAdapter(
                        categories,
                        categoryId -> selectedCategoryId = categoryId
                );

        rvCategories.setAdapter(adapter);
    }


    // =========================
    // Date Picker
    // =========================
    private void openDatePicker() {

        Calendar cal = Calendar.getInstance();
        cal.setTime(selectedDate);

        new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, day);
                    selectedDate = selected.getTime();
                    etDate.setText(formatDate(selectedDate));
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private String formatDate(Date date) {
        return android.text.format.DateFormat
                .format("dd/MM/yyyy", date)
                .toString();
    }

    // =========================
    // Save
    // =========================
    private void saveTransaction() {

        if (groupId == null) {
            Toast.makeText(this, "אין קבוצה פעילה", Toast.LENGTH_SHORT).show();
            return;
        }

        String amountStr = etAmount.getText().toString().trim();
        String title = etTitle.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        if (TextUtils.isEmpty(amountStr)) {
            etAmount.setError("חובה להזין סכום");
            return;
        }

        if (TextUtils.isEmpty(title)) {
            etTitle.setError("חובה להזין כותרת");
            return;
        }

        if (selectedCategoryId == null) {
            Toast.makeText(this, "יש לבחור קטגוריה", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (Exception e) {
            etAmount.setError("סכום לא תקין");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("amount", amount);
        data.put("categoryId", selectedCategoryId);
        data.put("enabled", true);
        data.put("frequency", "חד פעמי");
        data.put("createdAt", FieldValue.serverTimestamp());
        data.put("updatedAt", selectedDate);

        if (!TextUtils.isEmpty(notes)) {
            data.put("notes", notes);
        }

        db.collection("groups")
                .document(groupId)
                .collection("finance_flow_items")
                .add(data)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(
                            this,
                            isIncome ? "הכנסה נשמרה 💚" : "הוצאה נשמרה",
                            Toast.LENGTH_SHORT
                    ).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "שגיאה בשמירה",
                                Toast.LENGTH_LONG
                        ).show()
                );
    }
}
