package com.mycasa.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Add_Money extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_money);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 🔹 כפתור "הוספת הוצאה" – מעבר לעמוד הצגת הוצאות
        Button addExpense = findViewById(R.id.Addaמexpense);
        addExpense.setOnClickListener(v ->
                startActivity(new Intent(this, Display_Economy.class))
        );

        // 🔹 כפתור "Return" לעמוד Display_Economy
        ImageButton returnToEconomy = findViewById(R.id.Return);
        returnToEconomy.setOnClickListener(v ->
                startActivity(new Intent(this, Display_Economy.class))
        );


    }
}
