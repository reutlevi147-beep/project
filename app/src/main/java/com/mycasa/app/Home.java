package com.mycasa.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Home extends AppCompatActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        // =====================
        // SharedPreferences
        // =====================
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);

        String userId = prefs.getString("user_id", null);
        String groupId = prefs.getString("group_id", null);
        String userName = prefs.getString("user_name", null);
        String familyName = prefs.getString("family_name", null);

        // =====================
        // Toasts – אחד אחד
        // =====================
        Handler handler = new Handler(Looper.getMainLooper());

        handler.postDelayed(() ->
                Toast.makeText(this, "userId = " + userId, Toast.LENGTH_SHORT).show(), 0);

        handler.postDelayed(() ->
                Toast.makeText(this, "groupId = " + groupId, Toast.LENGTH_SHORT).show(), 2000);

        handler.postDelayed(() ->
                Toast.makeText(this, "userName = " + userName, Toast.LENGTH_SHORT).show(), 4000);

        handler.postDelayed(() ->
                Toast.makeText(this, "familyName = " + familyName, Toast.LENGTH_SHORT).show(), 6000);

        // =====================
        // שלום למשתמש
        // =====================
        TextView tvHello = findViewById(R.id.tvHello);

        if (familyName != null && !familyName.isEmpty()) {
            tvHello.setText("שלום, משפחת " + familyName);
        } else if (userName != null && !userName.isEmpty()) {
            tvHello.setText("שלום, " + userName);
        } else {
            tvHello.setText("שלום");
        }



        // =====================
        // Quick actions
        // =====================
        findViewById(R.id.quickShopping).setOnClickListener(v ->
                startActivity(new Intent(this, ShoppingListActivity.class))
        );

        findViewById(R.id.quickCalendar).setOnClickListener(v ->
                startActivity(new Intent(this, CalendarDayActivity.class))
        );

        findViewById(R.id.quickTasks).setOnClickListener(v ->
                startActivity(new Intent(Home.this, TasksActivity.class))
        );
        findViewById(R.id.quickSettings).setOnClickListener(v ->
                startActivity(new Intent(Home.this, SettingsActivity.class))
        );


        // =====================
        // Bottom Navigation
        // =====================
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) return true;

            if (id == R.id.nav_calendar) {
                startActivity(new Intent(this, CalendarDayActivity.class));
                return true;
            }

            if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }

            if (id == R.id.nav_stats) {
                startActivity(new Intent(this, Finance.class));
                return true;
            }

            return false;
        });
    }
}
