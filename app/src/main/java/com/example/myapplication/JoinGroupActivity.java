package com.example.myapplication;

import android.annotation.SuppressLint;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class JoinGroupActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_GROUP_ID = "group_id";
    private static final String KEY_JOIN_CODE = "join_code";
    private static final String KEY_ONBOARDING_DONE = "onboarding_completed";

    private EditText etName, etPhone, etEmail, etGroupCode;
    private LinearLayout layoutFamilyRole;
    private RadioGroup rgFamilyRole;

    @SuppressLint("MissingInflatedId")
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

        btnJoinGroup.setOnClickListener(v -> {
            if (validateForm()) {
                checkGroupAndJoin();
            }
        });
    }

    // 🔹 ולידציות בסיסיות
    private boolean validateForm() {

        etName.setError(null);
        etPhone.setError(null);
        etEmail.setError(null);
        etGroupCode.setError(null);

        boolean valid = true;

        if (TextUtils.isEmpty(etName.getText())) {
            etName.setError("חובה למלא שם");
            valid = false;
        }

        if (TextUtils.isEmpty(etPhone.getText())) {
            etPhone.setError("חובה למלא מספר טלפון");
            valid = false;
        }

        if (TextUtils.isEmpty(etEmail.getText())) {
            etEmail.setError("חובה למלא אימייל");
            valid = false;
        }

        String code = etGroupCode.getText().toString().trim();
        if (code.length() < 6) {
            etGroupCode.setError("קוד קבוצה לא תקין");
            valid = false;
        }

        return valid;
    }

    // 🔹 בדיקת קוד קבוצה + זיהוי סוג קבוצה
    private void checkGroupAndJoin() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String groupCode = etGroupCode.getText().toString().trim();

        db.collection("groups")
                .whereEqualTo("groupCode", groupCode)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        etGroupCode.setError("קוד קבוצה לא קיים");
                        return;
                    }

                    QueryDocumentSnapshot groupDoc =
                            (QueryDocumentSnapshot) querySnapshot.getDocuments().get(0);

                    String groupId = groupDoc.getId();
                    String groupType = groupDoc.getString("groupType");

                    if ("family".equals(groupType)) {
                        layoutFamilyRole.setVisibility(View.VISIBLE);

                        if (rgFamilyRole.getCheckedRadioButtonId() == -1) {
                            Toast.makeText(this,
                                    "יש לבחור תפקיד במשפחה",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    String role = resolveRole(groupType);
                    joinGroup(groupId, groupCode, role);
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }

    // 🔹 קביעת role
    private String resolveRole(String groupType) {
        if (!"family".equals(groupType)) {
            return "member";
        }

        return (rgFamilyRole.getCheckedRadioButtonId() == R.id.rbParent)
                ? "parent"
                : "child";
    }

    // 🔹 יצירת משתמש בקבוצה
    private void joinGroup(String groupId, String groupCode, String role) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> userData = new HashMap<>();
        userData.put("name", etName.getText().toString().trim());
        userData.put("phone", etPhone.getText().toString().trim());
        userData.put("email", etEmail.getText().toString().trim());
        userData.put("groupId", groupId);
        userData.put("role", role);
        userData.put("createdAt", FieldValue.serverTimestamp());

        db.collection("users")
                .add(userData)
                .addOnSuccessListener(userRef -> {
                    saveGroupLocally(groupId, groupCode);
                    markOnboardingCompleted();
                    goToHome();
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }

    private void saveGroupLocally(String groupId, String joinCode) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_GROUP_ID, groupId)
                .putString(KEY_JOIN_CODE, joinCode)
                .apply();
    }

    private void markOnboardingCompleted() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, true).apply();
    }

    private void goToHome() {
        Intent intent = new Intent(this, Home.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
