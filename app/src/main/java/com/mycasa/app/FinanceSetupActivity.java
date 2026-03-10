package com.mycasa.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FinanceSetupActivity extends BaseActivity {

    private RecyclerView rvIncome;
    private RecyclerView rvExpenseFixed;
    private RecyclerView rvExpenseVariable;

    private TextView txtIncomeTotal;
    private TextView txtExpenseTotal;
    private TextView txtBalance;

    private List<FlowItem> allItems;

    private FlowCategoryAdapter incomeAdapter;
    private FlowCategoryAdapter fixedExpenseAdapter;
    private FlowCategoryAdapter variableExpenseAdapter;

    private FirebaseFirestore db;
    private String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finance_setup);

        db = FirebaseFirestore.getInstance();

        String mode = getIntent().getStringExtra("mode");
        boolean isEditMode = "edit".equals(mode);

        // ===== Load session =====
        SharedPreferences prefs =
                getSharedPreferences("app_prefs", MODE_PRIVATE);

        groupId = prefs.getString("group_id", null);
        String userRole = AppSession.getUserRole();
        Log.e("ROLE_DEBUG", "role=" + userRole);
        Log.e("SETUP_DEBUG", "groupId=" + groupId);

        if (groupId == null) {
            Log.e("SETUP_ERROR", "groupId is null");
            finish();
            return;
        }

        userRole = userRole == null ? "" : userRole.trim().toLowerCase();

        // ===== Only parent can access =====
        if (!userRole.equals("parent") && !isEditMode) {
            startActivity(new Intent(this, Finance.class));
            finish();
            return;
        }

        // ===== Bind views =====
        txtIncomeTotal = findViewById(R.id.txtIncomeTotal);
        txtExpenseTotal = findViewById(R.id.txtExpenseTotal);
        txtBalance = findViewById(R.id.txtBalance);

        rvIncome = findViewById(R.id.rvIncome);
        rvExpenseFixed = findViewById(R.id.rvExpenseFixed);
        rvExpenseVariable = findViewById(R.id.rvExpenseVariable);

        rvIncome.setLayoutManager(new LinearLayoutManager(this));
        rvExpenseFixed.setLayoutManager(new LinearLayoutManager(this));
        rvExpenseVariable.setLayoutManager(new LinearLayoutManager(this));

        // ===== Load catalog =====
        allItems = new ArrayList<>(FinanceCatalog.getAllItems());

        List<FlowCategory> incomeCategories =
                FinanceCatalog.getFixedIncomeCategories();

        List<FlowCategory> fixedExpenseCategories =
                FinanceCatalog.getFixedExpenseCategories();

        List<FlowCategory> variableExpenseCategories =
                FinanceCatalog.getVariableExpenseCategories();

        incomeAdapter =
                new FlowCategoryAdapter(this, incomeCategories, allItems);

        fixedExpenseAdapter =
                new FlowCategoryAdapter(this, fixedExpenseCategories, allItems);

        variableExpenseAdapter =
                new FlowCategoryAdapter(this, variableExpenseCategories, allItems);

        rvIncome.setAdapter(incomeAdapter);
        rvExpenseFixed.setAdapter(fixedExpenseAdapter);
        rvExpenseVariable.setAdapter(variableExpenseAdapter);

        incomeAdapter.setOnItemChangedListener(this::updateTotals);
        fixedExpenseAdapter.setOnItemChangedListener(this::updateTotals);
        variableExpenseAdapter.setOnItemChangedListener(this::updateTotals);

        incomeAdapter.setOnAddIncomeClickListener(this::addNewIncomeItem);

        // ===== Load existing data =====
        loadExistingAmounts();

        // ===== Finish button =====
        findViewById(R.id.btnFinish).setOnClickListener(v -> {

            Calendar c = Calendar.getInstance();
            c.set(Calendar.DAY_OF_MONTH, 1);

            Map<String, Object> settingsData = new HashMap<>();
            settingsData.put("startMonth",
                    new com.google.firebase.Timestamp(c.getTime()));

            db.collection("groups")
                    .document(groupId)
                    .collection("finance_settings")
                    .document("main")
                    .set(settingsData, SetOptions.merge())
                    .addOnSuccessListener(unused -> {

                        Log.e("SETUP_DEBUG", "Finance setup saved");

                        startActivity(new Intent(this, Finance.class));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("SETUP_ERROR", "Failed saving finance setup", e);
                    });
        });
    }

    // =====================
    // Totals calculation (חודשי אמיתי)
    // =====================
    private void updateTotals() {

        double totalIncome = 0;
        double totalExpense = 0;

        for (FlowItem item : allItems) {

            if (!item.isConfigured()) continue;

            double amount = item.getAmount();
            String freq = item.getFrequency();

            // חישוב חודשי אמיתי
            if (freq != null) {
                if (freq.contains("שנת")) amount /= 12;
                if (freq.contains("דו")) amount /= 2;
            }

            if (item.getCategoryId().startsWith("income_")) {
                totalIncome += amount;
            } else if (item.getCategoryId().startsWith("expense_")) {
                totalExpense += amount;
            }
        }

        txtIncomeTotal.setText("₪" + (int) totalIncome);
        txtExpenseTotal.setText("₪" + (int) totalExpense);
        txtBalance.setText("₪" + (int) (totalIncome - totalExpense));
    }

    // =====================
    // Add new income row
    // =====================
    private void addNewIncomeItem() {

        FlowItem newItem = new FlowItem(
                "income_" + System.currentTimeMillis(),
                "income_work",
                "משכורת נוספת"
        );

        newItem.setAmount(0);
        allItems.add(newItem);

        incomeAdapter.notifyDataSetChanged();
    }

    // =====================
    // Load existing amounts (מקור אמת אחד בלבד)
    // =====================
    private void loadExistingAmounts() {
        if (groupId == null) return;
        db.collection("groups")
                .document(groupId)
                .collection("finance_flow_items")
                .whereEqualTo("enabled", true)
                .get()
                .addOnSuccessListener(snapshot -> {

                    for (var doc : snapshot) {

                        String itemId = doc.getId();
                        Long amount = doc.getLong("amount");
                        String categoryId = doc.getString("categoryId");
                        String title = doc.getString("title");
                        String freq = doc.getString("frequency");

                        if (amount == null || categoryId == null) continue;

                        boolean found = false;

                        for (FlowItem item : allItems) {
                            if (item.getId().equals(itemId)) {
                                item.setAmount(amount.intValue());
                                item.setFrequency(freq);
                                found = true;
                                break;
                            }
                        }

                        // 🔥 אם לא קיים בקטלוג – נוסיף אותו דינמית
                        if (!found) {

                            FlowItem newItem = new FlowItem(
                                    itemId,
                                    categoryId,
                                    title != null ? title : "ללא כותרת"
                            );

                            newItem.setAmount(amount.intValue());
                            newItem.setFrequency(freq);

                            allItems.add(newItem);
                        }
                    }

                    updateTotals();

                    incomeAdapter.notifyDataSetChanged();
                    fixedExpenseAdapter.notifyDataSetChanged();
                    variableExpenseAdapter.notifyDataSetChanged();
                });
    }

}
