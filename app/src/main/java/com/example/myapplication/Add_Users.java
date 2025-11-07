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

public class Add_Users extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_users);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 🔹 כפתור "הוספת משתמש" – מעבר לעמוד הגדרות
        Button addUser = findViewById(R.id.add_user); // ודאי שה-ID הזה קיים בקובץ ה-XML שלך
        addUser.setOnClickListener(v -> {
            Intent intent = new Intent(this, Settings.class);
            startActivity(intent);
        });

        // 🔹 קישורים לשורת המשימות (ניווט תחתון)
        ImageButton m1 = findViewById(R.id.M1);
        ImageButton m2 = findViewById(R.id.M2);
        ImageButton m3 = findViewById(R.id.M3);
        ImageButton m4 = findViewById(R.id.M4);
        ImageButton m5 = findViewById(R.id.M5);

        m1.setOnClickListener(v -> startActivity(new Intent(this, Economy.class)));
        m2.setOnClickListener(v -> startActivity(new Intent(this, Display_Calender_Tasks.class)));
        m3.setOnClickListener(v -> startActivity(new Intent(this, Home.class)));
        m4.setOnClickListener(v -> startActivity(new Intent(this, Shopping.class)));
        m5.setOnClickListener(v -> startActivity(new Intent(this, Settings.class)));
    }
}
