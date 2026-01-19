package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
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

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_GROUP_ID = "group_id";

    // UI
    private ImageButton btnBack, btnPrevMonth, btnNextMonth;
    private TextView tvMonthTitle, tvEmptyState;
    private RecyclerView recyclerMonth, recyclerEvents;
    private FloatingActionButton fabAdd;

    // Calendar
    private final List<Integer> monthDays = new ArrayList<>();
    private final List<Integer> daysWithEvents = new ArrayList<>();
    private CalendarMonthAdapter monthAdapter;
    private Calendar selectedCalendar;

    // Events
    private final List<DocumentSnapshot> events = new ArrayList<>();
    private CalendarEventsAdapter eventsAdapter;

    private final SimpleDateFormat dbFormat =
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_day);

        btnBack = findViewById(R.id.btnBack);
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        tvMonthTitle = findViewById(R.id.tvMonthTitle);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        recyclerMonth = findViewById(R.id.recyclerMonth);
        recyclerEvents = findViewById(R.id.recyclerEvents);
        fabAdd = findViewById(R.id.fabAdd);

        selectedCalendar = Calendar.getInstance();

        btnBack.setOnClickListener(v -> finish());

        btnPrevMonth.setOnClickListener(v -> {
            selectedCalendar.add(Calendar.MONTH, -1);
            selectedCalendar.set(Calendar.DAY_OF_MONTH, 1);
            updateMonthTitle();
            buildMonthCalendar();
            monthAdapter.setSelectedDay(1);
            loadEventsForSelectedDate();
        });

        btnNextMonth.setOnClickListener(v -> {
            selectedCalendar.add(Calendar.MONTH, 1);
            selectedCalendar.set(Calendar.DAY_OF_MONTH, 1);
            updateMonthTitle();
            buildMonthCalendar();
            loadEventsForSelectedDate();
        });

        recyclerMonth.setLayoutManager(new GridLayoutManager(this, 7));
        monthAdapter = new CalendarMonthAdapter(
                monthDays,
                daysWithEvents,
                selectedCalendar.get(Calendar.DAY_OF_MONTH),
                day -> {
                    selectedCalendar.set(Calendar.DAY_OF_MONTH, day);
                    loadEventsForSelectedDate();
                }
        );
        recyclerMonth.setAdapter(monthAdapter);

        recyclerEvents.setLayoutManager(new LinearLayoutManager(this));
        eventsAdapter = new CalendarEventsAdapter(events);
        recyclerEvents.setAdapter(eventsAdapter);

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddCalendarEventActivity.class);
            intent.putExtra("date", dbFormat.format(selectedCalendar.getTime()));
            startActivity(intent);
        });

        updateMonthTitle();
        buildMonthCalendar();
        loadEventsForSelectedDate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEventsForSelectedDate();
    }


    private void updateMonthTitle() {
        SimpleDateFormat monthFormat =
                new SimpleDateFormat("MMMM yyyy", new Locale("he"));
        tvMonthTitle.setText(monthFormat.format(selectedCalendar.getTime()));
    }

    private void buildMonthCalendar() {
        monthDays.clear();

        Calendar cal = (Calendar) selectedCalendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);

        int firstDay = cal.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 0; i < firstDay; i++) monthDays.add(0);
        for (int d = 1; d <= daysInMonth; d++) monthDays.add(d);

        monthAdapter.notifyDataSetChanged();
    }

    private void loadEventsForSelectedDate() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String groupId = prefs.getString(KEY_GROUP_ID, null);
        if (groupId == null || groupId.isEmpty()) return;

        FirebaseFirestore.getInstance()
                .collection("groups")
                .document(groupId)
                .collection("calendar_events")
                .addSnapshotListener(this, (snapshot, e) -> {
                    if (snapshot == null) return;

                    events.clear();
                    daysWithEvents.clear();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String dateStr = doc.getString("date");
                        if (TextUtils.isEmpty(dateStr)) continue;

                        try {
                            Calendar c = Calendar.getInstance();
                            c.setTime(dbFormat.parse(dateStr));

                            if (c.get(Calendar.YEAR) == selectedCalendar.get(Calendar.YEAR)
                                    && c.get(Calendar.MONTH) == selectedCalendar.get(Calendar.MONTH)) {
                                daysWithEvents.add(c.get(Calendar.DAY_OF_MONTH));
                            }

                            if (sameDay(c, selectedCalendar)) {
                                events.add(doc);
                            }

                        } catch (Exception ignored) {}
                    }

                    eventsAdapter.notifyDataSetChanged();
                    monthAdapter.notifyDataSetChanged();
                    tvEmptyState.setVisibility(events.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    private boolean sameDay(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }
}
