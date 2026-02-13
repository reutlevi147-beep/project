package com.mycasa.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class Finance extends AppCompatActivity {

    // ===== UI =====
    private ImageButton btnBack, btnPrevMonth, btnNextMonth, btnSettings;
    private FloatingActionButton btnAddTransaction;
    private View btnAddGoal;

    private TextView tvIncomeMonth, tvExpenseMonth, tvBalanceMonth, tvSelectedMonth;
    private RecyclerView rvCategoryExpenses;

    private RecyclerView rvGoals;
    private PieChart pieExpenses;
    private BarChart barYear;
    // ===== Pending approvals =====
    private RecyclerView rvPendingApprovals;
    private TextView tvPendingTitle;
    private PendingApprovalAdapter pendingAdapter;
    private final List<FlowItem> pendingItems = new ArrayList<>();

    private SavingsGoalAdapter goalsAdapter;
    private final List<SavingsGoal> goalsList = new ArrayList<>();
    private MaterialButtonToggleGroup toggleExpenseType;
    private boolean showingFixed = true;

    // ===== State =====
    private Calendar selectedMonth;
    private int lastRenderedYear = -1;
    private Calendar startMonth = null;

    // ===== Firebase =====
    private FirebaseFirestore db;
    private String groupId;
    private Calendar firstDataMonth = null;

    // ===== Category titles =====
    private static final Map<String, String> CATEGORY_TITLES = new HashMap<>();
    static {
        CATEGORY_TITLES.put("expense_housing", "דיור");
        CATEGORY_TITLES.put("expense_food", "אוכל וקניות");
        CATEGORY_TITLES.put("expense_transport", "תחבורה");
        CATEGORY_TITLES.put("expense_health", "בריאות");
        CATEGORY_TITLES.put("expense_education", "חינוך");
        CATEGORY_TITLES.put("expense_leisure", "פנאי");
        CATEGORY_TITLES.put("expense_other", "שונות");
    }

    // ===== Colors =====
    private static final int[] PIE_COLORS = new int[]{
            Color.parseColor("#6366F1"),
            Color.parseColor("#22C55E"),
            Color.parseColor("#F59E0B"),
            Color.parseColor("#EC4899"),
            Color.parseColor("#0EA5E9"),
            Color.parseColor("#A855F7"),
            Color.parseColor("#64748B")
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("Finance", "onCreate START");

        setContentView(R.layout.activity_finance);

        db = FirebaseFirestore.getInstance();

        // ========= groupId =========
        groupId = AppSession.getGroupId();

        if (groupId == null) {
            SharedPreferences prefs =
                    getSharedPreferences("app_prefs", MODE_PRIVATE);
            groupId = prefs.getString("group_id", null);
            AppSession.setGroupId(groupId);
        }

        if (groupId == null) {
            Log.e("Finance", "groupId still null – closing Finance");
            finish();
            return;
        }

        // ========= UI =========
        bindViews();
        setupMonth();
        setupListeners();
        setupGoals();
        setupPendingApprovals();
        loadPendingApprovals();
        loadCategoryList(true); // ברירת מחדל – קבועות



        // ========= Load start month from settings =========
        loadStartMonth();   // ⭐ זה הגבול האחורי לחודשים

        // ========= Data =========
        loadMonthData();

        lastRenderedYear = selectedMonth.get(Calendar.YEAR);
        loadYearBarChart();
        loadSavingsGoals();
    }




    private void detectFirstDataMonth() {

        db.collection("groups")
                .document(groupId)
                .collection("finance_flow_items")
                .whereEqualTo("enabled", true)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot.isEmpty()) return;

                    Date d = snapshot.getDocuments().get(0).getDate("createdAt");
                    if (d == null) return;

                    Calendar c = Calendar.getInstance();
                    c.setTime(d);
                    c.set(Calendar.DAY_OF_MONTH, 1);

                    firstDataMonth = c;

                    // ⬅️ חשוב: אם החודש הנוכחי לפני תחילת הנתונים – תקפיצי אליו
                    if (selectedMonth.before(firstDataMonth)) {
                        selectedMonth = (Calendar) firstDataMonth.clone();
                        updateMonthLabel();
                        loadMonthData();
                    }
                });
    }

    // ================= Bind =================
    private void bindViews() {

        btnBack = findViewById(R.id.btnBack);
        btnSettings = findViewById(R.id.btnFinanceSettings);
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);

        btnAddTransaction = findViewById(R.id.btnAddTransaction);
        btnAddGoal = findViewById(R.id.btnAddGoal);

        tvSelectedMonth = findViewById(R.id.tvSelectedMonth);
        tvIncomeMonth = findViewById(R.id.tvIncomeMonth);
        tvExpenseMonth = findViewById(R.id.tvExpenseMonth);
        tvBalanceMonth = findViewById(R.id.tvBalanceMonth);
        rvCategoryExpenses = findViewById(R.id.rvCategoryExpenses);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setAutoMeasureEnabled(true);
        rvCategoryExpenses.setLayoutManager(lm);

        rvGoals = findViewById(R.id.rvGoals);
        rvGoals.setLayoutManager(new LinearLayoutManager(this));
        rvCategoryExpenses.setNestedScrollingEnabled(false);
        rvCategoryExpenses.setHasFixedSize(false);
        rvCategoryExpenses.setItemViewCacheSize(50);

        pieExpenses = findViewById(R.id.pieFixedExpenses);
        barYear = findViewById(R.id.barMonthlySummary);

        toggleExpenseType = findViewById(R.id.toggleExpenseType);

        // ===== Pending approvals =====
        // ===== Pending approvals =====
        tvPendingTitle = findViewById(R.id.tvPendingTitle);
        rvPendingApprovals = findViewById(R.id.rvPendingApprovals);

        if (rvPendingApprovals != null) {
            rvPendingApprovals.setLayoutManager(new LinearLayoutManager(this));
            rvPendingApprovals.setNestedScrollingEnabled(false);
        }


    }

    // ================= Month =================
    private void setupMonth() {
        selectedMonth = Calendar.getInstance();
        selectedMonth.set(Calendar.DAY_OF_MONTH, 1);
        updateMonthLabel();
    }

    private void changeMonth(int delta) {

        Calendar next = (Calendar) selectedMonth.clone();
        next.add(Calendar.MONTH, delta);

        Calendar now = Calendar.getInstance();
        now.set(Calendar.DAY_OF_MONTH, 1);

        // ❌ עתיד
        if (next.after(now)) return;

        // ❌ לפני חודש התחלה מהגדרות כלכלה
        if (startMonth != null && next.before(startMonth)) return;

        selectedMonth = next;
        updateMonthLabel();
        loadMonthData();
    }



    private void updateMonthLabel() {
        tvSelectedMonth.setText(
                android.text.format.DateFormat.format("MMMM yyyy", selectedMonth)
        );
    }

    // ================= Listeners =================
    private void setupListeners() {

        btnBack.setOnClickListener(v -> finish());

        btnSettings.setOnClickListener(v -> {
            Intent i = new Intent(Finance.this, FinanceSetupActivity.class);
            i.putExtra("fromSettings", true);
            startActivity(i);
        });

        btnAddTransaction.setOnClickListener(v ->
                startActivity(new Intent(this, Add_Transaction.class)));

        btnAddGoal.setOnClickListener(v ->
                startActivity(new Intent(this, AddGoalActivity.class)));

        btnPrevMonth.setOnClickListener(v -> changeMonth(-1));
        btnNextMonth.setOnClickListener(v -> changeMonth(1));

        toggleExpenseType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            showingFixed = checkedId == R.id.btnFixed;

            loadCategoryList(showingFixed);   // 🔥 זה העיקר
            loadExpensesLive();               // תרשים
            loadSummaryLive();                // סיכומים

        });
    }

    // ================= Month Data =================
    private void loadMonthData() {
        loadSummaryLive();
        loadExpensesLive();
    }

    private void loadCategoryList(boolean fixed) {

        if (groupId == null) return;

        Map<String, CategoryWithItems> categoryMap = new LinkedHashMap<>();

        List<FlowCategory> categories = fixed
                ? FinanceCatalog.getFixedExpenseCategories()
                : FinanceCatalog.getVariableExpenseCategories();

        Log.d("CAT_DEBUG", "fixed=" + fixed + " categories.size=" + categories.size());
        for (FlowCategory c : categories) {
            Log.d("CAT_DEBUG", "cat=" + c.getId() + " / " + c.getTitle());
        }

        // ===== שלד מלא =====
        for (FlowCategory cat : categories) {

            CategoryWithItems parent = new CategoryWithItems(
                    cat.getId(),
                    cat.getTitle(),
                    resolveColorForFixedCategory(cat.getId()),
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

        // ===== מילוי סכומים בלבד =====
        db.collection("groups")
                .document(groupId)
                .collection("finance_flow_items")
                .whereEqualTo("enabled", true)
                .get()
                .addOnSuccessListener(snapshot -> {

                    for (QueryDocumentSnapshot doc : snapshot) {

                        String categoryId = doc.getString("categoryId");
                        if (!categoryMap.containsKey(categoryId)) continue;

                        CategoryWithItems parent = categoryMap.get(categoryId);

                        Double num = doc.getDouble("amount");
                        if (num == null) continue;

                        double amount = adjustRecurring(
                                num,
                                doc.getString("frequency")
                        );

                        String title = doc.getString("title");
                        if (title == null) continue;

                        for (SubCategoryItem sub : parent.items) {
                            if (title.equals(sub.title)) {
                                sub.amount += amount;
                                parent.totalAmount += amount;
                                break;
                            }
                        }
                    }

                    rvCategoryExpenses.setAdapter(
                            new FixedExpenseDrillAdapter(
                                    new ArrayList<>(categoryMap.values())
                            )
                    );
                });
    }

    private int resolveColorForFixedCategory(String categoryId) {
            switch (categoryId) {

                // ===== קבועות =====
                case "expense_communication": return Color.parseColor("#6366F1");
                case "expense_housing": return Color.parseColor("#4B5563");
                case "expense_kids": return Color.parseColor("#8B5CF6");
                case "expense_insurance": return Color.parseColor("#EC4899");
                case "expense_transport": return Color.parseColor("#10B981");
                case "expense_finance": return Color.parseColor("#F59E0B");
                case "expense_savings": return Color.parseColor("#14B8A6");
                case "expense_other_fixed": return Color.parseColor("#9CA3AF");

                // ===== משתנות =====
                case "expense_food": return Color.parseColor("#22C55E");
                case "expense_health": return Color.parseColor("#EF4444");
                case "expense_leisure": return Color.parseColor("#F97316");
                case "expense_personal": return Color.parseColor("#EC4899");
                case "expense_pets": return Color.parseColor("#84CC16");
                case "expense_home_misc": return Color.parseColor("#64748B");
                case "expense_one_time": return Color.parseColor("#0EA5E9");

                default:
                    return Color.parseColor("#CBD5E1");
            }
        }


        // ================= Summary =================
    private void loadSummaryLive() {

        db.collection("groups")
                .document(groupId)
                .collection("finance_flow_items")
                .whereEqualTo("enabled", true)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshot) {

                        double income = 0;
                        double expense = 0;

                        for (QueryDocumentSnapshot doc : snapshot) {

                            Double amount = doc.getDouble("amount");
                            if (amount == null) continue;

                            String cat = doc.getString("categoryId");
                            if (cat == null) continue;

                            Date date = resolveItemDate(doc);
                            boolean oneTime = Boolean.TRUE.equals(doc.getBoolean("isOneTime"));
                            String freq = doc.getString("frequency");

                            double v = amountForSelectedMonth(amount, freq, oneTime, date);

                            if (cat.startsWith("income_")) income += v;
                            else if (cat.startsWith("expense_")) expense += v;

                            Log.d("FinanceDebug",
                                    "month=" + (selectedMonth.get(Calendar.MONTH) + 1)
                                            + " amount=" + v
                            );

                        }

                        tvIncomeMonth.setText("₪" + format(income));
                        tvExpenseMonth.setText("₪" + format(expense));
                        tvBalanceMonth.setText("₪" + format(income - expense));
                    }
                });
    }

    // ================= Expenses Pie =================
    private void loadExpensesLive() {

        List<FlowCategory> cats = showingFixed
                ? FinanceCatalog.getFixedExpenseCategories()
                : FinanceCatalog.getVariableExpenseCategories();

        final Map<String, Double> totals = new LinkedHashMap<>();
        for (FlowCategory c : cats) totals.put(c.getId(), 0.0);

        db.collection("groups")
                .document(groupId)
                .collection("finance_flow_items")
                .whereEqualTo("enabled", true)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshot) {

                        for (QueryDocumentSnapshot doc : snapshot) {

                            String cat = doc.getString("categoryId");
                            if (!totals.containsKey(cat)) continue;

                            Double amount = doc.getDouble("amount");
                            if (amount == null) continue;

                            Date date = resolveItemDate(doc);
                            boolean oneTime = Boolean.TRUE.equals(doc.getBoolean("isOneTime"));
                            String freq = doc.getString("frequency");

                            double v = amountForSelectedMonth(amount, freq, oneTime, date);
                            totals.put(cat, totals.get(cat) + v);
                        }

                        renderPie(totals);
                    }
                });
    }




    private void renderPie(Map<String, Double> totals) {

        boolean hasData = false;
        double sum = 0;

        for (double v : totals.values()) {
            if (v > 0) {
                hasData = true;
                sum += v;
            }
        }

        if (!hasData) {
            pieExpenses.clear();
            pieExpenses.setNoDataText("אין הוצאות בחודש זה");
            pieExpenses.setNoDataTextColor(Color.GRAY);
            pieExpenses.invalidate();
            return;
        }

        final double totalSum = sum; // ⭐ פתרון השגיאה

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        for (String categoryId : totals.keySet()) {

            double value = totals.get(categoryId);
            if (value <= 0) continue;

            entries.add(new PieEntry((float) value, ""));

            // 🔥 אותו צבע כמו ברשימה
            colors.add(resolveColorForFixedCategory(categoryId));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(2f);

        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int percent = Math.round((value / (float) totalSum) * 100f);
                return percent + "%";
            }
        });

        dataSet.setValueTextSize(13f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        pieExpenses.setData(data);

        pieExpenses.setDrawEntryLabels(false);
        pieExpenses.getLegend().setEnabled(false);
        pieExpenses.getDescription().setEnabled(false);
        pieExpenses.setHoleRadius(55f);
        pieExpenses.setTransparentCircleRadius(60f);

        pieExpenses.invalidate();
    }




    // ================= Bar Chart =================
    private void loadYearBarChart() {

        final int year = selectedMonth.get(Calendar.YEAR);

        Calendar effectiveStart = Calendar.getInstance();
        effectiveStart.set(year, Calendar.JANUARY, 1);

// אם יש startMonth – משתמשים בו
        if (startMonth != null && startMonth.get(Calendar.YEAR) == year) {
            effectiveStart = (Calendar) startMonth.clone();
        }

        if (barYear == null) return;


        final double[] income = new double[12];
        final double[] expense = new double[12];

        Calendar now = Calendar.getInstance();
        now.set(Calendar.DAY_OF_MONTH, 1);

        db.collection("groups")
                .document(groupId)
                .collection("finance_flow_items")
                .whereEqualTo("enabled", true)
                .get()
                .addOnSuccessListener(snapshot -> {

                    for (QueryDocumentSnapshot doc : snapshot) {

                        Double amount = doc.getDouble("amount");
                        if (amount == null) continue;

                        String cat = doc.getString("categoryId");
                        if (cat == null) continue;

                        Date date = resolveItemDate(doc);
                        boolean oneTime = Boolean.TRUE.equals(doc.getBoolean("isOneTime"));
                        String freq = doc.getString("frequency");

                        // ===== חד־פעמי =====
                        if (oneTime && date != null) {

                            Calendar c = Calendar.getInstance();
                            c.setTime(date);

                            if (c.get(Calendar.YEAR) != year) continue;

                            int m = c.get(Calendar.MONTH);

                            if (c.before(startMonth) || c.after(now)) continue;

                            if (cat.startsWith("income_")) income[m] += amount;
                            else if (cat.startsWith("expense_")) expense[m] += amount;
                        }

                        // ===== חוזר =====
                        else {

                            double v = adjustRecurring(amount, freq);

                            Calendar cursor;

                            if (startMonth != null) {
                                cursor = (Calendar) startMonth.clone();
                            } else {
                                cursor = Calendar.getInstance();
                                cursor.set(year, Calendar.JANUARY, 1);
                            }

                            while (!cursor.after(now)) {

                                if (cursor.get(Calendar.YEAR) == year) {
                                    int m = cursor.get(Calendar.MONTH);

                                    if (cat.startsWith("income_")) income[m] += v;
                                    else if (cat.startsWith("expense_")) expense[m] += v;
                                }

                                cursor.add(Calendar.MONTH, 1);
                            }
                        }
                    }

                    renderYearBar(income, expense);
                });
    }


    private void renderYearBar(double[] income, double[] expense) {

        List<BarEntry> incomeEntries = new ArrayList<>();
        List<BarEntry> expenseEntries = new ArrayList<>();

        // 12 חודשים תמיד
        for (int i = 0; i < 12; i++) {
            incomeEntries.add(new BarEntry(i, (float) income[i]));
            expenseEntries.add(new BarEntry(i, (float) expense[i]));
        }

        BarDataSet dsIncome = new BarDataSet(incomeEntries, "הכנסות");
        dsIncome.setColor(Color.parseColor("#22C55E"));
        dsIncome.setValueTextSize(11f);

        BarDataSet dsExpense = new BarDataSet(expenseEntries, "הוצאות");
        dsExpense.setColor(Color.parseColor("#64748B"));
        dsExpense.setValueTextSize(11f);

        // ❌ מסתירים ערכים = 0
        ValueFormatter hideZeroFormatter = new ValueFormatter() {
            @Override
            public String getBarLabel(BarEntry barEntry) {
                return barEntry.getY() == 0f ? "" : String.valueOf((int) barEntry.getY());
            }
        };

        dsIncome.setValueFormatter(hideZeroFormatter);
        dsExpense.setValueFormatter(hideZeroFormatter);

        BarData data = new BarData(dsIncome, dsExpense);

        // רוחב עמודות
        float barWidth = 0.35f;
        float barSpace = 0.05f;
        float groupSpace = 0.25f;

        data.setBarWidth(barWidth);

        barYear.setData(data);

        // ===== X Axis =====
        String[] months = {
                "ינו", "פבר", "מרץ", "אפר", "מאי", "יונ",
                "יול", "אוג", "ספט", "אוק", "נוב", "דצ"
        };

        XAxis xAxis = barYear.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(months));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setCenterAxisLabels(true);
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(12f);

        // ===== Grouping =====
        barYear.groupBars(0f, groupSpace, barSpace);

        // ===== Style =====
        barYear.getAxisRight().setEnabled(false);
        barYear.getDescription().setEnabled(false);
        barYear.setExtraBottomOffset(8f);
        barYear.setFitBars(true);

        barYear.invalidate();
    }




    // ================= Goals =================
    private void setupGoals() {
        goalsAdapter = new SavingsGoalAdapter(goalsList);
        rvGoals.setAdapter(goalsAdapter);
    }

    private void loadSavingsGoals() {

        db.collection("groups")
                .document(groupId)
                .collection("savings_goals")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshot) {

                        goalsList.clear();

                        for (QueryDocumentSnapshot doc : snapshot) {

                            String title = doc.getString("title");
                            Long target = doc.getLong("targetAmount");
                            Long current = doc.getLong("currentAmount");
                            String mode = doc.getString("goalMode");

                            Date deadline = safeReadDeadline(doc.get("deadline"));

                            goalsList.add(new SavingsGoal(
                                    title,
                                    target != null ? target.intValue() : 0,
                                    current != null ? current.intValue() : 0,
                                    mode,
                                    deadline
                            ));
                        }

                        goalsAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void loadStartMonth() {

        db.collection("groups")
                .document(groupId)
                .collection("finance_settings")
                .document("main")
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) return;

                    Timestamp ts = doc.getTimestamp("startMonth");
                    if (ts == null) return;

                    Calendar c = Calendar.getInstance();
                    c.setTime(ts.toDate());
                    c.set(Calendar.DAY_OF_MONTH, 1);

                    startMonth = c;

                    // אם נפלנו לפני startMonth – קופצים אליו
                    if (selectedMonth.before(startMonth)) {
                        selectedMonth = (Calendar) startMonth.clone();
                        updateMonthLabel();
                        loadMonthData();
                    }
                });
    }

    // ================= Utils =================
    private Date resolveItemDate(QueryDocumentSnapshot doc) {
        Timestamp ts = doc.getTimestamp("lastApprovedAt");
        if (ts != null) return ts.toDate();
        return doc.getDate("createdAt");
    }

    private double amountForSelectedMonth(double amount, String freq, boolean oneTime, Date date) {
        if (oneTime) {
            if (date == null) return 0;
            return isSameMonth(date) ? amount : 0;
        }
        return adjustRecurring(amount, freq);
    }

    private double adjustRecurring(double a, String f) {
        if (f == null) return a;
        if (f.contains("שנת")) return a / 12;
        if (f.contains("דו")) return a / 2;
        return a;
    }

    private boolean isSameMonth(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        return c.get(Calendar.YEAR) == selectedMonth.get(Calendar.YEAR)
                && c.get(Calendar.MONTH) == selectedMonth.get(Calendar.MONTH);
    }

    private Date safeReadDeadline(Object o) {
        if (o instanceof Date) return (Date) o;
        if (o instanceof Timestamp) return ((Timestamp) o).toDate();
        return null;
    }

    private String format(double v) {
        return String.format(Locale.US, "%,.0f", v);
    }


    private void setupPendingApprovals() {

        pendingAdapter = new PendingApprovalAdapter(pendingItems);

        // אם כבר יש לך listener כזה באדפטר (ראיתי אצלך בעבר שיש OnPendingChangedListener)
        pendingAdapter.setOnPendingChangedListener(remainingCount -> {
            if (tvPendingTitle != null) {
                tvPendingTitle.setText("יש לך חיובים לאישור (" + remainingCount + ")");
            }
        });

        rvPendingApprovals.setAdapter(pendingAdapter);
    }
    private void loadPendingApprovals() {

        if (groupId == null) return;

        db.collection("groups")
                .document(groupId)
                .collection("finance_flow_items")
                .whereEqualTo("enabled", true)
                .get()
                .addOnSuccessListener(snapshot -> {

                    pendingItems.clear();

                    for (QueryDocumentSnapshot doc : snapshot) {

                        // ✅ תנאי "ממתין לאישור": אין lastApprovedAt
                        Timestamp lastApprovedAt = doc.getTimestamp("lastApprovedAt");
                        if (lastApprovedAt != null) continue;

                        String freq = doc.getString("frequency");
                        if (freq == null || !freq.contains("חודשי")) continue;

                        String categoryId = doc.getString("categoryId");
                        if (categoryId == null) continue;

                        // בד"כ חיובים לאישור = הוצאות (אם את רוצה גם הכנסות תורידי את זה)
                        if (!categoryId.startsWith("expense_")) continue;

                        String title = doc.getString("title");
                        if (title == null) title = "ללא כותרת";

                        Number num = (Number) doc.get("amount");
                        int amount = (num != null) ? (int) Math.round(num.doubleValue()) : 0;

                        FlowItem item = new FlowItem(doc.getId(), categoryId, title);
                        item.setAmount(amount);
                        item.setFrequency(freq);
                        item.setLastApprovedAt(null);

                        pendingItems.add(item);
                    }

                    if (tvPendingTitle != null) {
                        tvPendingTitle.setText("יש לך חיובים לאישור (" + pendingItems.size() + ")");
                    }

                    pendingAdapter.notifyDataSetChanged();
                });
    }


}
