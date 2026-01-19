package com.example.myapplication;

import android.content.Intent;
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
import java.util.function.Consumer;

public class CreateGroupActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_ONBOARDING_DONE = "onboarding_completed";
    private static final String KEY_GROUP_CODE = "group_code";

    private EditText etName, etPhone, etEmail, etGroupCode, etFamilyName;
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
        etFamilyName = findViewById(R.id.etFamilyName);

        rgGroupType = findViewById(R.id.rgGroupType);
        rgFamilyRole = findViewById(R.id.rgFamilyRole);
        layoutFamilyRole = findViewById(R.id.layoutFamilyRole);

        Button btnCreateGroup = findViewById(R.id.btnCreateGroup);
        Button btnSuggestCode = findViewById(R.id.btnSuggestCode);

        rgGroupType.setOnCheckedChangeListener((group, checkedId) ->
                layoutFamilyRole.setVisibility(
                        checkedId == R.id.rbFamily ? View.VISIBLE : View.GONE
                )
        );

        btnSuggestCode.setOnClickListener(v -> {
            etGroupCode.setError(null);
            etGroupCode.setText("...");

            generateAvailableGroupCode(code -> {
                etGroupCode.setText(code);
            });
        });

        // 🔹 יצירת קוד מוצע אוטומטית
        generateAvailableGroupCode(code -> etGroupCode.setText(code));

        btnCreateGroup.setOnClickListener(v -> {
            if (validateForm()) {
                checkGroupCodeAndCreate();
            }
        });
    }

    private String cleanPhone() {
        return etPhone.getText().toString().replaceAll("[^0-9]", "").trim();
    }

    private boolean validateForm() {
        boolean valid = true;

        if (TextUtils.isEmpty(etName.getText().toString().trim())) {
            etName.setError("חובה למלא שם");
            valid = false;
        }

        if (!cleanPhone().matches("^05\\d{8}$")) {
            etPhone.setError("מספר טלפון לא תקין");
            valid = false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS
                .matcher(etEmail.getText().toString().trim()).matches()) {
            etEmail.setError("אימייל לא תקין");
            valid = false;
        }

        if (!etGroupCode.getText().toString().trim().matches("^[A-Za-z0-9]{6,10}$")) {
            etGroupCode.setError("קוד קבוצה לא תקין");
            valid = false;
        }

        if (rgGroupType.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "יש לבחור סוג קבוצה", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (rgGroupType.getCheckedRadioButtonId() == R.id.rbFamily) {
            if (rgFamilyRole.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, "יש לבחור תפקיד במשפחה", Toast.LENGTH_SHORT).show();
                valid = false;
            }
            if (TextUtils.isEmpty(etFamilyName.getText().toString().trim())) {
                etFamilyName.setError("חובה להזין שם משפחה");
                valid = false;
            }
        }

        return valid;
    }

    private void checkGroupCodeAndCreate() {
        String groupCode = etGroupCode.getText().toString().trim().toUpperCase();

        FirebaseFirestore.getInstance()
                .collection("groups")
                .document(groupCode)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        etGroupCode.setError("קוד הקבוצה כבר קיים");
                    } else {
                        createGroup(groupCode);
                    }
                });
    }

    private void createGroup(String groupCode) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

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

        if ("family".equals(groupType)) {
            groupData.put("familyName", etFamilyName.getText().toString().trim());
        }

        db.collection("groups")
                .document(groupCode)
                .set(groupData)
                .addOnSuccessListener(unused -> {

                    Map<String, Object> userData = new HashMap<>();
                    userData.put("name", etName.getText().toString().trim());
                    userData.put("phone", cleanPhone());
                    userData.put("email", etEmail.getText().toString().trim());
                    userData.put("groupId", groupCode);
                    userData.put("role", role);
                    userData.put("createdAt", FieldValue.serverTimestamp());

                    db.collection("groups")
                            .document(groupCode)
                            .collection("users")
                            .add(userData)
                            .addOnSuccessListener(userRef -> {
                                saveUserLocally(
                                        userRef.getId(),
                                        role,
                                        groupCode,
                                        etName.getText().toString().trim()
                                );
                                markOnboardingCompleted();
                                goToHome();
                            });
                });
    }

    private void saveUserLocally(String userId, String role,
                                 String groupCode, String userName) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString("user_id", userId)
                .putString("role", role)
                .putString(KEY_GROUP_CODE, groupCode)
                .putString("group_id", groupCode)
                .putString("user_name", userName)
                .putString("family_name", etFamilyName.getText().toString().trim())
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

    // =========================
    // 🔹 קוד מוצע אוטומטי
    // =========================

    private String generateGroupCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }

    private void generateAvailableGroupCode(Consumer<String> callback) {
        String code = generateGroupCode();

        FirebaseFirestore.getInstance()
                .collection("groups")
                .document(code)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        callback.accept(code);
                    } else {
                        generateAvailableGroupCode(callback);
                    }
                });
    }
}
