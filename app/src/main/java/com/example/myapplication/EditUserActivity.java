package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.FirebaseFirestore;

public class EditUserActivity extends AppCompatActivity {

    // ===== UI =====
    private EditText etName, etPhone, etEmail, etRole;
    private Button btnSave;
    private ImageView imgAvatar;

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
        etRole = findViewById(R.id.etRole);
        btnSave = findViewById(R.id.btnSave);
        imgAvatar = findViewById(R.id.imgAvatar);

        // ===== Firebase =====
        db = FirebaseFirestore.getInstance();

        // ===== Load user =====
        loadUser();

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
                    etRole.setText(doc.getString("role"));

                    String avatarColor = doc.getString("avatarColor");
                    if (avatarColor != null) {
                        selectedAvatarColor = avatarColor;
                        applyAvatarColor(avatarColor);
                    }
                });
    }

    // ===============================
    // Save user
    // ===============================
    private void saveUser() {
        if (TextUtils.isEmpty(etName.getText())) {
            etName.setError("שם חובה");
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
                        "phone", etPhone.getText().toString().trim(),
                        "email", etEmail.getText().toString().trim(),
                        "role", etRole.getText().toString().trim(),
                        "avatarColor", selectedAvatarColor
                )
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "נשמר בהצלחה", Toast.LENGTH_SHORT).show();
                    finish();
                });
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
            selectColor("blue");
            dialog.dismiss();
        });

        view.findViewById(R.id.avatarPurple).setOnClickListener(v -> {
            selectColor("purple");
            dialog.dismiss();
        });

        view.findViewById(R.id.avatarRed).setOnClickListener(v -> {
            selectColor("red");
            dialog.dismiss();
        });

        view.findViewById(R.id.avatarPink).setOnClickListener(v -> {
            selectColor("pink");
            dialog.dismiss();
        });

        view.findViewById(R.id.avatarTeal).setOnClickListener(v -> {
            selectColor("teal");
            dialog.dismiss();
        });

        dialog.show();
    }

    private void selectColor(String color) {
        selectedAvatarColor = color;
        applyAvatarColor(color);
    }

    // ===============================
    // Apply avatar color (רקע בלבד)
    // ===============================
    private void applyAvatarColor(String color) {

        // אייקון נשאר רגיל
        imgAvatar.setImageResource(R.drawable.ic_user);
        imgAvatar.setColorFilter(
                ContextCompat.getColor(this, android.R.color.white)
        );


        int bgColor = R.color.avatar_beige; // ברירת מחדל עדינה

        switch (color) {
            case "blue":
                bgColor = R.color.avatar_blue;
                break;
            case "purple":
                bgColor = R.color.avatar_purple;
                break;
            case "red":
                bgColor = R.color.avatar_red;
                break;
            case "pink":
                bgColor = R.color.avatar_pink;
                break;
            case "teal":
                bgColor = R.color.avatar_teal;
                break;
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
