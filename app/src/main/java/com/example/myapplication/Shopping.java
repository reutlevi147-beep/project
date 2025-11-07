package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Shopping extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shopping);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // שורת משימות
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

        // ✅ כפתור רשימת הקניות שלי → Shopping_list
        ImageButton myShopList = findViewById(R.id.my_shop1);
        myShopList.setOnClickListener(v -> {
            Intent intent = new Intent(Shopping.this, Shopping_list.class);
            startActivity(intent);
        });

        // ✅ כפתור רשימות מוצעות → Suggested_list
        ImageButton suggestedList = findViewById(R.id.my_shop2);
        suggestedList.setOnClickListener(v -> {
            Intent intent = new Intent(Shopping.this, Suggested_list.class);
            startActivity(intent);
        });
    }
}
