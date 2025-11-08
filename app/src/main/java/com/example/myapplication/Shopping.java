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
