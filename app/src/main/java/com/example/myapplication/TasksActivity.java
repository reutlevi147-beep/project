package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;

public class TasksActivity extends AppCompatActivity {

    private RecyclerView recyclerTasks;
    private HomeTasksAdapter adapter;
    private final ArrayList<Object> items = new ArrayList<>();

    private String selectedCategory = "הכל";

    // מונים
    private TextView tvActiveCount;
    private TextView tvCompletedCount;

    // כפתורי קטגוריות
    private Button btnAll, btnFamily, btnHome, btnWork,
            btnShopping, btnPersonal, btnHealth, btnOther;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        // ===== HEADER – מונים =====
        View statActive = findViewById(R.id.statActive);
        tvActiveCount = statActive.findViewById(R.id.tvValue);
        TextView tvActiveLabel = statActive.findViewById(R.id.tvLabel);
        tvActiveLabel.setText("פעילות");

        View statCompleted = findViewById(R.id.statCompleted);
        tvCompletedCount = statCompleted.findViewById(R.id.tvValue);
        TextView tvCompletedLabel = statCompleted.findViewById(R.id.tvLabel);
        tvCompletedLabel.setText("בוצעו");

        // ===== כפתורי קטגוריות =====
        btnAll = findViewById(R.id.btnAll);
        btnFamily = findViewById(R.id.btnFamily);
        btnHome = findViewById(R.id.btnHome);
        btnWork = findViewById(R.id.btnWork);
        btnShopping = findViewById(R.id.btnShopping);
        btnPersonal = findViewById(R.id.btnPersonal);
        btnHealth = findViewById(R.id.btnHealth);
        btnOther = findViewById(R.id.btnOther);

        btnAll.setOnClickListener(v -> selectCategory("הכל"));
        btnFamily.setOnClickListener(v -> selectCategory("משפחה"));
        btnHome.setOnClickListener(v -> selectCategory("בית"));
        btnWork.setOnClickListener(v -> selectCategory("עבודה"));
        btnShopping.setOnClickListener(v -> selectCategory("קניות"));
        btnPersonal.setOnClickListener(v -> selectCategory("אישי"));
        btnHealth.setOnClickListener(v -> selectCategory("בריאות"));
        btnOther.setOnClickListener(v -> selectCategory("אחר"));

        // ===== חזרה =====
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // ===== RecyclerView =====
        recyclerTasks = findViewById(R.id.recyclerTasks);
        recyclerTasks.setLayoutManager(new LinearLayoutManager(this));
        recyclerTasks.setHasFixedSize(false);

        adapter = new HomeTasksAdapter(items);
        recyclerTasks.setAdapter(adapter);

        // ===== האזנה ל־Firestore =====
        listenToTasks();

