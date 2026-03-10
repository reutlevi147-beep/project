package com.mycasa.app;

import static com.github.mikephil.charting.utils.Utils.formatNumber;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.view.View;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import com.mycasa.app.BaseActivity.PagePermission;
import com.mycasa.app.BaseActivity.AppPage;
public class Home extends BaseActivity {
    FirebaseFirestore db;
    String groupId;

    MaterialCardView cardTasksSummary;
    MaterialCardView cardShoppingSummary;
    MaterialCardView cardEventSummary;
    MaterialCardView cardBalance;
    MaterialCardView cardIncome;
    MaterialCardView cardExpense;

    LinearLayout layoutDailySummary;

    TextView tvTasksSummary;
    TextView tvShoppingSummary;
    TextView tvEventTitle;
    TextView tvEventTime;
    TextView tvIncome, tvExpense, tvBalance;
    TextView tvGrowth;
    LinearLayout layoutFinanceSection;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        db = FirebaseFirestore.getInstance();
        layoutDailySummary = findViewById(R.id.layoutDailySummary);
        tvIncome = findViewById(R.id.tvIncome);
        tvExpense = findViewById(R.id.tvExpense);
        tvBalance = findViewById(R.id.tvBalance);
        tvGrowth = findViewById(R.id.tvGrowth);
        layoutFinanceSection = findViewById(R.id.layoutFinanceSection);
        cardBalance = findViewById(R.id.cardBalance);
        cardIncome = findViewById(R.id.cardIncome);
        cardExpense = findViewById(R.id.cardExpense);

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);

        String userId = prefs.getString("user_id", null);
        groupId = prefs.getString("group_id", null);

        db.collection("groups")
                .document(groupId)
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {

                    String role = doc.getString("role");

                    prefs.edit().putString("role", role).apply();
                    AppSession.setUserRole(role);
                });

        String userRole = prefs.getString("role", null);
        String userName = prefs.getString("user_name", null);
        String familyName = prefs.getString("family_name", null);


        AppSession.setUserId(userId);
        AppSession.setGroupId(groupId);
        AppSession.setUserRole(userRole);
        resolvePermissionFromServer(
                AppPage.FINANCE,
                groupId,
                userId,
                permission -> {

                    if (permission == PagePermission.LOCKED) {

                        layoutFinanceSection.setVisibility(View.GONE);

                    } else {

                        layoutFinanceSection.setVisibility(View.VISIBLE);

                    }

                }
        );
        // =====================
        // שלום למשתמש (מעוצב)
        // =====================
        TextView tvHello = findViewById(R.id.tvHello);

        if (familyName != null && !familyName.isEmpty()) {

            String baseText = "שלום, משפחת ";
            String familyText =   familyName;
            String fullText = baseText + familyText;

            SpannableString spannable = new SpannableString(fullText);

            int start = baseText.length();
            int end = fullText.length();

            // הגדלת גודל השם
            spannable.setSpan(
                    new android.text.style.RelativeSizeSpan(1.3f),
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            // צבע שחור מלא
            spannable.setSpan(
                    new ForegroundColorSpan(android.graphics.Color.BLACK),
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            // הדגשה
            spannable.setSpan(
                    new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            tvHello.setText(spannable);

        } else if (userName != null && !userName.isEmpty()) {
            tvHello.setText("שלום, " + userName);
        } else {
            tvHello.setText("שלום");
        }

        // =====================
        // מעבר לפיננסים
        // =====================
        View.OnClickListener goToFinance = v ->
                startActivity(new Intent(Home.this, FinanceSetupActivity.class));

        cardIncome.setOnClickListener(goToFinance);
        cardExpense.setOnClickListener(goToFinance);
        cardBalance.setOnClickListener(goToFinance);

        // =====================
        // Daily summary
        // =====================
        cardTasksSummary = findViewById(R.id.cardTasksSummary);
        cardShoppingSummary = findViewById(R.id.cardShoppingSummary);
        cardEventSummary = findViewById(R.id.cardEventSummary);

        tvTasksSummary = findViewById(R.id.tvTasksSummary);
        tvShoppingSummary = findViewById(R.id.tvShoppingSummary);
        tvEventTitle = findViewById(R.id.tvEventTitle);
        tvEventTime = findViewById(R.id.tvEventTime);

        cardTasksSummary.setOnClickListener(v ->
                startActivity(new Intent(this, TasksActivity.class)));

        cardShoppingSummary.setOnClickListener(v ->
                startActivity(new Intent(this, ShoppingListActivity.class)));

        cardEventSummary.setOnClickListener(v ->
                startActivity(new Intent(this, CalendarDayActivity.class)));

        // =====================
        // Quick actions
        // =====================
        findViewById(R.id.quickShopping).setOnClickListener(v ->
                startActivity(new Intent(this, ShoppingListActivity.class)));

        findViewById(R.id.quickCalendar).setOnClickListener(v ->
                startActivity(new Intent(this, CalendarDayActivity.class)));

        findViewById(R.id.quickTasks).setOnClickListener(v ->
                startActivity(new Intent(this, TasksActivity.class)));

        findViewById(R.id.quickSettings).setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));

        // =====================
        // Bottom Navigation
        // =====================
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_home) return true;

            if (id == R.id.nav_calendar) {
                startActivity(new Intent(this, CalendarDayActivity.class));
                return true;
            }

            if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }

            if (id == R.id.nav_stats) {
                startActivity(new Intent(this, FinanceSetupActivity.class));
                return true;
            }

            return false;
        });

}

    @Override
    protected void onResume() {
        super.onResume();

        loadTasksSummary();
        loadShoppingSummary();
        loadClosestEvent();

        if(layoutFinanceSection.getVisibility() == View.VISIBLE){
            loadFinanceSummary();
        }
    }

    private void loadFinanceSummary() {

        if (groupId == null) return;

        db.collection("groups")
                .document(groupId)
                .collection("finance_flow_items")
                .get()
                .addOnSuccessListener(snapshot -> {

                    double income = 0;
                    double expense = 0;

                    for (QueryDocumentSnapshot doc : snapshot) {

                        Double amount = doc.getDouble("amount");
                        if (amount == null) continue;

                        String categoryId = doc.getString("categoryId");
                        if (categoryId == null) continue;

                        if (categoryId.startsWith("income_")) {
                            income += amount;
                        }
                        else if (categoryId.startsWith("expense_")) {
                            expense += amount;
                        }
                    }

                    double balance = income - expense;
                    double percent = 0;

                    if (expense > 0) {
                        percent = (balance / expense) * 100;
                    }

                    String sign = percent > 0 ? "+" : "";
                    tvGrowth.setText(sign + String.format(Locale.getDefault(), "%.0f%%", percent));

                    tvIncome.setText("₪" + formatNumber(income));
                    tvExpense.setText("₪" + formatNumber(expense));
                    tvBalance.setText("₪" + formatNumber(balance));


                    // ❗ צבע לא משתנה יותר
                    cardBalance.setBackgroundResource(R.drawable.bg_balance_indigo);
                });
    }


    private void updateBalanceColor(double balance) {

        if (balance < 0) {
            cardBalance.setBackgroundResource(R.drawable.bg_balance_negative);
        } else {
            cardBalance.setBackgroundResource(R.drawable.bg_balance_indigo);
        }
    }


    private double adjustAmountByFrequency(double amount, String frequency) {

        if (frequency == null) return amount;

        switch (frequency) {

            case "weekly":
                return amount * 4.33;

            case "yearly":
                return amount / 12.0;

            case "daily":
                return amount * 30;

            case "monthly":
            default:
                return amount;
        }
    }


    private String formatNumber(double value) {
        return String.format("%,.0f", value);
    }

    private void loadTasksSummary() {

        db.collection("groups")
                .document(groupId)
                .collection("home_tasks")
                .whereEqualTo("completed", false)
                .get()
                .addOnSuccessListener(snapshot -> {

                    int count = snapshot.size();

                    if (count > 0) {
                        layoutDailySummary.setVisibility(View.VISIBLE);
                        cardTasksSummary.setVisibility(View.VISIBLE);
                        tvTasksSummary.setText(count + " משימות פתוחות");
                    }
                });
    }



    private void loadShoppingSummary() {

        db.collection("groups")
                .document(groupId)
                .collection("shopping")
                .whereEqualTo("isPurchased", false)
                .get()
                .addOnSuccessListener(snapshot -> {

                    int count = snapshot.size();

                    if (count > 0) {
                        layoutDailySummary.setVisibility(View.VISIBLE);
                        cardShoppingSummary.setVisibility(View.VISIBLE);
                        tvShoppingSummary.setText(count + " מוצרים ברשימה");
                    }

                });
    }


    private void loadClosestEvent() {

        String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(new Date());

        String nowTime = new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(new Date());

        db.collection("groups")
                .document(groupId)
                .collection("calendar_events")
                .whereEqualTo("date", today)
                .get()
                .addOnSuccessListener(snapshot -> {

                    String closestTitle = null;
                    String closestStart = null;
                    String closestEnd = null;

                    for (QueryDocumentSnapshot doc : snapshot) {

                        String start = doc.getString("startTime");
                        String end = doc.getString("endTime");

                        if (start == null) continue;

                        if (start.compareTo(nowTime) >= 0 ||
                                (end != null && end.compareTo(nowTime) >= 0)) {

                            if (closestStart == null ||
                                    start.compareTo(closestStart) < 0) {

                                closestStart = start;
                                closestEnd = end;
                                closestTitle = doc.getString("title");
                            }
                        }
                    }

                    if (closestTitle != null) {

                        layoutDailySummary.setVisibility(View.VISIBLE);
                        cardEventSummary.setVisibility(View.VISIBLE);
                        tvEventTitle.setText(closestTitle);
                        tvEventTime.setText(closestStart + " - " + closestEnd);
                    }
                });
    }



}
