package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class StartActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_ONBOARDING_DONE = "onboarding_completed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean onboardingDone = prefs.getBoolean(KEY_ONBOARDING_DONE, false);

        // אם כבר סיימו את ההתחלה – מדלגים ישר לבית
        if (onboardingDone) {
            Intent intent = new Intent(this, Home.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return;
        }

        setContentView(R.layout.activity_start);

        Button btnCreateGroup = findViewById(R.id.btnCreateGroup);
        Button btnJoinGroup = findViewById(R.id.btnJoinGroup);

        btnCreateGroup.setOnClickListener(v ->
                startActivity(new Intent(StartActivity.this, CreateGroupActivity.class))
        );

        btnJoinGroup.setOnClickListener(v ->
                startActivity(new Intent(StartActivity.this, JoinGroupActivity.class))
        );
    }
}
