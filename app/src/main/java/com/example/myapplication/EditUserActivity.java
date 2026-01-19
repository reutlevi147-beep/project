package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditUserActivity extends AppCompatActivity {

    // ===== UI =====
    private EditText etName, etPhone, etEmail;
    private Button btnSave;
    private ImageView imgAvatar;

    // Role selection
    private MaterialButtonToggleGroup toggleRole;
    private MaterialButton btnParent, btnChild;
    private String selectedRole = "child"; // ברירת מחדל

    // ===== Firebase =====
    private FirebaseFirestore db;

    private String userId;
    private String selectedAvatarColor;

    // ===== SharedPreferences =====
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_GROUP_ID = "group_id";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);

        // ===== userId =====
        userId = getIntent().getStringExtra("userId");
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "❌ חסר userId", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // ===== Bind UI =====
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        btnSave = findViewById(R.id.btnSave);
        imgAvatar = findViewById(R.id.imgAvatar);

        toggleRole = findViewById(R.id.toggleRole);
        btnParent = findViewById(R.id.btnParent);
        btnChild = findViewById(R.id.btnChild);

        // ===== Firebase =====
        db = FirebaseFirestore.getInstance();

        // ===== Load user =====
        loadUser();

        // ===== Role selection =====
        toggleRole.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;

            if (checkedId == R.id.btnParent) {
                selectedRole = "parent";
            } else if (checkedId == R.id.btnChild) {
                selectedRole = "child";
            }
        });

        // ===== Clicks =====
        btnSave.setOnClickListener(v -> saveUser());
        imgAvatar.setOnClickListener(v -> openAvatarDialog());
    }

    // ===============================
    // Load user
    // ===============================
    private void loadUser() {
        String groupId = getGroupId();
        if (groupId == null) return;

        db.collection("groups")
                .document(groupId)
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    etName.setText(doc.getString("name"));
                    etPhone.setText(doc.getString("phone"));
                    etEmail.setText(doc.getString("email"));

                    String role = doc.getString("role");
                    if ("parent".equals(role)) {
                        toggleRole.check(R.id.btnParent);
                        selectedRole = "parent";
                    } else {
                        toggleRole.check(R.id.btnChild);
                        selectedRole = "child";
                    }

                    String avatarColor = doc.getString("avatarColor");
                    if (avatarColor != null) {
                        selectedAvatarColor = avatarColor;
                        applyAvatarColor(avatarColor);
                    }
                });
    }

    // ===============================
    // Save user (עם ולידציות)
    // ===============================
    private void saveUser() {

        // ---- Name ----
        if (TextUtils.isEmpty(etName.getText())) {
            etName.setError("שם חובה");
            return;
        }

        // ---- Phone ----
        String phone = etPhone.getText().toString().trim();
        if (!TextUtils.isEmpty(phone) && !isValidPhone(phone)) {
            etPhone.setError("מספר טלפון לא תקין");
            etPhone.requestFocus();
            return;
        }

        // ---- Email ----
        String email = etEmail.getText().toString().trim();
        if (!TextUtils.isEmpty(email) && !isValidEmail(email)) {
            etEmail.setError("אימייל לא תקין");
            etEmail.requestFocus();
            return;
        }

        String groupId = getGroupId();
        if (groupId == null) return;

        db.collection("groups")
                .document(groupId)
                .collection("users")
                .document(userId)
                .update(
                        "name", etName.getText().toString().trim(),
                        "phone", phone,
                        "email", email,
                        "role", selectedRole,
                        "avatarColor", selectedAvatarColor
                )
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "נשמר בהצלחה", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    // ===============================
    // Validations
    // ===============================
    private boolean isValidPhone(String phone) {
        if (!phone.matches("\\d+")) return false;
        if (phone.length() < 9 || phone.length() > 10) return false;
        return phone.startsWith("0");
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // ===============================
    // Avatar dialog
    // ===============================
    private void openAvatarDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_avatar_picker, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();

        view.findViewById(R.id.avatarBlue).setOnClickListener(v -> {
            selectColor("blue"); dialog.dismiss();
        });
        view.findViewById(R.id.avatarPurple).setOnClickListener(v -> {
            selectColor("purple"); dialog.dismiss();
        });
        view.findViewById(R.id.avatarRed).setOnClickListener(v -> {
            selectColor("red"); dialog.dismiss();
        });
        view.findViewById(R.id.avatarPink).setOnClickListener(v -> {
            selectColor("pink"); dialog.dismiss();
        });
        view.findViewById(R.id.avatarTeal).setOnClickListener(v -> {
            selectColor("teal"); dialog.dismiss();
        });

        dialog.show();
    }

    private void selectColor(String color) {
        selectedAvatarColor = color;
        applyAvatarColor(color);
    }

    private void applyAvatarColor(String color) {
        imgAvatar.setImageResource(R.drawable.ic_user);
        imgAvatar.setColorFilter(
                ContextCompat.getColor(this, android.R.color.white)
        );

        int bgColor = R.color.avatar_beige;
        switch (color) {
            case "blue": bgColor = R.color.avatar_blue; break;
            case "purple": bgColor = R.color.avatar_purple; break;
            case "red": bgColor = R.color.avatar_red; break;
            case "pink": bgColor = R.color.avatar_pink; break;
            case "teal": bgColor = R.color.avatar_teal; break;
        }

        imgAvatar.setBackgroundTintList(
                ContextCompat.getColorStateList(this, bgColor)
        );
    }

    // ===============================
    // groupId
    // ===============================
    private String getGroupId() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(KEY_GROUP_ID, null);
    }
}
