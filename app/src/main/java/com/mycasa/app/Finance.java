package com.mycasa.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Finance extends AppCompatActivity {

    // ===== UI =====
    private ImageButton btnBack;
    private FloatingActionButton btnAddTransaction;

    private MaterialButtonToggleGroup togglePeriod;
    private MaterialButton btnWeek, btnMonth, btnYear;

    private TextView tvIncomeMonth, tvExpenseMonth, tvBalanceMonth;

    // ===== Firebase =====
    private FirebaseFirestore db;
    private String groupId;

    private RecyclerView rvCategories;

    // ===== State =====
    private String selectedPeriod = "month"; // week | month | year
    // ===============================
// Resolve main category color
// ===============================
    private int resolveMainCategoryColor(String categoryId) {

        if (categoryId == null) return Color.parseColor("#9CA3AF");

        if (categoryId.startsWith("expense_car")) return Color.parseColor("#6366F1");
        if (categoryId.startsWith("expense_food")) return Color.parseColor("#F59E0B");
        if (categoryId.startsWith("expense_transport")) return Color.parseColor("#10B981");
        if (categoryId.startsWith("expense_health")) return Color.parseColor("#EC4899");
        if (categoryId.startsWith("expense_children")) return Color.parseColor("#8B5CF6");

        // דיור
        if (categoryId.startsWith("expense_rent")
                || categoryId.startsWith("expense_mortgage")
                || categoryId.startsWith("expense_water")
                || categoryId.startsWith("expense_tv")
                || categoryId.startsWith("expense_phone")) {
            return Color.parseColor("#4B5563");
        }

        return Color.parseColor("#9CA3AF");
    }



    // ===============================
// Resolve main category name
// ===============================
    private String resolveMainCategoryName(String categoryId) {

        if (categoryId == null) return "שונות";

        if (categoryId.startsWith("expense_car")) return "רכב";
        if (categoryId.startsWith("expense_food")) return "אוכל";
        if (categoryId.startsWith("expense_transport")) return "תחבורה";
        if (categoryId.startsWith("expense_health")) return "בריאות";
        if (categoryId.startsWith("expense_children")) return "ילדים";

        // דיור
        if (categoryId.startsWith("expense_rent")
                || categoryId.startsWith("expense_mortgage")
                || categoryId.startsWith("expense_water")
                || categoryId.startsWith("expense_tv")
                || categoryId.startsWith("expense_phone")) {
            return "דיור";
        }

        return "שונות";
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finance);

        // ===== Firebase =====
        db = FirebaseFirestore.getInstance();
        groupId = AppSession.getGroupId();

        // ===== Bind Views =====
        btnBack = findViewById(R.id.btnBack);
        btnAddTransaction = findViewById(R.id.btnAddTransaction);

        togglePeriod = findViewById(R.id.togglePeriod);
        btnWeek = findViewById(R.id.btnWeek);
        btnMonth = findViewById(R.id.btnMonth);
        btnYear = findViewById(R.id.btnYear);

        tvIncomeMonth = findViewById(R.id.tvIncomeMonth);
        tvExpenseMonth = findViewById(R.id.tvExpenseMonth);
        tvBalanceMonth = findViewById(R.id.tvBalanceMonth);

        // ===== Back =====
        btnBack.setOnClickListener(v -> finish());

        // ===== Floating Action Button (+) =====
        btnAddTransaction.setOnClickListener(v ->
                startActivity(new Intent(this, Add_Transaction.class))
        );

        // ===== Pending approvals =====
        View cardPending = findViewById(R.id.cardPending);
        RecyclerView rvPending = findViewById(R.id.rvPending);

        rvPending.setLayoutManager(new LinearLayoutManager(this));
        rvPending.setNestedScrollingEnabled(false);
        rvCategories = findViewById(R.id.rvCategoryExpenses);
        rvCategories.setLayoutManager(new LinearLayoutManager(this));

        RecyclerView rvCategories = findViewById(R.id.rvCategoryExpenses);
        rvCategories.setLayoutManager(new LinearLayoutManager(this));



        FinanceRepository.getPendingItems(
                groupId,
                selectedPeriod,
                pendingItems -> {

                    if (pendingItems.isEmpty()) {
                        cardPending.setVisibility(View.GONE);
                    } else {
                        cardPending.setVisibility(View.VISIBLE);

                        PendingApprovalAdapter adapter =
                                new PendingApprovalAdapter(pendingItems);

                        adapter.setOnPendingChangedListener(remaining -> {
                            if (remaining == 0) {
                                cardPending.setVisibility(View.GONE);
                            }
                        });

                        rvPending.setAdapter(adapter);
                    }
                }
        );

        // ===== Period toggle =====
        togglePeriod.check(R.id.btnMonth);

        togglePeriod.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;

            if (checkedId == R.id.btnWeek) selectedPeriod = "week";
            else if (checkedId == R.id.btnMonth) selectedPeriod = "month";
            else if (checkedId == R.id.btnYear) selectedPeriod = "year";

            loadSummary(); // רענון סכומים
        });

        // ===== Load summary =====
        loadSummary();
        loadExpensesByCategory();

    }

    // ===============================
    // Load income / expense / balance
    // ===============================
    private void loadSummary() {

        if (groupId == null) return;

        db.collection("groups")
                .document(groupId)
                .collection("finance_flow_items")
                .whereEqualTo("enabled", true)
                .get()
                .addOnSuccessListener(snapshot -> {

                    double income = 0;
                    double expense = 0;

                    for (QueryDocumentSnapshot doc : snapshot) {

                        Number num = (Number) doc.get("amount");
                        if (num == null) continue;

                        double rawAmount = num.doubleValue();

                        String frequency = doc.getString("frequency");
                        double amount = adjustAmountByFrequency(rawAmount, frequency);

                        String categoryId = doc.getString("categoryId");
                        if (categoryId == null) continue;

                        if (categoryId.startsWith("income_")) income += amount;
                        else if (categoryId.startsWith("expense_")) expense += amount;

                        // צבעים להכנסות / הוצאות
                        tvIncomeMonth.setTextColor(
                                getResources().getColor(R.color.green_income)
                        );

                        tvExpenseMonth.setTextColor(
                                getResources().getColor(R.color.red_expense)
                        );

                    }

                    double balance = income - expense;

                    tvIncomeMonth.setText("₪" + format(income));
                    tvExpenseMonth.setText("₪" + format(expense));
                    tvBalanceMonth.setText("₪" + format(balance));

                    // ===== Balance smart color =====
                    if (balance >= 0) {
                        tvBalanceMonth.setTextColor(
                                getResources().getColor(R.color.green_income)
                        );
                    } else {
                        tvBalanceMonth.setTextColor(
                                getResources().getColor(R.color.red_expense)
                        );
                    }

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Finance load failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private double adjustAmountByFrequency(double amount, String frequency) {

        if (frequency == null) return amount;

        String f = frequency.trim(); // מסיר רווחים

        if (f.contains("שנת")) {
            return amount / 12.0;
        }

        if (f.contains("דו")) {
            return amount / 2.0;
        }

        // חודשי או כל ברירת מחדל
        return amount;
    }

    private void loadExpensesByCategory() {

        if (groupId == null) return;

        db.collection("groups")
                .document(groupId)
                .collection("finance_flow_items")
                .whereEqualTo("enabled", true)
                .get()
                .addOnSuccessListener(snapshot -> {

                    Map<String, Double> totals = new LinkedHashMap<>();

                    for (QueryDocumentSnapshot doc : snapshot) {

                        String categoryId = doc.getString("categoryId");
                        if (categoryId == null || !categoryId.startsWith("expense_")) continue;

                        Number num = (Number) doc.get("amount");
                        if (num == null) continue;

                        double rawAmount = num.doubleValue();
                        String frequency = doc.getString("frequency");
                        double amount = adjustAmountByFrequency(rawAmount, frequency);

                        double current = totals.containsKey(categoryId)
                                ? totals.get(categoryId)
                                : 0;

                        totals.put(categoryId, current + amount);
                    }

                    List<CategoryExpenseItem> items = new ArrayList<>();

                    for (Map.Entry<String, Double> e : totals.entrySet()) {

                        String categoryId = e.getKey();

                        String title = resolveMainCategoryName(categoryId);
                        int color = resolveMainCategoryColor(categoryId);

                        items.add(new CategoryExpenseItem(
                                categoryId,
                                title,
                                e.getValue(),
                                color
                        ));
                    }


                    rvCategories.setAdapter(new CategoryExpenseAdapter(items));
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }



    private String format(double value) {
        return String.format(Locale.US, "%,.0f", value);
    }
}
