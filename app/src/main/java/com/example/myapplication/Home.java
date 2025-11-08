package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Home extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // כפתור מעבר לעמוד כלכלת הבית
        ImageButton economyBtn = findViewById(R.id.H_Economy);
        economyBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, Economy.class);
            startActivity(intent);
        });

        // כפתור מעבר לעמוד קניות
        ImageButton shoppingBtn = findViewById(R.id.HShopping);
        shoppingBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, Shopping.class);
            startActivity(intent);
        });

        // כפתור מעבר לעמוד משימות בית
        ImageButton tasksBtn = findViewById(R.id.HTasks);
        tasksBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, Display_Calender_Tasks.class);
            startActivity(intent);
        });

        // כפתור מעבר לעמוד לו״ז
        ImageButton CalendersBtn = findViewById(R.id.HCalender);
        CalendersBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, Display_Calender_Tasks.class);
            startActivity(intent);
        });

        // כפתור מעבר לעמוד הגדרות
        ImageButton settingsBtn = findViewById(R.id.HSetting);
        settingsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, Settings.class);
            startActivity(intent);
        });
    }
}
