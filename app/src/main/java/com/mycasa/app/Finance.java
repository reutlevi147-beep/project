package com.mycasa.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
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

import eightbitlab.com.blurview.BlurView;

public class Finance extends BaseActivity {

    // ===== UI =====
    private ImageButton btnBack, btnPrevMonth, btnNextMonth, btnSettings;
    private FloatingActionButton btnAddTransaction;
    private View btnAddGoal;
    private double currentMonthlySurplus = 0;
    private View lockOverlay;
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
    private CardView cardPending;

    private SavingsGoalAdapter goalsAdapter;
    private final List<SavingsGoal> goalsList = new ArrayList<>();
    private MaterialButtonToggleGroup toggleExpenseType;
    private boolean showingFixed = true;

    // ===== State =====
    private Calendar selectedMonth;
    private int lastRenderedYear = -1;
    private Calendar startMonth = null;
    private PagePermission currentPermission = PagePermission.VIEW_ONLY;
    // ===== Firebase =====
    private FirebaseFirestore db;
    private String groupId;
    private Calendar firstDataMonth = null;
    private RecyclerView rvGoalAlerts;
    private GoalAlertAdapter alertAdapter;
    private List<GoalAlert> goalAlerts = new ArrayList<>();
    private LinearLayout layoutExpensesContent;
    private ImageView imgExpandCategories;

    private LinearLayout layoutGoalsContent;
    private ImageView imgGoalsArrow;

    private ImageView imgPendingArrow;

    private ImageView imgGoalAlertsArrow;
    // Top goal alerts (האדום העליון)
    private LinearLayout layoutGoalAlertsContentTop;
    private ImageView imgGoalAlertsArrowTop;

    // Pending content wrapper
    private LinearLayout layoutPendingContent;

    // Headers
    private View headerGoalAlertsTop;
    private View headerPending;
    private View headerExpenses;
    private View headerGoals;

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

    private static final Map<String, Integer> CATEGORY_COLORS = new HashMap<>();

    static {

        // ===== קבועות – עמוקות יותר =====
        CATEGORY_COLORS.put("expense_communication", Color.parseColor("#00ACC1")); // Cyan חזק
        CATEGORY_COLORS.put("expense_housing",       Color.parseColor("#3949AB")); // Indigo עמוק
        CATEGORY_COLORS.put("expense_kids",          Color.parseColor("#FB8C00")); // Orange חד
        CATEGORY_COLORS.put("expense_insurance",     Color.parseColor("#E53935")); // Red חזק
        CATEGORY_COLORS.put("expense_transport",     Color.parseColor("#00897B")); // Teal עמוק
        CATEGORY_COLORS.put("expense_finance",       Color.parseColor("#FDD835")); // Yellow חזק
        CATEGORY_COLORS.put("expense_savings",       Color.parseColor("#43A047")); // Green חד
        CATEGORY_COLORS.put("expense_other_fixed",   Color.parseColor("#546E7A")); // Blue Grey

        // ===== משתנות – קצת יותר חיות אבל רגועות =====
        CATEGORY_COLORS.put("expense_food",      Color.parseColor("#1E88E5")); // Blue חי
        CATEGORY_COLORS.put("expense_health",    Color.parseColor("#8E24AA")); // Purple חזק
        CATEGORY_COLORS.put("expense_leisure",   Color.parseColor("#FF7043")); // Coral
        CATEGORY_COLORS.put("expense_personal",  Color.parseColor("#D81B60")); // Pink חד
        CATEGORY_COLORS.put("expense_pets",      Color.parseColor("#7CB342")); // Lime ירוק
        CATEGORY_COLORS.put("expense_home_misc", Color.parseColor("#5E35B1")); // Violet עמוק
        CATEGORY_COLORS.put("expense_one_time",  Color.parseColor("#00BFA5")); // Aqua

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finance);
        db = FirebaseFirestore.getInstance();

        // ===== GROUP =====
        SharedPreferences prefs =
                getSharedPreferences("app_prefs", MODE_PRIVATE);

        groupId = prefs.getString("group_id", null);
        String userId = prefs.getString("user_id", null);   // ✅ זה היה חסר
        String role = prefs.getString("role", null);

        AppSession.setUserId(userId);
        AppSession.setGroupId(groupId);
        AppSession.setUserRole(role);

        Log.e("ROLE_DEBUG", "role=" + role);
        Log.e("USER_DEBUG", "userId=" + userId);

        Log.e("ROLE_DEBUG", "role=" + role);
        bindViews();
        BlurView blurView = findViewById(R.id.lockOverlay);
        View decorView = getWindow().getDecorView();
        ViewGroup rootView = (ViewGroup) decorView.findViewById(android.R.id.content);

