package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Add_Tasks extends AppCompatActivity {

    private EditText etTitle;
    private FirebaseFirestore db;

    private String selectedPriority = null;
    private String selectedCategory = null;
    private ArrayList<String> selectedUserIds = new ArrayList<>();
    private boolean allSelected = false;
    private ArrayList<String> allUserIds = new ArrayList<>();
    private ArrayList<String> allUserNames = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_tasks);

        db = FirebaseFirestore.getInstance();

        // כותרת
        etTitle = findViewById(R.id.etTitle);

        // חזרה
        findViewById(R.id.btnBack).setOnClickListener(v ->
                finish()
        );

        // שמירה
        findViewById(R.id.btnSave).setOnClickListener(v ->
                saveTaskToFirestore()
        );

        setupPrioritySelection();
        setupCategorySelection();
    }

    private void loadUsersFromFirestore() {

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String groupId = prefs.getString("group_id", null);

        if (groupId == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .whereEqualTo("groupId", groupId)
                .get()
                .addOnSuccessListener(snapshot -> {

                    allUserIds.clear();
                    allUserNames.clear();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        allUserIds.add(doc.getId());
                        allUserNames.add(doc.getString("name"));
                    }

                    buildUsersUI();
                });
    }

    private void buildUsersUI() {

        LinearLayout container = findViewById(R.id.usersContainer);
        container.removeAllViews();

        // כפתור "כולם"
        View allView = createUserView("כולם", "ALL");
        container.addView(allView);

        for (int i = 0; i < allUserIds.size(); i++) {
            String userId = allUserIds.get(i);
            String name = allUserNames.get(i);

            View userView = createUserView(name, userId);
            container.addView(userView);
        }
    }

    private View createUserView(String name, String userId) {

        TextView tv = new TextView(this);
        tv.setText(name);
        tv.setPadding(32, 24, 32, 24);
        tv.setBackgroundResource(R.drawable.bg_selectable_circle);
        tv.setTextSize(16);

        tv.setOnClickListener(v -> {
            if (userId.equals("ALL")) {
                toggleSelectAll(allUserIds);
            } else {
                toggleUser(userId);
            }
        });

        return tv;
    }


    // --------------------------------------------------
    // 🔴 עדיפות
    // --------------------------------------------------
    private void setupPrioritySelection() {

        View high = findViewById(R.id.circleHigh);
        View medium = findViewById(R.id.circleMedium);
        View low = findViewById(R.id.circleLow);

        findViewById(R.id.cardPriorityHigh).setOnClickListener(v ->
                selectPriority("high", high, medium, low)
        );

        findViewById(R.id.cardPriorityMedium).setOnClickListener(v ->
                selectPriority("medium", medium, high, low)
        );

        findViewById(R.id.cardPriorityLow).setOnClickListener(v ->
                selectPriority("low", low, high, medium)
        );
    }

    private void selectPriority(String value, View selected, View o1, View o2) {
        selected.setBackgroundResource(R.drawable.bg_selectable_circle_selected);
        o1.setBackgroundResource(R.drawable.bg_selectable_circle);
        o2.setBackgroundResource(R.drawable.bg_selectable_circle);
        selectedPriority = value;
    }

    // --------------------------------------------------
    // 🏷 קטגוריה
    // --------------------------------------------------
    private void setupCategorySelection() {

        findViewById(R.id.cardCatHome).setOnClickListener(v -> selectCategory("בית"));
        findViewById(R.id.cardCatWork).setOnClickListener(v -> selectCategory("עבודה"));
        findViewById(R.id.cardCatShopping).setOnClickListener(v -> selectCategory("קניות"));
        findViewById(R.id.cardCatPersonal).setOnClickListener(v -> selectCategory("אישי"));
        findViewById(R.id.cardCatHealth).setOnClickListener(v -> selectCategory("בריאות"));
        findViewById(R.id.cardCatOther).setOnClickListener(v -> selectCategory("אחר"));
    }

    private void selectCategory(String category) {
        selectedCategory = category;
    }

    // --------------------------------------------------
    // 💾 שמירה
    // --------------------------------------------------
    private void saveTaskToFirestore() {

        String title = etTitle.getText().toString().trim();

        if (title.isEmpty() || selectedPriority == null || selectedCategory == null) {
            Toast.makeText(this, "נא למלא כותרת, עדיפות וקטגוריה", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> task = new HashMap<>();
        task.put("title", title);
        task.put("priority", selectedPriority);
        task.put("category", selectedCategory);
        task.put("isDone", false);
        task.put("createdAt", Timestamp.now());

        db.collection("home_tasks")
                .document("defaultList")
                .collection("items")
                .add(task)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "המשימה נוספה", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, TasksActivity.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "שגיאה בשמירה", Toast.LENGTH_SHORT).show()
                );
    }

    private void toggleUser(String userId) {

        if (allSelected) {
            allSelected = false;
            selectedUserIds.clear();
        }

        if (selectedUserIds.contains(userId)) {
            selectedUserIds.remove(userId);
        } else {
            selectedUserIds.add(userId);
        }

        updateUserUI();
    }


    private void toggleSelectAll(ArrayList<String> allUserIds) {

        if (allSelected) {
            allSelected = false;
            selectedUserIds.clear();
        } else {
            allSelected = true;
            selectedUserIds.clear();
            selectedUserIds.addAll(allUserIds);
        }

        updateUserUI();
    }


    private void updateUserUI() {
        // שלב הבא – כאן נסמן עיגולים / כרטיסים
    }




}
