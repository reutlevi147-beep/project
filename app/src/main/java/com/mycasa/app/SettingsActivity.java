package com.mycasa.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    // ===== Firebase =====
    private FirebaseFirestore db;

    // ===== Adapter =====
    private UsersAdapter usersAdapter;
    private final List<User> usersList = new ArrayList<>();

    // ===== State =====
    private boolean isGroupCodeVisible = false;

    // ===== SharedPreferences =====
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_GROUP_ID = "group_id";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        bindViews();
        setupListeners();
        setupRecycler();

        db = FirebaseFirestore.getInstance();

        String groupId = getGroupId();
        if (groupId != null && !groupId.isEmpty()) {
            loadUsers(groupId);
        } else if (tvGroupCode != null) {
            tvGroupCode.setText("קוד קבוצה: לא נמצא");
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
    }

    // ===============================
    // RecyclerView
    // ===============================
    private void setupRecycler() {
        if (recyclerUsers == null) return;

        usersAdapter = new UsersAdapter(this, usersList);
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerUsers.setAdapter(usersAdapter);
    }

    // ===============================
    // Copy group code
    // ===============================
    private void copyGroupCodeIfVisible() {
        if (!isGroupCodeVisible || tvGroupCode == null) return;

        String code = tvGroupCode.getText()
                .toString()
                .replace("קוד קבוצה:", "")
                .trim();

        ClipboardManager clipboard =
                (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        if (clipboard != null) {
            clipboard.setPrimaryClip(
                    ClipData.newPlainText("groupCode", code)
            );
            Toast.makeText(this, "קוד הועתק", Toast.LENGTH_SHORT).show();
        }
    }

    // ===============================
    // Get group_id
    // ===============================
    private String getGroupId() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(KEY_GROUP_ID, null);
    }

    // ===============================
    // Load users
    // ===============================
    private void loadUsers(String groupId) {
        db.collection("groups")
                .document(groupId)
                .collection("users")
                .get()
                .addOnSuccessListener(snapshot -> {
                    usersList.clear();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            user.setId(doc.getId());
                            usersList.add(user);
                        }
                    }

                    if (usersAdapter != null) {
                        usersAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("SettingsActivity", "❌ Firestore error", e)
                );
    }

    // ===============================
    // Toggle group code
    // ===============================
    private void toggleGroupCode() {
        if (tvGroupCode == null || btnToggleGroupCode == null) return;

        String groupId = getGroupId();

        if (groupId == null || groupId.isEmpty()) {
            tvGroupCode.setText("קוד קבוצה: לא נמצא");
            return;
        }

        if (isGroupCodeVisible) {
            tvGroupCode.setText("קוד קבוצה: -----");
            btnToggleGroupCode.setImageResource(android.R.drawable.ic_menu_view);
        } else {
            tvGroupCode.setText("קוד קבוצה: " + groupId);
            btnToggleGroupCode.setImageResource(android.R.drawable.ic_secure);
        }

        isGroupCodeVisible = !isGroupCodeVisible;
    }

    @Override
    protected void onResume() {
        super.onResume();
        String groupId = getGroupId();
        if (groupId != null && !groupId.isEmpty()) {
            loadUsers(groupId);
        }
    }

}
