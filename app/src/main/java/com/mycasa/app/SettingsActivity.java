package com.mycasa.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    // ===== UI =====
    private ImageButton btnBack;
    private ImageButton btnToggleGroupCode;
    private TextView tvGroupCode;
    private RecyclerView recyclerUsers;
    private MaterialCardView btnManageGroup;

    // ===== Firebase =====
    private FirebaseFirestore db;

    // ===== Adapter =====
    private UsersAdapter usersAdapter;
    private final List<User> usersList = new ArrayList<>();

    // ===== State =====
    private boolean isGroupCodeVisible = false;
    private String groupId;
    private String userId;

    // ===== SharedPreferences =====
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_GROUP_ID = "group_id";
    private static final String KEY_USER_ID = "user_id";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        bindViews();
        setupListeners();
        setupRecycler();

        db = FirebaseFirestore.getInstance();

        if (btnManageGroup != null) {
            btnManageGroup.setVisibility(View.GONE);
        }

        // ===== Get IDs from prefs =====
        groupId = getFromPrefs(KEY_GROUP_ID);
        userId  = getFromPrefs(KEY_USER_ID);

        Log.d("SETTINGS_DEBUG", "groupId = " + groupId);
        Log.d("SETTINGS_DEBUG", "userId = " + userId);

        if (groupId == null || userId == null) {
            Toast.makeText(this, "שגיאת זיהוי משתמש", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        checkUserRole();
        loadUsers();
    }


    // ===============================
    // SharedPrefs helper
    // ===============================
    private String getFromPrefs(String key) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(key, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 200 && resultCode == RESULT_OK) {
            loadUsers(); // טוען מחדש את המשתמשים
        }
    }
    // ===============================
    // Bind Views
    // ===============================
    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        btnToggleGroupCode = findViewById(R.id.btnToggleGroupCode);
        tvGroupCode = findViewById(R.id.tvGroupCode);
        recyclerUsers = findViewById(R.id.recyclerUsers);
        btnManageGroup = findViewById(R.id.btnManageGroup);
    }

    // ===============================
    // Listeners
    // ===============================
    private void setupListeners() {

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnToggleGroupCode != null) {
            btnToggleGroupCode.setOnClickListener(v -> toggleGroupCode());
        }

        if (tvGroupCode != null) {
            tvGroupCode.setOnClickListener(v -> copyGroupCodeIfVisible());
        }

        if (btnManageGroup != null) {
            btnManageGroup.setOnClickListener(v -> {
                Intent intent = new Intent(SettingsActivity.this, ManageMembersActivity.class);
                intent.putExtra("GROUP_ID", groupId);
                startActivity(intent);
            });
        }
    }

    // ===============================
    // Recycler
    // ===============================
    private void setupRecycler() {
        if (recyclerUsers == null) return;

        usersAdapter = new UsersAdapter(this, usersList);
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerUsers.setAdapter(usersAdapter);
    }

    // ===============================
    // Load users
    // ===============================
    private void loadUsers() {

        db.collection("groups")
                .document(groupId)
                .collection("users")
                .get()
                .addOnSuccessListener(snapshot -> {

                    usersList.clear();

                    User currentUser = null;

                    for (QueryDocumentSnapshot doc : snapshot) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            user.setId(doc.getId());

                            if (doc.getId().equals(userId)) {
                                currentUser = user;
                            } else {
                                usersList.add(user);
                            }
                        }
                    }

// אם נמצא המשתמש הנוכחי – נשים אותו ראשון
                    if (currentUser != null) {
                        usersList.add(0, currentUser);
                    }

                    usersAdapter.notifyDataSetChanged();
                });
    }

    // ===============================
    // Check role (NO FirebaseAuth)
    // ===============================
    private void checkUserRole() {

        if (groupId == null || userId == null) {
            btnManageGroup.setVisibility(View.GONE);
            return;
        }

        db.collection("groups")
                .document(groupId)
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (!documentSnapshot.exists()) {
                        btnManageGroup.setVisibility(View.GONE);
                        return;
                    }

                    String role = documentSnapshot.getString("role");

                    if ("parent".equals(role)) {
                        btnManageGroup.setVisibility(View.VISIBLE);
                    } else {
                        btnManageGroup.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    btnManageGroup.setVisibility(View.GONE);
                });
    }
    // ===============================
    // Toggle group code
    // ===============================
    private void toggleGroupCode() {

        if (isGroupCodeVisible) {
            tvGroupCode.setText("קוד קבוצה: -----");
            btnToggleGroupCode.setImageResource(R.drawable.outline_visibility_24);
        } else {
            tvGroupCode.setText("קוד קבוצה: " + groupId);
            btnToggleGroupCode.setImageResource(R.drawable.outline_visibility_off_24);
        }

        isGroupCodeVisible = !isGroupCodeVisible;
    }

    // ===============================
    // Copy group code
    // ===============================
    private void copyGroupCodeIfVisible() {

        if (!isGroupCodeVisible) return;

        String code = groupId;

        ClipboardManager clipboard =
                (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        if (clipboard != null) {
            clipboard.setPrimaryClip(
                    ClipData.newPlainText("groupCode", code)
            );
            Toast.makeText(this, "קוד הועתק", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadUsers();       // רענון רשימת משתמשים
        checkUserRole();   // רענון הרשאות
    }
}