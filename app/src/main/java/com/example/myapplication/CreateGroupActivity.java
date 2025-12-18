package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CreateGroupActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_ONBOARDING_DONE = "onboarding_completed";
    private static final String KEY_GROUP_ID = "group_id";
    private static final String KEY_JOIN_CODE = "join_code";

    // UI
    private EditText etName, etPhone, etEmail, etGroupCode;
    private RadioGroup rgGroupType, rgFamilyRole;
    private LinearLayout layoutFamilyRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etGroupCode = findViewById(R.id.etGroupCode);

        rgGroupType = findViewById(R.id.rgGroupType);
        rgFamilyRole = findViewById(R.id.rgFamilyRole);
        layoutFamilyRole = findViewById(R.id.layoutFamilyRole);

        Button btnCreateGroup = findViewById(R.id.btnCreateGroup);

        rgGroupType.setOnCheckedChangeListener((group, checkedId) -> {
            layoutFamilyRole.setVisibility(
                    checkedId == R.id.rbFamily ? View.VISIBLE : View.GONE
            );
        });

        btnCreateGroup.setOnClickListener(v -> {
            if (validateForm()) {
                checkGroupCodeAndCreate();
            }
        });
    }

    // 🔹 ולידציות
    private boolean validateForm() {

        etName.setError(null);
        etPhone.setError(null);
        etEmail.setError(null);
        etGroupCode.setError(null);

        boolean valid = true;

        if (TextUtils.isEmpty(etName.getText().toString().trim())) {
            etName.setError("חובה למלא שם");
            valid = false;
        }

        String phone = etPhone.getText().toString().trim();
        if (TextUtils.isEmpty(phone) || !phone.matches("^05\\d{8}$")) {
            etPhone.setError("מספר טלפון לא תקין");
            valid = false;
        }

        String email = etEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)
                || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("כתובת אימייל לא תקינה");
            valid = false;
        }

        String code = etGroupCode.getText().toString().trim();
        if (TextUtils.isEmpty(code)
                || code.length() < 6
                || code.length() > 10
                || !code.matches("^[A-Za-z0-9]+$")) {
            etGroupCode.setError("קוד קבוצה לא תקין");
            valid = false;
        }

        int selectedGroupType = rgGroupType.getCheckedRadioButtonId();
        if (selectedGroupType == -1) {
            Toast.makeText(this, "יש לבחור סוג קבוצה", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (selectedGroupType == R.id.rbFamily &&
                rgFamilyRole.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "יש לבחור תפקיד במשפחה", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        return valid;
    }

    // 🔹 בדיקת ייחודיות קוד
    private void checkGroupCodeAndCreate() {
        FirebaseFirestore.getInstance()
                .collection("groups")
                .whereEqualTo("groupCode", etGroupCode.getText().toString().trim())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        etGroupCode.setError("קוד הקבוצה כבר קיים");
                    } else {
                        createGroup();
                    }
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }

    // 🔹 יצירת קבוצה + משתמש
    private void createGroup() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String groupCode = etGroupCode.getText().toString().trim();

        String groupType =
                rgGroupType.getCheckedRadioButtonId() == R.id.rbFamily ? "family" :
                        rgGroupType.getCheckedRadioButtonId() == R.id.rbPartners ? "partners" :
                                "couple";

        String role =
                "family".equals(groupType)
                        ? (rgFamilyRole.getCheckedRadioButtonId() == R.id.rbParent ? "parent" : "child")
                        : "member";

        Map<String, Object> groupData = new HashMap<>();
        groupData.put("groupCode", groupCode);
        groupData.put("groupType", groupType);
        groupData.put("createdAt", FieldValue.serverTimestamp());

        db.collection("groups")
                .add(groupData)
                .addOnSuccessListener(groupRef -> {

                    String groupId = groupRef.getId();

                    Map<String, Object> userData = new HashMap<>();
                    userData.put("name", name);
                    userData.put("phone", phone);
                    userData.put("email", email);
                    userData.put("groupId", groupId);
                    userData.put("role", role);
                    userData.put("createdAt", FieldValue.serverTimestamp());

                    db.collection("users")
                            .add(userData)
                            .addOnSuccessListener(userRef -> {
                                saveUserLocally(userRef.getId(), role, groupId, groupCode);
                                markOnboardingCompleted();
                                goToHome();
                            });
                });
    }

    // 🔹 שמירת משתמש מקומית (חדש)
    private void saveUserLocally(String userId, String role, String groupId, String joinCode) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit()
                .putString("user_id", userId)
                .putString("role", role)
                .putString(KEY_GROUP_ID, groupId)
                .putString(KEY_JOIN_CODE, joinCode)
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
