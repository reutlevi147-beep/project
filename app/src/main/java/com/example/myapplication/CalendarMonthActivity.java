package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CalendarView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CalendarMonthActivity extends AppCompatActivity {

    private CalendarView calendarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_month);

        calendarView = findViewById(R.id.calendarView);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {

            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth);

            SimpleDateFormat format =
                    new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            String selectedDate = format.format(cal.getTime());

            // מעבר ליומן יומי
            Intent intent = new Intent(this, CalendarDayActivity.class);
            intent.putExtra("date", selectedDate);
            startActivity(intent);
        });
    }
}