        // ===== FAB =====
        findViewById(R.id.fabAddTask).setOnClickListener(v ->
                startActivity(new Intent(this, Add_Tasks.class))
        );
    }

    // ================= קטגוריה =================

    private void selectCategory(String category) {
        selectedCategory = category;

        resetCategoryButtons();
        highlightSelectedCategory(category);

        listenToTasks();
    }


    // ================= LISTENER =================

    private void listenToTasks() {

        FirebaseFirestore.getInstance()
                .collection("home_tasks")
                .document("defaultList")
                .collection("items")
                .addSnapshotListener((snapshot, e) -> {

                    if (e != null || snapshot == null) {
                        Toast.makeText(this, "שגיאה בטעינת משימות", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ArrayList<Task> activeTasks = new ArrayList<>();
                    ArrayList<Task> completedTasks = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        Task task = doc.toObject(Task.class);
                        task.setId(doc.getId());

                        // ===== סינון לפי קטגוריה =====
                        if (!selectedCategory.equals("הכל")) {
                            if (task.getCategory() == null ||
                                    !task.getCategory().equals(selectedCategory)) {
                                continue;
                            }
                        }

                        if (task.isCompleted()) {
                            completedTasks.add(task);
                        } else {
                            activeTasks.add(task);
                        }
                    }

                    // ===== מיון לפי עדיפות =====
                    sortTasks(activeTasks);
                    sortTasks(completedTasks);

                    // ===== בניית הרשימה =====
                    items.clear();
                    items.addAll(activeTasks);

                    if (!completedTasks.isEmpty()) {
                        items.add("DIVIDER");
                        items.addAll(completedTasks);
                    }

                    adapter.notifyDataSetChanged();

                    // ===== עדכון מונים =====
                    tvActiveCount.setText(String.valueOf(activeTasks.size()));
                    tvCompletedCount.setText(String.valueOf(completedTasks.size()));
                });
    }

    // ================= SORT =================

    private void sortTasks(ArrayList<Task> list) {
        Collections.sort(list, (t1, t2) ->
                priorityWeight(t1.getPriority()) - priorityWeight(t2.getPriority())
        );
    }

    private int priorityWeight(String priority) {
        if ("high".equals(priority)) return 0;
        if ("medium".equals(priority)) return 1;
        if ("low".equals(priority)) return 2;
        return 3;
    }

    // ================= DELETE COMPLETED =================

    public void showDeleteCompletedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("מחיקת משימות")
                .setMessage("האם את בטוחה שברצונך למחוק את כל המשימות שבוצעו?")
                .setPositiveButton("כן, למחוק", (dialog, which) -> deleteCompletedTasks())
                .setNegativeButton("ביטול", null)
                .show();
    }

    private void deleteCompletedTasks() {
        FirebaseFirestore.getInstance()
                .collection("home_tasks")
                .document("defaultList")
                .collection("items")
                .whereEqualTo("completed", true)
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (QueryDocumentSnapshot doc : snapshot) {
                        doc.getReference().delete();
                    }
                });
    }

    private void resetCategoryButtons() {
        btnAll.setBackgroundResource(R.drawable.bg_category_unselected);
        btnFamily.setBackgroundResource(R.drawable.bg_category_unselected);
        btnHome.setBackgroundResource(R.drawable.bg_category_unselected);
        btnWork.setBackgroundResource(R.drawable.bg_category_unselected);
        btnShopping.setBackgroundResource(R.drawable.bg_category_unselected);
        btnPersonal.setBackgroundResource(R.drawable.bg_category_unselected);
        btnHealth.setBackgroundResource(R.drawable.bg_category_unselected);
        btnOther.setBackgroundResource(R.drawable.bg_category_unselected);

        btnAll.setTextColor(getResources().getColor(android.R.color.black));
        btnFamily.setTextColor(getResources().getColor(android.R.color.black));
        btnHome.setTextColor(getResources().getColor(android.R.color.black));
        btnWork.setTextColor(getResources().getColor(android.R.color.black));
        btnShopping.setTextColor(getResources().getColor(android.R.color.black));
        btnPersonal.setTextColor(getResources().getColor(android.R.color.black));
        btnHealth.setTextColor(getResources().getColor(android.R.color.black));
        btnOther.setTextColor(getResources().getColor(android.R.color.black));
    }
    private void highlightSelectedCategory(String category) {
        Button selectedButton = null;

        switch (category) {
            case "הכל":
                selectedButton = btnAll;
                break;
            case "משפחה":
                selectedButton = btnFamily;
                break;
            case "בית":
                selectedButton = btnHome;
                break;
            case "עבודה":
                selectedButton = btnWork;
                break;
            case "קניות":
                selectedButton = btnShopping;
                break;
            case "אישי":
                selectedButton = btnPersonal;
                break;
            case "בריאות":
                selectedButton = btnHealth;
                break;
            case "אחר":
                selectedButton = btnOther;
                break;
        }

        if (selectedButton != null) {
            selectedButton.setBackgroundResource(R.drawable.bg_category_selected);
            selectedButton.setTextColor(Color.parseColor("#374151"));

        }
    }

}
