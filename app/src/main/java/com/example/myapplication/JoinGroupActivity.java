package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class JoinGroupActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_ONBOARDING_DONE = "onboarding_completed";
    private static final String KEY_GROUP_CODE = "group_code";

    private EditText etName, etPhone, etEmail, etGroupCode;
    private LinearLayout layoutFamilyRole;
    private RadioGroup rgFamilyRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etGroupCode = findViewById(R.id.etGroupCode);
        layoutFamilyRole = findViewById(R.id.layoutFamilyRole);
        rgFamilyRole = findViewById(R.id.rgFamilyRole);

        Button btnJoinGroup = findViewById(R.id.btnJoinGroup);
        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        btnJoinGroup.setOnClickListener(v -> {
            if (validateForm()) {
                joinGroup();
            } else {
                Toast.makeText(this, "יש שדות שלא מולאו כראוי", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String cleanPhone() {
        return etPhone.getText().toString().replaceAll("[^0-9]", "").trim();
    }

    private boolean validateForm() {
        etName.setError(null);
        etPhone.setError(null);
        etEmail.setError(null);
        etGroupCode.setError(null);

        if (TextUtils.isEmpty(etName.getText().toString().trim())) {
            etName.setError("חובה להזין שם");
            return false;
        }

        String phone = cleanPhone();
        if (!phone.matches("^05\\d{8}$")) {
            etPhone.setError("מספר טלפון לא תקין");
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS
                .matcher(etEmail.getText().toString().trim()).matches()) {
            etEmail.setError("אימייל לא תקין");
            return false;
        }

        if (TextUtils.isEmpty(etGroupCode.getText().toString().trim())) {
            etGroupCode.setError("יש להזין קוד קבוצה");
            return false;
        }

        return true;
    }

    private void joinGroup() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String groupCode = etGroupCode.getText().toString().trim().toUpperCase();

        db.collection("groups")
                .document(groupCode)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        etGroupCode.setError("קוד קבוצה לא נמצא");
                        return;
                    }

                    String groupType = doc.getString("groupType");
                    String familyName = doc.getString("familyName");


                    if ("family".equals(groupType)
                            && rgFamilyRole.getCheckedRadioButtonId() == -1) {
                        layoutFamilyRole.setVisibility(View.VISIBLE);
                        Toast.makeText(this, "יש לבחור תפקיד במשפחה", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String role =
                            "family".equals(groupType)
                                    ? (rgFamilyRole.getCheckedRadioButtonId() == R.id.rbParent ? "parent" : "child")
                                    : "member";

                    Map<String, Object> userData = new HashMap<>();
                    userData.put("name", etName.getText().toString().trim());
                    userData.put("phone", cleanPhone());
                    userData.put("email", etEmail.getText().toString().trim());
                    userData.put("groupId", groupCode);
                    userData.put("role", role);

                    db.collection("users")
                            .add(userData)
                            .addOnSuccessListener(userRef -> {
                                saveUserLocally(
                                        userRef.getId(),
                                        groupCode,
                                        groupCode,
                                        etName.getText().toString().trim(),
                                        familyName
                                );


                                markOnboardingCompleted();
                                goToHome();
                            });
                });
    }

    private void saveUserLocally(String userId, String groupId,
                                 String joinCode, String userName,
                                 String familyName)
    {



        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString("user_id", userId)
                .putString("group_id", groupId)
                .putString("join_code", joinCode)
                .putString("user_name", userName)
                .putString("family_name", familyName)
                .apply();

    }


    private void markOnboardingCompleted() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_ONBOARDING_DONE, true)
                .apply();
    }

    private void goToHome() {
        Intent intent = new Intent(this, Home.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
