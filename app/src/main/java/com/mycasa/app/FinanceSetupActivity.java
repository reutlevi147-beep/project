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
import java.util.Map;

public class FinanceSetupActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finance_setup);

        // ========= האם הגיעו מה־⚙️ =========
        boolean fromSettings =
                getIntent().getBooleanExtra("fromSettings", false);

        SharedPreferences financePrefs =
                getSharedPreferences("finance_prefs", MODE_PRIVATE);

        boolean setupDone =
                financePrefs.getBoolean("finance_setup_done", false);

        if (setupDone && !fromSettings) {
            startActivity(new Intent(this, Finance.class));
            finish();
            return;
        }

        // ========= groupId =========
        SharedPreferences prefs =
                getSharedPreferences("app_prefs", MODE_PRIVATE);

        String groupId = prefs.getString("group_id", null);
        AppSession.setGroupId(groupId);

        if (groupId == null) {
            Log.e("FinanceSetup", "groupId is null – closing");
            finish();
            return;
        }

        // ========= Back =========
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // ========= Totals =========
        txtIncomeTotal = findViewById(R.id.txtIncomeTotal);
        txtExpenseTotal = findViewById(R.id.txtExpenseTotal);
        txtBalance = findViewById(R.id.txtBalance);

        // ========= RecyclerViews =========
        rvIncome = findViewById(R.id.rvIncome);
        rvExpenseFixed = findViewById(R.id.rvExpenseFixed);
        rvExpenseVariable = findViewById(R.id.rvExpenseVariable);

        rvIncome.setLayoutManager(new LinearLayoutManager(this));
        rvExpenseFixed.setLayoutManager(new LinearLayoutManager(this));
        rvExpenseVariable.setLayoutManager(new LinearLayoutManager(this));

        rvIncome.setNestedScrollingEnabled(false);
        rvExpenseFixed.setNestedScrollingEnabled(false);
        rvExpenseVariable.setNestedScrollingEnabled(false);

        // ========= Categories =========
        List<FlowCategory> incomeCategories =
                FinanceCatalog.getFixedIncomeCategories();

        List<FlowCategory> fixedExpenseCategories =
                FinanceCatalog.getFixedExpenseCategories();

        List<FlowCategory> variableExpenseCategories =
                FinanceCatalog.getVariableExpenseCategories();

        // ========= Base items =========
        allItems = new ArrayList<>(FinanceCatalog.getAllItems());

        // ========= Adapters =========
        incomeAdapter = new FlowCategoryAdapter(this, incomeCategories, allItems);
        fixedExpenseAdapter = new FlowCategoryAdapter(this, fixedExpenseCategories, allItems);
        variableExpenseAdapter = new FlowCategoryAdapter(this, variableExpenseCategories, allItems);

        incomeAdapter.setOnAddIncomeClickListener(this::addNewIncomeItem);

        incomeAdapter.setOnItemChangedListener(this::updateTotals);
        fixedExpenseAdapter.setOnItemChangedListener(this::updateTotals);
        variableExpenseAdapter.setOnItemChangedListener(this::updateTotals);

        rvIncome.setAdapter(incomeAdapter);
        rvExpenseFixed.setAdapter(fixedExpenseAdapter);
        rvExpenseVariable.setAdapter(variableExpenseAdapter);

        // ⭐️ טעינה נכונה – קודם דינמי, אחר כך סכומים
        loadDynamicIncomeItems();

        // ========= Finish =========
        MaterialButton btnFinish = findViewById(R.id.btnFinish);
        btnFinish.setOnClickListener(v -> {

            Calendar c = Calendar.getInstance();
            c.set(Calendar.DAY_OF_MONTH, 1);

            Map<String, Object> settingsData = new HashMap<>();
            settingsData.put("startMonth", new com.google.firebase.Timestamp(c.getTime()));

            FirebaseFirestore.getInstance()
                    .collection("groups")
                    .document(AppSession.getGroupId())
                    .collection("finance_settings")
                    .document("main")
                    .set(settingsData, SetOptions.merge());

            financePrefs.edit()
                    .putBoolean("finance_setup_done", true)
                    .apply();

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            for (FlowItem item : allItems) {

                if (!"income_work".equals(item.getCategoryId())) continue;

                Map<String, Object> incomeData = new HashMap<>();
                incomeData.put("title", item.getTitle());
                incomeData.put("amount", item.getAmount());
                incomeData.put("categoryId", item.getCategoryId());
                incomeData.put("enabled", true);

                db.collection("groups")
                        .document(AppSession.getGroupId())
                        .collection("finance_items")
                        .document(item.getId())
                        .set(incomeData, SetOptions.merge());
            }

            startActivity(new Intent(this, Finance.class));
            finish();
        });
    }

    // =====================
    // Totals calculation
    // =====================
    private void updateTotals() {

        int totalIncome = 0;
        int totalExpense = 0;

        for (FlowItem item : allItems) {
            if (!item.isConfigured()) continue;

            if (item.getCategoryId().startsWith("income")) {
                totalIncome += item.getAmount();
            } else {
                totalExpense += item.getAmount();
            }
        }

        txtIncomeTotal.setText("₪" + totalIncome);
        txtExpenseTotal.setText("₪" + totalExpense);
        txtBalance.setText("₪" + (totalIncome - totalExpense));
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
    // Load dynamic incomes
    // =====================
    private void loadDynamicIncomeItems() {

        FirebaseFirestore.getInstance()
                .collection("groups")
                .document(AppSession.getGroupId())
                .collection("finance_items")
                .whereEqualTo("enabled", true)
                .get()
                .addOnSuccessListener(snapshot -> {

                    for (var doc : snapshot) {

                        String id = doc.getId();

                        boolean exists = false;
                        for (FlowItem item : allItems) {
                            if (item.getId().equals(id)) {
                                exists = true;
                                break;
                            }
                        }
                        if (exists) continue;

                        FlowItem item = new FlowItem(
                                id,
                                doc.getString("categoryId"),
                                doc.getString("title")
                        );

                        Long amount = doc.getLong("amount");
                        if (amount != null) {
                            item.setAmount(amount.intValue());
                        }

                        allItems.add(item);
                    }

                    loadExistingAmounts();
                });
    }

    // =====================
    // Load existing amounts
    // =====================
    private void loadExistingAmounts() {

        FirebaseFirestore.getInstance()
                .collection("groups")
                .document(AppSession.getGroupId())
                .collection("finance_flow_items")
                .whereEqualTo("enabled", true)
                .get()
                .addOnSuccessListener(snapshot -> {

                    for (var doc : snapshot) {

                        String itemId = doc.getId(); // 👈 חשוב! זה ה־ID
                        Long amount = doc.getLong("amount");

                        if (amount == null) continue;

                        for (FlowItem item : allItems) {
                            if (item.getId().equals(itemId)) {
                                item.setAmount(amount.intValue());
                                break;
                            }
                        }
                    }

                    updateTotals();

                    rvIncome.getAdapter().notifyDataSetChanged();
                    rvExpenseFixed.getAdapter().notifyDataSetChanged();
                    rvExpenseVariable.getAdapter().notifyDataSetChanged();
                });
    }

}
