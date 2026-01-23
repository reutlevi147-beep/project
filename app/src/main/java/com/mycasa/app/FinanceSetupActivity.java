package com.mycasa.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class FinanceSetupActivity extends AppCompatActivity {

    private RecyclerView rvIncome;
    private RecyclerView rvExpenseFixed;
    private RecyclerView rvExpenseVariable;

    private TextView txtIncomeTotal;
    private TextView txtExpenseTotal;
    private TextView txtBalance;

    private List<FlowItem> allItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finance_setup);

        // =====================
        // groupId → AppSession
        // =====================
        SharedPreferences prefs =
                getSharedPreferences("app_prefs", MODE_PRIVATE);

        String groupId = prefs.getString("group_id", null);
        AppSession.setGroupId(groupId);

        // =====================
        // Back
        // =====================
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // =====================
        // Totals
        // =====================
        txtIncomeTotal = findViewById(R.id.txtIncomeTotal);
        txtExpenseTotal = findViewById(R.id.txtExpenseTotal);
        txtBalance = findViewById(R.id.txtBalance);

        // =====================
        // RecyclerViews
        // =====================
        rvIncome = findViewById(R.id.rvIncome);
        rvExpenseFixed = findViewById(R.id.rvExpenseFixed);
        rvExpenseVariable = findViewById(R.id.rvExpenseVariable);

        rvIncome.setLayoutManager(new LinearLayoutManager(this));
        rvExpenseFixed.setLayoutManager(new LinearLayoutManager(this));
        rvExpenseVariable.setLayoutManager(new LinearLayoutManager(this));

        rvIncome.setNestedScrollingEnabled(false);
        rvExpenseFixed.setNestedScrollingEnabled(false);
        rvExpenseVariable.setNestedScrollingEnabled(false);

        // =====================
        // Data from catalog
        // =====================
        List<FlowCategory> incomeCategories =
                FinanceCatalog.getFixedIncomeCategories();

        List<FlowCategory> fixedExpenseCategories =
                FinanceCatalog.getFixedExpenseCategories();

        List<FlowCategory> variableExpenseCategories =
                FinanceCatalog.getVariableExpenseCategories();

        allItems = FinanceCatalog.getAllItems();

        // =====================
        // Adapters
        // =====================
        FlowCategoryAdapter incomeAdapter =
                new FlowCategoryAdapter(
                        this,
                        incomeCategories,
                        allItems
                );

        FlowCategoryAdapter fixedExpenseAdapter =
                new FlowCategoryAdapter(
                        this,
                        fixedExpenseCategories,
                        allItems
                );

        FlowCategoryAdapter variableExpenseAdapter =
                new FlowCategoryAdapter(
                        this,
                        variableExpenseCategories,
                        allItems
                );

        // ⭐️ חיבור עדכון סכומים
        incomeAdapter.setOnItemChangedListener(this::updateTotals);
        fixedExpenseAdapter.setOnItemChangedListener(this::updateTotals);
        variableExpenseAdapter.setOnItemChangedListener(this::updateTotals);

        rvIncome.setAdapter(incomeAdapter);
        rvExpenseFixed.setAdapter(fixedExpenseAdapter);
        rvExpenseVariable.setAdapter(variableExpenseAdapter);

        // =====================
        // Initial totals
        // =====================
        updateTotals();

        // =====================
        // Finish → Finance
        // =====================
        MaterialButton btnFinish = findViewById(R.id.btnFinish);
        btnFinish.setOnClickListener(v -> {
            startActivity(
                    new Intent(
                            FinanceSetupActivity.this,
                            Finance.class
                    )
            );
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

        int balance = totalIncome - totalExpense;

        txtIncomeTotal.setText("₪" + totalIncome);
        txtExpenseTotal.setText("₪" + totalExpense);
        txtBalance.setText("₪" + balance);
    }
}
