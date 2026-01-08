package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalendarDayActivity extends AppCompatActivity {

    // SharedPreferences
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_GROUP_ID = "group_id";

    // UI
    private ImageButton btnBack;
    private ImageButton btnPrevDay;
    private ImageButton btnNextDay;
    private RecyclerView recyclerEvents;
    private FloatingActionButton fabAdd;
    private TextView tvEmptyState;
    private TextView tvSelectedDate;
    private TextView tvCalendarName; // 🆕 שם לוח שנה

    private CalendarView calendarView;

    // Data
    private final List<DocumentSnapshot> events = new ArrayList<>();
    private CalendarEventsAdapter adapter;
    private Calendar selectedCalendar;

    private final SimpleDateFormat dbFormat =
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_day);

        // ----- Bind Views -----
        btnBack = findViewById(R.id.btnBack);
        btnPrevDay = findViewById(R.id.btnPrevDay);
        btnNextDay = findViewById(R.id.btnNextDay);
        recyclerEvents = findViewById(R.id.recyclerEvents);
        fabAdd = findViewById(R.id.fabAdd);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        calendarView = findViewById(R.id.calendarView);

        // ✅ היה חסר
        tvCalendarName = findViewById(R.id.tvCalendarName);

        recyclerEvents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CalendarEventsAdapter(events);
        recyclerEvents.setAdapter(adapter);

        selectedCalendar = Calendar.getInstance();

        // ✅ סנכרון CalendarView ליום הנוכחי
        calendarView.setDate(selectedCalendar.getTimeInMillis(), false, true);

        // ----- Back -----
        btnBack.setOnClickListener(v -> finish());

        // ----- Add Event -----
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddCalendarEventActivity.class);
            intent.putExtra("date", dbFormat.format(selectedCalendar.getTime()));
            startActivityForResult(intent, 1001);
        });

        // ✅ לחיצה על יום בלוח → עדכון מיידי למטה
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedCalendar.set(year, month, dayOfMonth);
            updateSelectedDateText();
            loadEventsForSelectedDate();
        });

        // ----- Prev / Next Day -----
        btnPrevDay.setOnClickListener(v -> {
            selectedCalendar.add(Calendar.DAY_OF_MONTH, -1);
            calendarView.setDate(selectedCalendar.getTimeInMillis(), false, true);
            updateSelectedDateText();
            loadEventsForSelectedDate();
        });

        btnNextDay.setOnClickListener(v -> {
            selectedCalendar.add(Calendar.DAY_OF_MONTH, 1);
            calendarView.setDate(selectedCalendar.getTimeInMillis(), false, true);
            updateSelectedDateText();
            loadEventsForSelectedDate();
        });

        // 🆕 שם לוח
        loadCalendarName();

        // ----- Initial load -----
        updateSelectedDateText();
        loadEventsForSelectedDate();
    }

    private void loadCalendarName() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String groupId = prefs.getString(KEY_GROUP_ID, null);

        if (groupId == null) {
            tvCalendarName.setText("יומן");
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("calendars")
                .document(groupId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        tvCalendarName.setText(
                                !TextUtils.isEmpty(name) ? name : "יומן"
                        );
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == RESULT_OK) {
            loadEventsForSelectedDate();
        }
    }

    // ✅ רענון לפי היום שנבחר
    private void loadEventsForSelectedDate() {
        loadEventsForDate(dbFormat.format(selectedCalendar.getTime()));
    }

    // ----- Firestore -----
    private void loadEventsForDate(String dateKey) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String groupId = prefs.getString(KEY_GROUP_ID, null);

        if (groupId == null) {
            events.clear();
            adapter.notifyDataSetChanged();
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerEvents.setVisibility(View.GONE);
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("calendar_events")
                .whereEqualTo("groupId", groupId)
                .addSnapshotListener(this, (querySnapshot, error) -> {

                    if (error != null || querySnapshot == null) return;

                    events.clear();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        if (shouldEventAppearOnDate(doc, dateKey)) {
                            events.add(doc);
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (events.isEmpty()) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                        recyclerEvents.setVisibility(View.GONE);
                    } else {
                        tvEmptyState.setVisibility(View.GONE);
                        recyclerEvents.setVisibility(View.VISIBLE);
                    }
                });
    }

    // ===== שאר הקוד שלך – לא נגעתי =====

    private boolean shouldEventAppearOnDate(DocumentSnapshot doc, String selectedDateStr) {
        // אותו קוד בדיוק
        // (לא שיניתי שורה)
        try {
            String startDateStr = doc.getString("date");
            if (TextUtils.isEmpty(startDateStr)) return false;

            String endDateStr = doc.getString("endDate");
            String repeatType = doc.getString("repeatType");
            if (TextUtils.isEmpty(repeatType)) repeatType = "once";

            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            Calendar startDate = Calendar.getInstance();
            startDate.setTime(format.parse(startDateStr));

            Calendar selectedDate = Calendar.getInstance();
            selectedDate.setTime(format.parse(selectedDateStr));

            Calendar endDate = (Calendar) startDate.clone();
            if (!TextUtils.isEmpty(endDateStr)) {
                endDate.setTime(format.parse(endDateStr));
            }

            boolean isMultiDay = !sameDay(startDate, endDate);
            if (isMultiDay) {
                return !selectedDate.before(startDate) && !selectedDate.after(endDate);
            }

            if (selectedDate.before(startDate)) return false;

            switch (repeatType) {
                case "weekly":
                    return startDate.get(Calendar.DAY_OF_WEEK) == selectedDate.get(Calendar.DAY_OF_WEEK);
                case "monthly":
                    return startDate.get(Calendar.DAY_OF_MONTH) == selectedDate.get(Calendar.DAY_OF_MONTH);
                case "yearly":
                    return startDate.get(Calendar.DAY_OF_MONTH) == selectedDate.get(Calendar.DAY_OF_MONTH)
                            && startDate.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH);
                default:
                    return sameDay(startDate, selectedDate);
            }
        } catch (Exception e) {
            return false;
        }
    }

    private void updateSelectedDateText() {
        SimpleDateFormat displayFormat =
                new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvSelectedDate.setText(displayFormat.format(selectedCalendar.getTime()));
    }

    private boolean sameDay(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }
}
