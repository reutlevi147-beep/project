package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Add_Calender extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_calender);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 🔹 כפתור "הוספה ליומן" - מעבר לעמוד המשימות והיומן
        Button addToCalendar = findViewById(R.id.AddtoCalendar);
        addToCalendar.setOnClickListener(v ->
                startActivity(new Intent(this, TasksActivity.class))
        );

        // 🔹 כפתור "Return" לעמוד Display_Calender_Tasks
        ImageButton returnToCalendar = findViewById(R.id.Return);
        returnToCalendar.setOnClickListener(v ->
                startActivity(new Intent(this, TasksActivity.class))
        );


    }
}

