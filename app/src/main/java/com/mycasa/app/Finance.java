package com.mycasa.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

public class Finance extends AppCompatActivity {

    // ===== UI =====
    private ImageButton btnBack;
    private FloatingActionButton btnAddTransaction;
    private BarChart barMonthlySummary;

    private MaterialButtonToggleGroup togglePeriod;
    private MaterialButton btnWeek, btnMonth, btnYear;

    // ⭐ NEW – קבועות / משתנות
    private MaterialButtonToggleGroup toggleExpenseType;
    private MaterialButton btnFixed, btnVariable;

    private TextView tvIncomeMonth, tvExpenseMonth, tvBalanceMonth;
    private RecyclerView rvCategories;

    // Pie
    private PieChart pieFixedExpenses;

    // Firebase
    private FirebaseFirestore db;
    private String groupId;

    private String selectedPeriod = "month";
    private boolean showingFixed = true; // ⭐ NEW

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finance);

        db = FirebaseFirestore.getInstance();
        groupId = AppSession.getGroupId();

        btnBack = findViewById(R.id.btnBack);
        btnAddTransaction = findViewById(R.id.btnAddTransaction);

        togglePeriod = findViewById(R.id.togglePeriod);
        btnWeek = findViewById(R.id.btnWeek);
        btnMonth = findViewById(R.id.btnMonth);
        btnYear = findViewById(R.id.btnYear);

        // ⭐ NEW
        toggleExpenseType = findViewById(R.id.toggleExpenseType);
        btnFixed = findViewById(R.id.btnFixed);
        btnVariable = findViewById(R.id.btnVariable);

        tvIncomeMonth = findViewById(R.id.tvIncomeMonth);
        tvExpenseMonth = findViewById(R.id.tvExpenseMonth);
        tvBalanceMonth = findViewById(R.id.tvBalanceMonth);
        barMonthlySummary = findViewById(R.id.barMonthlySummary);

        pieFixedExpenses = findViewById(R.id.pieFixedExpenses);

        rvCategories = findViewById(R.id.rvCategoryExpenses);
        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        rvCategories.setNestedScrollingEnabled(false);
        rvCategories.setHasFixedSize(false);
        rvCategories.setItemViewCacheSize(50);

        btnBack.setOnClickListener(v -> finish());

        btnAddTransaction.setOnClickListener(v ->
                startActivity(new Intent(this, Add_Transaction.class))
        );

        // ===== תקופה =====
        togglePeriod.check(R.id.btnMonth);
        togglePeriod.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;

            if (checkedId == R.id.btnWeek) selectedPeriod = "week";
            else if (checkedId == R.id.btnMonth) selectedPeriod = "month";
            else if (checkedId == R.id.btnYear) selectedPeriod = "year";

            loadSummary();
            reloadExpenses();
        });

        // ===== קבועות / משתנות =====
        toggleExpenseType.check(R.id.btnFixed);

        toggleExpenseType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;

            if (checkedId == R.id.btnFixed) {
                showingFixed = true;
                loadFixedExpensesWithDrillDown();
            } else if (checkedId == R.id.btnVariable) {
                showingFixed = false;
                loadVariableExpensesWithDrillDown();
            }
        });

        loadSummary();
        loadFixedExpensesWithDrillDown();
        loadMonthlyBarChart();
    }

    // ===============================
    // Summary
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

                        double amount = adjustAmountByFrequency(
                                num.doubleValue(),
                                doc.getString("frequency")
                        );

                        String categoryId = doc.getString("categoryId");
                        if (categoryId == null) continue;

                        if (categoryId.startsWith("income_")) income += amount;
                        else if (categoryId.startsWith("expense_")) expense += amount;
                    }

                    double balance = income - expense;

                    tvIncomeMonth.setText("₪" + format(income));
                    tvExpenseMonth.setText("₪" + format(expense));
                    tvBalanceMonth.setText("₪" + format(balance));

                    tvIncomeMonth.setTextColor(getColor(R.color.green_income));
                    tvExpenseMonth.setTextColor(getColor(R.color.red_expense));
                    tvBalanceMonth.setTextColor(
                            balance >= 0
                                    ? getColor(R.color.green_income)
                                    : getColor(R.color.red_expense)
                    );
                });
    }

    // ===============================
    // קבועות
    // ===============================
    private void loadFixedExpensesWithDrillDown() {
        loadExpensesWithDrillDown(
                FinanceCatalog.getFixedExpenseCategories(),
                true
        );
    }

    // ===============================
    // משתנות
    // ===============================
    private void loadVariableExpensesWithDrillDown() {
        loadExpensesWithDrillDown(
                FinanceCatalog.getVariableExpenseCategories(),
                false
        );
    }

    // ===============================
    // לוגיקה משותפת
    // ===============================
    private void loadExpensesWithDrillDown(
            List<FlowCategory> categories,
            boolean fixed
    ) {

        if (groupId == null) return;

        Map<String, CategoryWithItems> categoryMap = new LinkedHashMap<>();

        for (FlowCategory cat : categories) {

            CategoryWithItems parent = new CategoryWithItems(
                    cat.getId(),
                    cat.getTitle(),
                    fixed
                            ? resolveColorForFixedCategory(cat.getId())
                            : resolveColorForVariableCategory(cat.getId()),
                    0,
                    new ArrayList<>()
            );

            for (FlowItem item : FinanceCatalog.getAllItems()) {
                if (cat.getId().equals(item.getCategoryId())) {
                    parent.items.add(
                            new SubCategoryItem(
                                    item.getId(),
                                    item.getTitle(),
                                    0
                            )
                    );
                }
            }

            categoryMap.put(cat.getId(), parent);
        }

        db.collection("groups")
                .document(groupId)
                .collection("finance_flow_items")
                .whereEqualTo("enabled", true)
                .get()
                .addOnSuccessListener(snapshot -> {

                    for (QueryDocumentSnapshot doc : snapshot) {

                        String categoryId = doc.getString("categoryId");
                        if (!categoryMap.containsKey(categoryId)) continue;

                        Number num = (Number) doc.get("amount");
                        if (num == null) continue;

                        double amount = adjustAmountByFrequency(
                                num.doubleValue(),
                                doc.getString("frequency")
                        );

                        String itemId = doc.getId();
                        CategoryWithItems parent = categoryMap.get(categoryId);

                        for (SubCategoryItem sub : parent.items) {
                            if (sub.id.equals(itemId)) {
                                sub.amount += amount;
                                parent.totalAmount += amount;
                                break;
                            }
                        }
                    }

                    List<CategoryWithItems> result =
                            new ArrayList<>(categoryMap.values());

                    rvCategories.setAdapter(
                            new FixedExpenseDrillAdapter(result)
                    );

                    renderExpensesPie(
                            result,
                            fixed ? "הוצאות קבועות" : "הוצאות משתנות"
                    );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    // ===============================
    // Pie – משותף
    // ===============================
    private void renderExpensesPie(
            List<CategoryWithItems> categories,
            String centerText
    ) {

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        for (CategoryWithItems cat : categories) {
            if (cat.totalAmount <= 0) continue;

            entries.add(new PieEntry(
                    (float) cat.totalAmount,
                    cat.title
            ));
            colors.add(cat.color);
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(2f);
        dataSet.setDrawValues(true);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return Math.round(value) + "%";
            }
        });

        PieData data = new PieData(dataSet);

        pieFixedExpenses.setData(data);
        pieFixedExpenses.setUsePercentValues(true);
        pieFixedExpenses.setDrawEntryLabels(false);
        pieFixedExpenses.getDescription().setEnabled(false);
        pieFixedExpenses.setDrawHoleEnabled(true);
        pieFixedExpenses.setHoleRadius(55f);
        pieFixedExpenses.setTransparentCircleRadius(60f);
        pieFixedExpenses.setCenterText(centerText);
        pieFixedExpenses.setCenterTextSize(14f);
        pieFixedExpenses.invalidate();
    }

    // ===============================
    // Utils
    // ===============================
    private void reloadExpenses() {
        if (showingFixed) {
            loadFixedExpensesWithDrillDown();
        } else {
            loadVariableExpensesWithDrillDown();
        }
    }

    private int resolveColorForFixedCategory(String categoryId) {
        switch (categoryId) {
            case "expense_communication": return Color.parseColor("#6366F1");
            case "expense_housing": return Color.parseColor("#4B5563");
            case "expense_kids": return Color.parseColor("#8B5CF6");
            case "expense_insurance": return Color.parseColor("#EC4899");
            case "expense_transport": return Color.parseColor("#10B981");
            case "expense_finance": return Color.parseColor("#F59E0B");
            case "expense_savings": return Color.parseColor("#14B8A6");
            case "expense_other_fixed": return Color.parseColor("#9CA3AF");
            default: return Color.parseColor("#9CA3AF");
        }
    }

    private int resolveColorForVariableCategory(String categoryId) {
        switch (categoryId) {
            case "expense_food": return Color.parseColor("#F59E0B");
            case "expense_health": return Color.parseColor("#EC4899");
            case "expense_leisure": return Color.parseColor("#8B5CF6");
            case "expense_personal": return Color.parseColor("#6366F1");
            case "expense_pets": return Color.parseColor("#10B981");
            case "expense_home_misc": return Color.parseColor("#9CA3AF");
            default: return Color.parseColor("#9CA3AF");
        }
    }
    private void renderMonthlyBarChart(
            List<String> months,
            List<Float> incomes,
            List<Float> expenses
    ) {
        List<BarEntry> incomeEntries = new ArrayList<>();
        List<BarEntry> expenseEntries = new ArrayList<>();

        for (int i = 0; i < months.size(); i++) {
            incomeEntries.add(new BarEntry(i, incomes.get(i)));
            expenseEntries.add(new BarEntry(i, expenses.get(i)));
        }

        BarDataSet incomeSet = new BarDataSet(incomeEntries, "הכנסות");
        incomeSet.setColor(Color.parseColor("#14B8A6")); // ירוק טורקיז

        BarDataSet expenseSet = new BarDataSet(expenseEntries, "הוצאות");
        expenseSet.setColor(Color.parseColor("#64748B")); // אפור

        BarData data = new BarData(incomeSet, expenseSet);
        data.setBarWidth(0.35f);
        data.setValueTextSize(10f);

        barMonthlySummary.setData(data);

        // X Axis – חודשים
        XAxis xAxis = barMonthlySummary.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(months));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        // Y Axis
        barMonthlySummary.getAxisRight().setEnabled(false);
        barMonthlySummary.getAxisLeft().setAxisMinimum(0f);

        // מרווח בין קבוצות
        barMonthlySummary.groupBars(0f, 0.3f, 0.05f);

        barMonthlySummary.getDescription().setEnabled(false);
        barMonthlySummary.animateY(600);
        barMonthlySummary.invalidate();
    }
    private void loadMonthlyBarChart() {

        if (groupId == null) return;

        // monthIndex (0–11) -> [income, expense]
        Map<Integer, double[]> monthlyMap = new LinkedHashMap<>();

        for (int i = 0; i < 12; i++) {
            monthlyMap.put(i, new double[]{0, 0});
        }

        db.collection("groups")
                .document(groupId)
                .collection("finance_flow_items")
                .whereEqualTo("enabled", true)
                .get()
                .addOnSuccessListener(snapshot -> {

                    for (QueryDocumentSnapshot doc : snapshot) {

                        Number num = (Number) doc.get("amount");
                        if (num == null) continue;

                        // ===== בחירת תאריך נכונה =====
                        Date date = null;

                        // 1️⃣ תאריך אישור (בעיקר להוצאות)
                        com.google.firebase.Timestamp approvedTs =
                                doc.getTimestamp("lastApprovedAt");
                        if (approvedTs != null) {
                            date = approvedTs.toDate();
                        }

                        // 2️⃣ fallback – createdAt
                        if (date == null) {
                            date = doc.getDate("createdAt");
                        }

                        // 3️⃣ fallback – updatedAt
                        if (date == null) {
                            date = doc.getDate("updatedAt");
                        }

                        if (date == null) continue;

                        Calendar cal = Calendar.getInstance();
                        cal.setTime(date);
                        int month = cal.get(Calendar.MONTH); // 0–11

                        double amount = adjustAmountByFrequency(
                                num.doubleValue(),
                                doc.getString("frequency")
                        );

                        String categoryId = doc.getString("categoryId");
                        if (categoryId == null) continue;

                        if (categoryId.startsWith("income_")) {
                            monthlyMap.get(month)[0] += amount;
                        } else if (categoryId.startsWith("expense_")) {
                            monthlyMap.get(month)[1] += amount;
                        }
                    }

                    renderMonthlyBarChart(monthlyMap);
                });
    }


    private void renderMonthlyBarChart(Map<Integer, double[]> monthlyMap) {

        List<BarEntry> incomeEntries = new ArrayList<>();
        List<BarEntry> expenseEntries = new ArrayList<>();
        List<String> months = new ArrayList<>();

        String[] monthNames = {
                "ינו", "פבר", "מרץ", "אפר",
                "מאי", "יונ", "יול", "אוג",
                "ספט", "אוק", "נוב", "דצ"
        };

        int index = 0;
        for (int month : monthlyMap.keySet()) {

            double income = monthlyMap.get(month)[0];
            double expense = monthlyMap.get(month)[1];

            incomeEntries.add(new BarEntry(index, (float) income));
            expenseEntries.add(new BarEntry(index, (float) expense));
            months.add(monthNames[month]);

            Log.d(
                    "BAR_CHECK",
                    monthNames[month] + " | income=" + income + " expense=" + expense
            );

            index++;
        }

        // 🔹 כאן ההגדרה שחסרה לך
        BarDataSet incomeSet = new BarDataSet(incomeEntries, "הכנסות");
        incomeSet.setColor(Color.parseColor("#14B8A6"));

        BarDataSet expenseSet = new BarDataSet(expenseEntries, "הוצאות");
        expenseSet.setColor(Color.parseColor("#4B5563"));

        BarData data = new BarData(incomeSet, expenseSet);

        float barWidth = 0.3f;
        float barSpace = 0.05f;
        float groupSpace = 0.3f;

        data.setBarWidth(barWidth);
        data.setDrawValues(false);

        barMonthlySummary.setData(data);

        // ===== X Axis =====
        XAxis xAxis = barMonthlySummary.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(months));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setCenterAxisLabels(true);
        xAxis.setDrawGridLines(false);

        // 🔑 קריטי להצגת שתי הסדרות
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(months.size());

        // ===== Y Axis =====
        barMonthlySummary.getAxisLeft().setAxisMinimum(0f);
        barMonthlySummary.getAxisRight().setEnabled(false);

        barMonthlySummary.getDescription().setEnabled(false);
        barMonthlySummary.setExtraBottomOffset(10f);

        barMonthlySummary.groupBars(
                0f,
                groupSpace,
                barSpace
        );

        barMonthlySummary.animateY(600);
        barMonthlySummary.invalidate();


    }



    private double adjustAmountByFrequency(double amount, String frequency) {
        if (frequency == null) return amount;
        String f = frequency.trim();
        if (f.contains("שנת")) return amount / 12.0;
        if (f.contains("דו")) return amount / 2.0;
        return amount;
    }

    private String format(double value) {
        return String.format(Locale.US, "%,.0f", value);
    }
}
