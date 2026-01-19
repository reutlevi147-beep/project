package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

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

    // ===== UI =====
    private RecyclerView recyclerTasks;
    private HomeTasksAdapter adapter;
    private final ArrayList<Object> items = new ArrayList<>();

    private TextView tvActiveCount;
    private TextView tvCompletedCount;

    private Button btnAll, btnFamily, btnHome, btnWork,
            btnShopping, btnPersonal, btnHealth, btnOther;

    // ===== STATE =====
    private String selectedCategory = "הכל";

    // ===== PREFS =====
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_GROUP_ID = "group_id";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        // ===== HEADER STATS =====
        tvActiveCount = findViewById(R.id.statActive)
                .findViewById(R.id.tvValue);
        tvCompletedCount = findViewById(R.id.statCompleted)
                .findViewById(R.id.tvValue);

        // ===== CATEGORY BUTTONS =====
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

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // ===== RECYCLER =====
        recyclerTasks = findViewById(R.id.recyclerTasks);
        recyclerTasks.setLayoutManager(new LinearLayoutManager(this));

        adapter = new HomeTasksAdapter(items);
        recyclerTasks.setAdapter(adapter);

        // ===== SAVE COMPLETED TO FIREBASE =====
        adapter.setOnTaskCheckedChangeListener((task, completed) -> {

            SharedPreferences prefs =
                    getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            String groupId = prefs.getString(KEY_GROUP_ID, null);

            if (groupId == null || task.getId() == null) return;

            FirebaseFirestore.getInstance()
                    .collection("groups")
                    .document(groupId)
                    .collection("home_tasks")
                    .document(task.getId())
                    .update("completed", completed);
        });

        // ===== DELETE COMPLETED =====
        adapter.setOnDeleteCompletedClickListener(this::showDeleteDialog);

        // ===== DATA =====
        listenToTasks();

        findViewById(R.id.fabAddTask).setOnClickListener(v ->
                startActivity(new Intent(this, Add_Tasks.class))
        );
    }

    // ================= CATEGORY =================

    private void selectCategory(String category) {
        selectedCategory = category;
        resetCategoryButtons();
        highlightSelectedCategory(category);
        listenToTasks();
    }

    // ================= FIRESTORE =================

    private void listenToTasks() {

        SharedPreferences prefs =
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String groupId = prefs.getString(KEY_GROUP_ID, null);

        if (groupId == null) return;

        FirebaseFirestore.getInstance()
                .collection("groups")
                .document(groupId)
                .collection("home_tasks")
                .addSnapshotListener((snapshot, e) -> {

                    if (snapshot == null) return;

                    ArrayList<Task> active = new ArrayList<>();
                    ArrayList<Task> completed = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : snapshot) {

                        Task task = doc.toObject(Task.class);
                        task.setId(doc.getId());

                        if (!"הכל".equals(selectedCategory)) {
                            if (task.getCategory() == null ||
                                    !selectedCategory.equals(task.getCategory())) {
                                continue;
                            }
                        }

                        if (task.isCompleted()) {
                            completed.add(task);
                        } else {
                            active.add(task);
                        }
                    }

                    sortTasks(active);
                    sortTasks(completed);

                    items.clear();
                    items.addAll(active);

                    if (!completed.isEmpty()) {
                        items.add("DIVIDER");
                        items.addAll(completed);
                    }

                    adapter.notifyDataSetChanged();

                    tvActiveCount.setText(String.valueOf(active.size()));
                    tvCompletedCount.setText(String.valueOf(completed.size()));
                });
    }

    // ================= DELETE =================

    private void showDeleteDialog() {

        SharedPreferences prefs =
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String groupId = prefs.getString(KEY_GROUP_ID, null);

        if (groupId == null) return;

        new AlertDialog.Builder(this)
                .setTitle("מחיקת משימות שבוצעו")
                .setMessage("למחוק את כל המשימות שסומנו כבוצעו?")
                .setPositiveButton("כן", (dialog, which) -> {

                    FirebaseFirestore.getInstance()
                            .collection("groups")
                            .document(groupId)
                            .collection("home_tasks")
                            .whereEqualTo("completed", true)
                            .get()
                            .addOnSuccessListener(snapshot -> {
                                for (QueryDocumentSnapshot doc : snapshot) {
                                    doc.getReference().delete();
                                }
                            });
                })
                .setNegativeButton("ביטול", null)
                .show();
    }

    // ================= SORT =================

    private void sortTasks(ArrayList<Task> list) {
        Collections.sort(list, (t1, t2) ->
                priorityWeight(t1.getPriority()) -
                        priorityWeight(t2.getPriority())
        );
    }

    private int priorityWeight(String priority) {
        if ("high".equals(priority)) return 0;
        if ("medium".equals(priority)) return 1;
        if ("low".equals(priority)) return 2;
        return 3;
    }

    // ================= UI HELPERS =================

    private void resetCategoryButtons() {
        Button[] buttons = {
                btnAll, btnFamily, btnHome, btnWork,
                btnShopping, btnPersonal, btnHealth, btnOther
        };

        for (Button b : buttons) {
            b.setBackgroundResource(R.drawable.bg_category_unselected);
            b.setTextColor(Color.BLACK);
        }
    }

    private void highlightSelectedCategory(String category) {
        Button selectedButton = null;

        switch (category) {
            case "הכל": selectedButton = btnAll; break;
            case "משפחה": selectedButton = btnFamily; break;
            case "בית": selectedButton = btnHome; break;
            case "עבודה": selectedButton = btnWork; break;
            case "קניות": selectedButton = btnShopping; break;
            case "אישי": selectedButton = btnPersonal; break;
            case "בריאות": selectedButton = btnHealth; break;
            case "אחר": selectedButton = btnOther; break;
        }

        if (selectedButton != null) {
            selectedButton.setBackgroundResource(R.drawable.bg_category_selected);
            selectedButton.setTextColor(Color.parseColor("#374151"));
        }
    }
}
