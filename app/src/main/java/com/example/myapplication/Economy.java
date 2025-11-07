package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Economy extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_economy);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // כפתור חזרה לעמוד הבית
        ImageButton homeBtn = findViewById(R.id.M3);
        homeBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Economy.this, Home.class);
            startActivity(intent);
        });

        // כפתור מטרות
        ImageButton goalsBtn = findViewById(R.id.B5);
        goalsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Economy.this, Display_Purpose.class);
            startActivity(intent);
        });

        // כפתור פעולות
        ImageButton actionsBtn = findViewById(R.id.B6);
        actionsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Economy.this, Display_Purpose.class);
            startActivity(intent);
        });




    }
}
