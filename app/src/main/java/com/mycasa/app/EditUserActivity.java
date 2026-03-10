package com.mycasa.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditUserActivity extends AppCompatActivity {

    private TextInputEditText etName, etPhone, etEmail;
    private MaterialButton btnSave;

    private MaterialCardView cardChild, cardParent;
    private TextView tvChild, tvParent;
    private ImageView iconChildCheck, iconParentCheck;

    private FirebaseFirestore db;

    private String userId;
    private String groupId;

    private String selectedRole = "child";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);

        db = FirebaseFirestore.getInstance();

        // ===== Views =====
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        btnSave = findViewById(R.id.btnSave);

        cardChild = findViewById(R.id.cardChild);
        cardParent = findViewById(R.id.cardParent);

        tvChild = findViewById(R.id.tvChild);
        tvParent = findViewById(R.id.tvParent);

        iconChildCheck = findViewById(R.id.iconChildCheck);
        iconParentCheck = findViewById(R.id.iconParentCheck);

        // ===== קבלת נתונים מה Intent =====
        userId = getIntent().getStringExtra("USER_ID");
        groupId = getIntent().getStringExtra("GROUP_ID");

        if (userId == null || groupId == null) {
            Toast.makeText(this, "נתוני משתמש חסרים", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // ===== לחיצות =====
        cardChild.setOnClickListener(v -> selectRole("child"));
        cardParent.setOnClickListener(v -> selectRole("parent"));

        btnSave.setOnClickListener(v -> saveUserData());

        // ברירת מחדל
        selectRole("child");

        loadUserData();
    }

    // ==========================
    // בחירת תפקיד
    // ==========================
    private void selectRole(String role) {

        selectedRole = role;

        if ("child".equals(role)) {

            // CHILD SELECTED
            cardChild.setCardBackgroundColor(getColor(R.color.gray_dark));
            cardChild.setStrokeWidth(3);
            cardChild.setStrokeColor(getColor(R.color.gray_dark));
            tvChild.setTextColor(getColor(android.R.color.white));
            iconChildCheck.setVisibility(View.VISIBLE);

            cardParent.setCardBackgroundColor(getColor(android.R.color.white));
            cardParent.setStrokeWidth(2);
            cardParent.setStrokeColor(getColor(R.color.gray_light));
            tvParent.setTextColor(getColor(R.color.gray_dark));
            iconParentCheck.setVisibility(View.GONE);

        } else {

            // PARENT SELECTED
            cardParent.setCardBackgroundColor(getColor(R.color.gray_dark));
            cardParent.setStrokeWidth(3);
            cardParent.setStrokeColor(getColor(R.color.gray_dark));
            tvParent.setTextColor(getColor(android.R.color.white));
            iconParentCheck.setVisibility(View.VISIBLE);

            cardChild.setCardBackgroundColor(getColor(android.R.color.white));
            cardChild.setStrokeWidth(2);
            cardChild.setStrokeColor(getColor(R.color.gray_light));
            tvChild.setTextColor(getColor(R.color.gray_dark));
            iconChildCheck.setVisibility(View.GONE);
        }
    }

    // ==========================
    // טעינת נתונים
    // ==========================
    private void loadUserData() {

        btnSave.setEnabled(false);

        db.collection("groups")
                .document(groupId)
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(snapshot -> {

                    btnSave.setEnabled(true);

                    if (snapshot.exists()) {

                        String name = snapshot.getString("name");
                        String phone = snapshot.getString("phone");
                        String email = snapshot.getString("email");
                        String role = snapshot.getString("role");

                        etName.setText(name != null ? name : "");
                        etPhone.setText(phone != null ? phone : "");
                        etEmail.setText(email != null ? email : "");

                        if ("parent".equals(role)) {
                            selectRole("parent");
                        } else {
                            selectRole("child");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "שגיאה בטעינה", Toast.LENGTH_SHORT).show();
                });
    }

    // ==========================
    // שמירה
    // ==========================
    private void saveUserData() {

        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name)) {
            etName.setError("השם חובה");
            return;
        }

        if (!TextUtils.isEmpty(email) &&
                !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("אימייל לא תקין");
            return;
        }

        btnSave.setEnabled(false);

        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("phone", phone);
        map.put("email", email);
        map.put("role", selectedRole);

        db.collection("groups")
                .document(groupId)
                .collection("users")
                .document(userId)
                .update(map)
                .addOnSuccessListener(unused -> {

                    btnSave.setEnabled(true);
                    Toast.makeText(this, "עודכן בהצלחה", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "שגיאה בשמירה", Toast.LENGTH_SHORT).show();
                });
    }
}