        blurView.setupWith(rootView)
                .setBlurRadius(20f);
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(Finance.this, FinanceSetupActivity.class);
            intent.putExtra("mode", "edit");
            startActivity(intent);
        });


        Map<String, ?> all = prefs.getAll();
        for (Map.Entry<String, ?> entry : all.entrySet()) {
            Log.e("PREF_DEBUG", entry.getKey() + " = " + entry.getValue());
        }

        if (groupId == null) {
            finish();
            return;
        }

        // ======================================
        // בדיקת Setup רק להורה
        // ======================================
        if ("parent".equalsIgnoreCase(AppSession.getUserRole())) {
            db.collection("groups")
                    .document(groupId)
                    .collection("finance_settings")
                    .document("main")
                    .get()
                    .addOnSuccessListener(doc -> {

                        // אם אין הגדרות → פתיחת Setup
                        if (!doc.exists() || doc.get("startMonth") == null) {

                            startActivity(new Intent(Finance.this, FinanceSetupActivity.class));
                            finish();
                            return;
                        }

                        // יש הגדרות → ממשיכים למסך
                        initFinanceScreen();
                    });

        } else {

            // משתמש ילד → נכנס ישר למסך
            initFinanceScreen();
        }
    }


    private void initFinanceScreen() {

        resolvePermissionFromServer(
                AppPage.FINANCE,
                groupId,
                AppSession.getUserId(),
                permission -> {

                    Log.e("PERMISSION_DEBUG", "Finance permission = " + permission);

                    currentPermission = permission;

                    // 🔒 אין הרשאת צפייה
                    if (permission == PagePermission.LOCKED || permission == null){

                        lockOverlay.setVisibility(View.VISIBLE);
                        lockOverlay.setClickable(true);
                        lockOverlay.setFocusable(true);

                        btnAddTransaction.setVisibility(View.GONE);
                        btnAddGoal.setVisibility(View.GONE);
                        btnSettings.setVisibility(View.GONE);

                        btnBack.setEnabled(false);
                        btnPrevMonth.setEnabled(false);
                        btnNextMonth.setEnabled(false);

                        return;
                    }

                    // אחרת המסך פתוח
                    lockOverlay.setVisibility(View.GONE);
                    lockOverlay.setClickable(false);
                    lockOverlay.setFocusable(false);
                    switch (permission) {

                        case VIEW_ONLY:

                            btnAddTransaction.setVisibility(View.GONE);
                            btnAddGoal.setVisibility(View.GONE);
                            btnSettings.setVisibility(View.GONE);

                            if (headerPending != null)
                                headerPending.setVisibility(View.GONE);

                            if (layoutPendingContent != null)
                                layoutPendingContent.setVisibility(View.GONE);

                            if (headerGoalAlertsTop != null)
                                headerGoalAlertsTop.setVisibility(View.GONE);

                            if (layoutGoalAlertsContentTop != null)
                                layoutGoalAlertsContentTop.setVisibility(View.GONE);

                            break;

                        case ADD_ONLY:

                            btnAddTransaction.setVisibility(View.VISIBLE);
                            btnAddGoal.setVisibility(View.GONE);
                            btnSettings.setVisibility(View.GONE);

                            if (headerPending != null)
                                headerPending.setVisibility(View.GONE);

                            if (layoutPendingContent != null)
                                layoutPendingContent.setVisibility(View.GONE);

                            if (headerGoalAlertsTop != null)
                                headerGoalAlertsTop.setVisibility(View.GONE);

                            if (layoutGoalAlertsContentTop != null)
                                layoutGoalAlertsContentTop.setVisibility(View.GONE);

                            break;

                        case ADD_EDIT:
                        case FULL_ACCESS:

                            btnAddTransaction.setVisibility(View.VISIBLE);
                            btnAddGoal.setVisibility(View.VISIBLE);
                            btnSettings.setVisibility(View.VISIBLE);

                            if (headerPending != null)
                                headerPending.setVisibility(View.VISIBLE);

                            if (layoutPendingContent != null)
                                layoutPendingContent.setVisibility(View.VISIBLE);

                            if (headerGoalAlertsTop != null)
                                headerGoalAlertsTop.setVisibility(View.VISIBLE);

                            if (layoutGoalAlertsContentTop != null)
                                layoutGoalAlertsContentTop.setVisibility(View.VISIBLE);

                            break;
                    }

                    // ===== טעינת המסך =====

                    setupMonth();
                    setupListeners();
                    setupGoals();

                    if (permission == PagePermission.ADD_EDIT ||
                            permission == PagePermission.FULL_ACCESS) {

                        setupPendingApprovals();
                        loadPendingApprovals();
                    }

                    loadStartMonth();
                    loadMonthData();
                    loadYearBarChart();
                    loadSavingsGoals();
                }
        );
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
        lockOverlay = findViewById(R.id.lockOverlay);
        tvSelectedMonth = findViewById(R.id.tvSelectedMonth);
        tvIncomeMonth = findViewById(R.id.tvIncomeMonth);
        tvExpenseMonth = findViewById(R.id.tvExpenseMonth);
        tvBalanceMonth = findViewById(R.id.tvBalanceMonth);
        rvCategoryExpenses = findViewById(R.id.rvCategoryExpenses);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setAutoMeasureEnabled(true);
        rvCategoryExpenses.setLayoutManager(lm);
        cardPending = findViewById(R.id.cardPending);
        rvGoals = findViewById(R.id.rvGoals);
        rvGoals.setLayoutManager(new LinearLayoutManager(this));
        rvCategoryExpenses.setNestedScrollingEnabled(false);
        rvCategoryExpenses.setHasFixedSize(false);
        rvCategoryExpenses.setItemViewCacheSize(50);
        layoutExpensesContent = findViewById(R.id.layoutExpensesContent);
        imgExpandCategories = findViewById(R.id.imgExpandCategories);

        layoutGoalsContent = findViewById(R.id.layoutGoalsContent);


        imgPendingArrow = findViewById(R.id.imgPendingArrow);

        rvGoalAlerts = findViewById(R.id.rvGoalAlerts);
// ===== Top Goal Alerts =====
        headerGoalAlertsTop = findViewById(R.id.headerGoalAlertsTop);
        layoutGoalAlertsContentTop = findViewById(R.id.layoutGoalAlertsContentTop);
        imgGoalAlertsArrowTop = findViewById(R.id.imgGoalAlertsArrowTop);

// ===== Pending =====
        headerPending = findViewById(R.id.headerPending);
        layoutPendingContent = findViewById(R.id.layoutPendingContent);
        imgPendingArrow = findViewById(R.id.imgPendingArrow);

// ===== Expenses =====
        headerExpenses = findViewById(R.id.headerExpenses);

// ===== Goals =====
        headerGoals = findViewById(R.id.headerGoals);
        imgGoalsArrow = findViewById(R.id.imgGoalAlertsArrow); // זה החץ הירוק

        pieExpenses = findViewById(R.id.pieFixedExpenses);
        barYear = findViewById(R.id.barMonthlySummary);
        rvGoalAlerts = findViewById(R.id.rvGoalAlerts);

        if (rvGoalAlerts != null) {

            rvGoalAlerts.setLayoutManager(new LinearLayoutManager(this));
            rvGoalAlerts.setNestedScrollingEnabled(false);

            // 🔥 יצירת adapter לפני שימוש
            alertAdapter = new GoalAlertAdapter(goalAlerts);

            alertAdapter.setOnAlertActionListener(alert -> {
                SavingsGoal goal = alert.getGoal();
                if (goal != null) {
                    addMoneyToGoal(goal);
                }
            });

            rvGoalAlerts.setAdapter(alertAdapter);
        }

        toggleExpenseType = findViewById(R.id.toggleExpenseType);


        // ===== Pending approvals =====
        tvPendingTitle = findViewById(R.id.tvPendingTitle);
        rvPendingApprovals = findViewById(R.id.rvPendingApprovals);

        if (rvPendingApprovals != null) {
            rvPendingApprovals.setLayoutManager(new LinearLayoutManager(this));
            rvPendingApprovals.setNestedScrollingEnabled(false);
        }


    }
    private void generateGoalAlerts(double monthBalance) {

        goalAlerts.clear();

        if (goalsList == null || goalsList.isEmpty() || rvGoalAlerts == null) {
            rvGoalAlerts.setVisibility(View.GONE);
            alertAdapter.notifyDataSetChanged();
            return;
        }

        Date today = new Date();

        for (SavingsGoal g : goalsList) {

            // ❌ בלי דדליין – אין התראות בכלל
            if (g.getDeadline() == null) continue;

            // ❌ לא היום המתאים
            if (!shouldShowProgressAlertToday(g)) continue;

            double timeProgress = g.getTimeProgress();
            double moneyProgress = g.getMoneyProgress();

            GoalAlert.AlertType type;
            String title;
            String message;

            if (moneyProgress >= timeProgress) {

                type = GoalAlert.AlertType.SUCCESS;
                title = "✅ מצב מצוין - " + g.getTitle();
                message = "הקצב תקין ואפילו מעל הנדרש";

            } else if (moneyProgress >= timeProgress * 0.7) {

                type = GoalAlert.AlertType.WARNING;
                title = "⚠️ מעט מאחור - " + g.getTitle();
                message = "כדאי לחזק מעט את ההפקדות";

            } else {

                type = GoalAlert.AlertType.URGENT;
                title = "🚨 המטרה בסיכון - " + g.getTitle();
                message = "ההתקדמות נמוכה ביחס לזמן שעבר";
            }

            goalAlerts.add(new GoalAlert(
                    g,
                    title,
                    message,
                    "בדוק מטרה",
                    type
            ));

            // 🔥 עדכון תאריך הצגה
            db.collection("groups")
                    .document(groupId)
                    .collection("savings_goals")
                    .document(g.getGoalId())
                    .update("lastProgressAlertDate", today);
        }

        if (currentPermission == PagePermission.ADD_EDIT ||
                currentPermission == PagePermission.FULL_ACCESS) {

            rvGoalAlerts.setVisibility(goalAlerts.isEmpty() ? View.GONE : View.VISIBLE);

        } else {

            rvGoalAlerts.setVisibility(View.GONE);
        }
        alertAdapter.notifyDataSetChanged();
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

        int newYear = selectedMonth.get(Calendar.YEAR);
        if (newYear != lastRenderedYear) {
            lastRenderedYear = newYear;
            loadYearBarChart();
        }
    }



    private void updateMonthLabel() {
        tvSelectedMonth.setText(
                android.text.format.DateFormat.format("MMMM yyyy", selectedMonth)
        );
    }

    // ================= Listeners =================
    private void setupListeners() {

        btnBack.setOnClickListener(v -> finish());

        btnAddTransaction.setOnClickListener(v ->
                startActivity(new Intent(this, Add_Transaction.class)));

        btnAddGoal.setOnClickListener(v ->
                startActivity(new Intent(this, AddGoalActivity.class)));

        btnPrevMonth.setOnClickListener(v -> changeMonth(-1));
        btnNextMonth.setOnClickListener(v -> changeMonth(1));

        toggleExpenseType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            showingFixed = checkedId == R.id.btnFixed;

            loadCategoryList(showingFixed);
            loadExpensesLive();
            loadSummaryLive();
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
                    resolveColorForCategory(cat.getId()),
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

    private int resolveColorForCategory(String categoryId) {
        Integer color = CATEGORY_COLORS.get(categoryId);
        return color != null ? color : Color.parseColor("#CBD5E1");
    }



    // ================= Summary =================
        private void loadSummaryLive() {

            db.collection("groups")
                    .document(groupId)
                    .collection("finance_flow_items")
                    .whereEqualTo("enabled", true)
                    .get()
                    .addOnSuccessListener(snapshot -> {

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

                            if (cat.startsWith("income_")) {

                                income += v;

                                Log.e("INCOME_DEBUG",
                                        "FOUND INCOME -> title=" + doc.getString("title") +
                                                " amountRaw=" + amount +
                                                " valueForMonth=" + v +
                                                " freq=" + freq
                                );
                            }

                            else if (cat.startsWith("expense_")) {
                                expense += v;
                            }
                        }

                        tvIncomeMonth.setText("₪" + format(income));
                        tvExpenseMonth.setText("₪" + format(expense));
                        tvBalanceMonth.setText("₪" + format(income - expense));

                        double monthlySurplus = income - expense;
                        currentMonthlySurplus = monthlySurplus;

                        double monthBalance = income - expense;
                        generateGoalAlerts(monthBalance);


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

        final double totalSum = sum;

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        for (Map.Entry<String, Double> entry : totals.entrySet()) {

            String categoryId = entry.getKey();
            double value = entry.getValue();

            if (value <= 0) continue;

            entries.add(new PieEntry((float) value, ""));

            // 🔥 צבע קבוע מתוך המפה שלך
            Integer color = CATEGORY_COLORS.get(categoryId);
            if (color != null) {
                colors.add(color);
            } else {
                colors.add(Color.parseColor("#CBD5E1")); // fallback עדין
            }
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





    private void loadYearBarChart() {

        final int year = selectedMonth.get(Calendar.YEAR);

        final double[] income = new double[12];
        final double[] expense = new double[12];

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
                        if (date == null) continue;

                        Calendar c = Calendar.getInstance();
                        c.setTime(date);

                        if (c.get(Calendar.YEAR) != year) continue;

                        int monthIndex = c.get(Calendar.MONTH); // 0-11

                        if (cat.startsWith("income_")) {
                            income[monthIndex] += amount;
                        } else if (cat.startsWith("expense_")) {
                            expense[monthIndex] += amount;
                        }
                    }

                    renderYearBar(income, expense);
                });
    }


    private void renderYearBar(double[] income, double[] expense) {

        List<BarEntry> incomeEntries = new ArrayList<>();
        List<BarEntry> expenseEntries = new ArrayList<>();

        // תמיד 12 חודשים
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

        // הסתרת 0 מעל עמודות
        ValueFormatter hideZeroFormatter = new ValueFormatter() {
            @Override
            public String getBarLabel(BarEntry barEntry) {
                return barEntry.getY() == 0f ? "" : String.valueOf((int) barEntry.getY());
            }
        };

        dsIncome.setValueFormatter(hideZeroFormatter);
        dsExpense.setValueFormatter(hideZeroFormatter);

        BarData data = new BarData(dsIncome, dsExpense);

        float barWidth = 0.35f;
        float barSpace = 0.05f;
        float groupSpace = 0.25f;

        data.setBarWidth(barWidth);

        barYear.setData(data);

        // ===== חודשים =====
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

        // Grouping
        barYear.groupBars(0f, groupSpace, barSpace);

        // ===== תיקון ציר Y =====
        float maxValue = 0f;

        for (BarEntry e : incomeEntries) {
            if (e.getY() > maxValue) maxValue = e.getY();
        }

        for (BarEntry e : expenseEntries) {
            if (e.getY() > maxValue) maxValue = e.getY();
        }

        if (maxValue == 0f) {
            barYear.getAxisLeft().setAxisMinimum(0f);
            barYear.getAxisLeft().setAxisMaximum(1f);
        } else {
            barYear.getAxisLeft().setAxisMinimum(0f);
            barYear.getAxisLeft().setAxisMaximum(maxValue * 1.2f);
        }

        barYear.getAxisRight().setEnabled(false);

        barYear.getDescription().setEnabled(false);
        barYear.setFitBars(true);

        barYear.invalidate();
    }



    private String getMonthShortName(int month) {

        String[] months = {
                "ינו", "פבר", "מרץ", "אפר",
                "מאי", "יונ", "יול", "אוג",
                "ספט", "אוק", "נוב", "דצ"
        };

        return months[month];
    }


    // ================= Goals =================
    private void setupGoals() {

        Log.e("DELETE_DEBUG", "setupGoals CALLED");

        goalsAdapter = new SavingsGoalAdapter(goalsList);

        goalsAdapter.setOnGoalActionListener(new SavingsGoalAdapter.OnGoalActionListener() {

            @Override
            public void onAddAmountClicked(SavingsGoal goal) {
                Log.e("DELETE_DEBUG", "ADD CLICKED");
                showGoalActionSheet(goal);
            }

            @Override
            public void onDeleteClicked(SavingsGoal goal) {
                Log.e("DELETE_DEBUG", "DELETE CLICKED FROM ADAPTER");
                deleteGoal(goal.getGoalId());
            }

        });

        rvGoals.setAdapter(goalsAdapter);
    }

    private void setupToggle(View header,
                             View content,
                             ImageView arrow) {

        header.setOnClickListener(v -> {

            if (content.getVisibility() == View.VISIBLE) {

                content.setVisibility(View.GONE);
                arrow.animate().rotation(0f).setDuration(200).start();

            } else {

                content.setVisibility(View.VISIBLE);
                arrow.animate().rotation(180f).setDuration(200).start();
            }
        });
    }







    private void loadSavingsGoals() {

        db.collection("groups")
                .document(groupId)
                .collection("savings_goals")
                .get()
                .addOnSuccessListener(snapshot -> {

                    goalsList.clear();

                    for (QueryDocumentSnapshot doc : snapshot) {

                        String title = doc.getString("title");

                        Long target = doc.getLong("targetAmount");
                        Long current = doc.getLong("currentAmount");

                        String modeStr = doc.getString("goalMode");
                        String typeStr = doc.getString("goalType");
                        String periodStr = doc.getString("periodType");

                        String linkedCategoryId = doc.getString("linkedCategoryId");

                        Date deadline = safeReadDeadline(doc.get("deadline"));
                        Date createdAt = safeReadDeadline(doc.get("createdAt"));
                        Date lastAlert = safeReadDeadline(doc.get("lastProgressAlertDate"));

                        Boolean handled = doc.getBoolean("successHandled");

                        SavingsGoal goal = new SavingsGoal(
                                doc.getId(),
                                title,
                                target != null ? target.intValue() : 0,
                                current != null ? current.intValue() : 0,
                                modeStr != null ? SavingsGoal.GoalMode.valueOf(modeStr) : SavingsGoal.GoalMode.SAVE,
                                typeStr != null ? SavingsGoal.GoalType.valueOf(typeStr) : SavingsGoal.GoalType.TARGET,
                                periodStr != null ? SavingsGoal.PeriodType.valueOf(periodStr) : SavingsGoal.PeriodType.NONE,
                                linkedCategoryId,
                                deadline
                        );

                        goal.setCreatedAt(createdAt);
                        goal.setLastProgressAlertDate(lastAlert);
                        goal.setSuccessHandled(handled != null && handled);

                        goalsList.add(goal);
                    }



                    goalsAdapter.notifyDataSetChanged();
                    generateGoalAlerts(currentMonthlySurplus);

                    for (SavingsGoal g : goalsList) {
                        if (shouldAskPeriodDecision(g)) {
                            showPeriodDecisionDialog(g);
                            break; // שלא יפתח כמה דיאלוגים יחד
                        }
                    }

                });
    }

    private void deleteGoal(String goalId) {

        Log.e("DELETE_DEBUG", "INSIDE deleteGoal with id = " + goalId);

        if (groupId == null || goalId == null) {
            Log.e("DELETE_DEBUG", "groupId or goalId is null");
            Toast.makeText(this, "שגיאה במחיקה", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("groups")
                .document(groupId)
                .collection("savings_goals")
                .document(goalId)
                .delete()
                .addOnSuccessListener(unused -> {

                    Log.e("DELETE_DEBUG", "DELETE SUCCESS");

                    Toast.makeText(this,
                            "המטרה נמחקה בהצלחה",
                            Toast.LENGTH_SHORT).show();

                    // רענון רשימת מטרות
                    loadSavingsGoals();

                    // רענון סיכומים והתראות
                    loadSummaryLive();
                })
                .addOnFailureListener(e -> {

                    Log.e("DELETE_DEBUG", "DELETE FAILED", e);

                    Toast.makeText(this,
                            "שגיאה במחיקה",
                            Toast.LENGTH_SHORT).show();
                });
    }


    private void confirmDeleteGoal(SavingsGoal goal) {

        if (goal == null) return;

        new AlertDialog.Builder(this)
                .setTitle("⚠️ מחיקת מטרה")
                .setMessage("האם את בטוחה שברצונך למחוק את המטרה:\n\n\""
                        + goal.getTitle() + "\" ?\n\nלא ניתן לשחזר פעולה זו.")
                .setPositiveButton("מחק", (dialog, which) -> {
                    deleteGoal(goal.getGoalId());
                })
                .setNegativeButton("ביטול", null)
                .show();
    }



    private void showGoalSuccessDialog(SavingsGoal goal) {

        new AlertDialog.Builder(this)
                .setTitle("🎉 כל הכבוד!")
                .setMessage("המטרה \"" + goal.getTitle() + "\" הושגה.\n\nמה תרצי לעשות?")

                .setPositiveButton("להמשיך לחסוך", (d, which) -> {
                    markGoalSuccessHandled(goal);
                })

                .setNeutralButton("להגדיל יעד", (d, which) -> {
                    increaseGoalTarget(goal);
                    markGoalSuccessHandled(goal);
                })

                .setNegativeButton("🗑 מחק מטרה", (d, which) -> {
                    deleteGoal(goal.getGoalId());
                })

                .setCancelable(false)
                .show();
    }


    private void markGoalSuccessHandled(SavingsGoal goal) {

        goal.setSuccessHandled(true);

        db.collection("groups")
                .document(groupId)
                .collection("savings_goals")
                .document(goal.getGoalId())
                .update("successHandled", true);
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

    private boolean shouldAskPeriodDecision(SavingsGoal g) {

        if (g.getGoalType() != SavingsGoal.GoalType.PERIOD) {
            return false;
        }

        if (g.getPeriodType() == SavingsGoal.PeriodType.NONE) {
            return false;
        }

        Calendar now = Calendar.getInstance();

        Calendar lastDecision = Calendar.getInstance();
        if (g.getLastPeriodDecision() != null) {
            lastDecision.setTime(g.getLastPeriodDecision());
        } else {
            lastDecision.set(Calendar.YEAR, 1900);
        }

        // חודשי
        if (g.getPeriodType() == SavingsGoal.PeriodType.MONTHLY) {

            return now.get(Calendar.MONTH) != lastDecision.get(Calendar.MONTH) ||
                    now.get(Calendar.YEAR) != lastDecision.get(Calendar.YEAR);
        }

        // שנתי
        if (g.getPeriodType() == SavingsGoal.PeriodType.YEARLY) {

            return now.get(Calendar.YEAR) != lastDecision.get(Calendar.YEAR);
        }

        return false;
    }

    private void showPeriodDecisionDialog(SavingsGoal g) {

        String message;

        if (g.getGoalMode() == SavingsGoal.GoalMode.LIMIT) {

            message =
                    "התקופה הסתיימה.\n\n" +
                            "הוצאת ₪" + format(g.getCurrentAmount()) +
                            " מתוך ₪" + format(g.getTargetAmount()) +
                            ".\n\nמה תרצי לעשות?";
        } else {

            message =
                    "התקופה הסתיימה.\n\n" +
                            "חסכת ₪" + format(g.getCurrentAmount()) +
                            " מתוך ₪" + format(g.getTargetAmount()) +
                            ".\n\nאיך להמשיך?";
        }

        new AlertDialog.Builder(this)
                .setTitle("סיכום תקופה - " + g.getTitle())
                .setMessage(message)

                .setPositiveButton("להמשיך כרגיל", (d, which) -> {
                    savePeriodDecision(g);
                })

                .setNeutralButton("לאפס סכום", (d, which) -> {
                    resetGoalAmount(g);
                    savePeriodDecision(g);
                })

                .setNegativeButton("להגדיל יעד", (d, which) -> {
                    increaseGoalTarget(g);
                    savePeriodDecision(g);
                })

                .setCancelable(false)
                .show();
    }

    private void resetGoalAmount(SavingsGoal g) {

        db.collection("groups")
                .document(groupId)
                .collection("savings_goals")
                .whereEqualTo("title", g.getTitle())
                .get()
                .addOnSuccessListener(snapshot -> {

                    for (QueryDocumentSnapshot doc : snapshot) {

                        doc.getReference().update(
                                "currentAmount", 0
                        );
                    }

                    loadSavingsGoals(); // רענון
                });
    }
    private void showGoalActionSheet(SavingsGoal goal) {

        View view = getLayoutInflater()
                .inflate(R.layout.bottom_sheet_goal_action, null);

        com.google.android.material.bottomsheet.BottomSheetDialog dialog =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);

        dialog.setContentView(view);

        TextView tvTitle = view.findViewById(R.id.tvSheetTitle);
        Button btnAddMoney = view.findViewById(R.id.btnAddMoney);
        Button btnIncrease = view.findViewById(R.id.btnIncreaseTarget);
        Button btnReset = view.findViewById(R.id.btnResetGoal);
        Button btnDelete = view.findViewById(R.id.btnDeleteGoal);

        tvTitle.setText("ניהול מטרה: " + goal.getTitle());

        btnAddMoney.setOnClickListener(v -> {
            dialog.dismiss();
            addMoneyToGoal(goal);
        });

        btnIncrease.setOnClickListener(v -> {
            dialog.dismiss();
            increaseGoalTarget(goal);
        });

        btnReset.setOnClickListener(v -> {
            dialog.dismiss();
            resetGoalAmount(goal);
        });

        btnDelete.setOnClickListener(v -> {
            dialog.dismiss();
            confirmDeleteGoal(goal);
        });


        dialog.show();
    }


    private void addMoneyToGoal(SavingsGoal goal) {

        if (goal == null) return;

        EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setHint("הכנס סכום להוספה");

        new AlertDialog.Builder(this)
                .setTitle("הוספת סכום - " + goal.getTitle())
                .setView(input)
                .setPositiveButton("אישור", (dialog, which) -> {

                    String value = input.getText().toString().trim();

                    if (value.isEmpty()) {
                        Toast.makeText(this, "נא להזין סכום", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int amountToAdd = Integer.parseInt(value);

                    if (amountToAdd <= 0) {
                        Toast.makeText(this, "סכום לא תקין", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int newAmount = goal.getCurrentAmount() + amountToAdd;

                    db.collection("groups")
                            .document(groupId)
                            .collection("savings_goals")
                            .document(goal.getGoalId())
                            .update("currentAmount", newAmount)
                            .addOnSuccessListener(unused -> {

                                Toast.makeText(this,
                                        "נוספו ₪" + amountToAdd,
                                        Toast.LENGTH_SHORT).show();

                                loadSavingsGoals();
                                loadSummaryLive();
                            });

                })
                .setNegativeButton("ביטול", null)
                .show();
    }


    private int extractAmountFromAlert(SavingsGoal goal) {

        // במקרה שלנו את מציגה משהו כמו "הוסף ₪2,500"
        // אז פשוט נחשב לפי יתרה חכמה

        double monthBalance = currentMonthlySurplus;

        double remaining = goal.getRemainingAmount();

        double suggested = Math.min(monthBalance * 0.25, remaining * 0.5);

        return (int) Math.round(suggested);
    }




    private void increaseGoalTarget(SavingsGoal goal) {

        EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setHint("הכנס יעד חדש");
        input.setText(String.valueOf(goal.getTargetAmount())); // מציג יעד נוכחי כברירת מחדל

        new AlertDialog.Builder(this)
                .setTitle("עדכון יעד - " + goal.getTitle())
                .setView(input)
                .setPositiveButton("שמור", (dialog, which) -> {

                    String value = input.getText().toString().trim();

                    if (value.isEmpty()) {
                        Toast.makeText(this, "נא להזין סכום יעד", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int newTarget = Integer.parseInt(value);

                    if (newTarget <= 0) {
                        Toast.makeText(this,
                                "היעד חייב להיות גדול מ־0",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    db.collection("groups")
                            .document(groupId)
                            .collection("savings_goals")
                            .document(goal.getGoalId())
                            .update("targetAmount", newTarget)
                            .addOnSuccessListener(unused -> {

                                Toast.makeText(this,
                                        "היעד עודכן בהצלחה",
                                        Toast.LENGTH_SHORT).show();

                                loadSavingsGoals();
                                loadSummaryLive();
                            });

                })
                .setNegativeButton("ביטול", null)
                .show();
    }



    private void savePeriodDecision(SavingsGoal g) {

        Date now = new Date();
        g.setLastPeriodDecision(now);

        db.collection("groups")
                .document(groupId)
                .collection("savings_goals")
                .document(g.getGoalId())   // ✅ התיקון
                .update("lastPeriodDecision", now);
    }





    // ================= Utils =================
    private Date resolveItemDate(QueryDocumentSnapshot doc) {

        Date txDate = doc.getDate("transactionDate");
        if (txDate != null) return txDate;

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

    private boolean isSameDay(Date d1, Date d2) {

        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();

        c1.setTime(d1);
        c2.setTime(d2);

        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }



    private boolean shouldShowProgressAlertToday(SavingsGoal g) {

        if (g.getDeadline() == null) return false;

        Date start = g.getCreatedAt();
        Date end = g.getDeadline();

        if (start == null || end == null) return false;

        Date nowDate = new Date();

        long total = end.getTime() - start.getTime();
        if (total <= 0) return false;

        long half = total / 2;
        Date halfDate = new Date(start.getTime() + half);

        // 🎯 בדיוק ביום מחצית הזמן
        if (isSameDay(nowDate, halfDate)) {
            return true;
        }

        // 🎯 אחרי מחצית הזמן – פעם בחודש
        if (nowDate.after(halfDate)) {

            Date lastShown = g.getLastProgressAlertDate();

            if (lastShown == null) return true;

            Calendar now = Calendar.getInstance();
            Calendar last = Calendar.getInstance();

            now.setTime(nowDate);
            last.setTime(lastShown);

            return now.get(Calendar.MONTH) != last.get(Calendar.MONTH)
                    || now.get(Calendar.YEAR) != last.get(Calendar.YEAR);
        }

        return false;
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
    private ListenerRegistration pendingListener;

    private void loadPendingApprovals() {

        if (groupId == null) return;

        // אם כבר יש מאזין – מבטלים
        if (pendingListener != null) {
            pendingListener.remove();
        }

        pendingListener = db.collection("groups")
                .document(groupId)
                .collection("finance_flow_items")
                .whereEqualTo("enabled", true)
                .addSnapshotListener((snapshot, e) -> {

                    if (e != null || snapshot == null) return;

                    pendingItems.clear();

                    for (QueryDocumentSnapshot doc : snapshot) {

                        Timestamp lastApprovedAt = doc.getTimestamp("lastApprovedAt");
                        if (lastApprovedAt != null) continue;

                        String freq = doc.getString("frequency");
                        if (freq == null || !freq.contains("חודשי")) continue;

                        String categoryId = doc.getString("categoryId");
                        if (categoryId == null) continue;

                        if (!categoryId.startsWith("expense_")) continue;

                        String title = doc.getString("title");
                        if (title == null) title = "ללא כותרת";

                        Number num = (Number) doc.get("amount");
                        int amount = (num != null)
                                ? (int) Math.round(num.doubleValue())
                                : 0;

                        FlowItem item = new FlowItem(doc.getId(), categoryId, title);
                        item.setAmount(amount);
                        item.setFrequency(freq);
                        item.setLastApprovedAt(null);

                        pendingItems.add(item);
                    }

                    // עדכון כותרת
                    if (tvPendingTitle != null) {
                        tvPendingTitle.setText(
                                "יש לך חיובים לאישור (" + pendingItems.size() + ")"
                        );
                    }

                    if (cardPending != null) {

                        if (currentPermission == PagePermission.ADD_EDIT ||
                                currentPermission == PagePermission.FULL_ACCESS) {

                            cardPending.setVisibility(
                                    pendingItems.size() > 0
                                            ? View.VISIBLE
                                            : View.GONE
                            );

                        } else {

                            cardPending.setVisibility(View.GONE);
                        }
                    }

                    pendingAdapter.notifyDataSetChanged();
                });
    }




}
