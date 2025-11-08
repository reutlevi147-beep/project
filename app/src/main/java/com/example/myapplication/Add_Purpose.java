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

public class Add_Purpose extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_purpose);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 🔹 כפתור "הוספת מטרה" – מעבר לעמוד תצוגת מטרות
        Button addPurpose = findViewById(R.id.Add_purpose);
        addPurpose.setOnClickListener(v -> {
            Intent intent = new Intent(this, Display_Purpose.class);
            startActivity(intent);
        });

        // 🔹 כפתור "Return" לעמוד Display_Purpose
        ImageButton returnToPurpose = findViewById(R.id.Return);
        returnToPurpose.setOnClickListener(v ->
                startActivity(new Intent(this, Display_Purpose.class))
        );



    }
}
