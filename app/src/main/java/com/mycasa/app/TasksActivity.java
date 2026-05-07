package com.mycasa.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.google.firebase.firestore.ListenerRegistration;

import eightbitlab.com.blurview.BlurView;

public class TasksActivity extends BaseActivity {

    private RecyclerView recyclerTasks;
    private TasksAdapter adapter;
    private final List<Task> tasks = new ArrayList<>();

    private TextView tvActiveCount;
    private TextView tvCompletedCount;

    private FloatingActionButton fabAdd;

    private Button btnAll, btnFamily, btnHome, btnWork,
            btnShopping, btnPersonal, btnHealth, btnOther;

    private String selectedCategory = "הכל";

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_GROUP_ID = "group_id";
    private static final String KEY_USER_ID = "user_id";
    private ListenerRegistration tasksListener;
    private String groupId;
    private View lockOverlay;
    private PagePermission currentPermission = PagePermission.VIEW_ONLY;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        recyclerTasks = findViewById(R.id.recyclerTasks);
        fabAdd = findViewById(R.id.fabAddTask);

        View statActive = findViewById(R.id.statActive);
        tvActiveCount = statActive.findViewById(R.id.tvValue);
        ((TextView) statActive.findViewById(R.id.tvLabel)).setText("פעילות");
        View statCompleted = findViewById(R.id.statCompleted);
        tvCompletedCount = statCompleted.findViewById(R.id.tvValue);
        ((TextView) statCompleted.findViewById(R.id.tvLabel)).setText("בוצעו");

        recyclerTasks.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TasksAdapter(this, tasks);
        recyclerTasks.setAdapter(adapter);
        adapter.setOnDeleteCompletedListener(() -> deleteCompletedTasks());

        setupCategoryButtons();

        SharedPreferences prefs =
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        groupId = prefs.getString(KEY_GROUP_ID, null);
        String userId = prefs.getString(KEY_USER_ID, null);

        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(this, Add_Tasks.class)));

        // ===== מחיקה אמיתית =====
        adapter.setOnTaskDeleteListener(task -> {
            FirebaseFirestore.getInstance()
                    .collection("groups")
                    .document(groupId)
                    .collection("home_tasks")
                    .document(task.getId())
                    .delete();
        });

        // ===== סימון בוצע =====
        adapter.setOnTaskCheckedChangeListener((task, completed) -> {
            FirebaseFirestore.getInstance()
                    .collection("groups")
                    .document(groupId)
                    .collection("home_tasks")
                    .document(task.getId())
                    .update("completed", completed);
        });

        BlurView blurView = findViewById(R.id.lockOverlay);
        View decorView = getWindow().getDecorView();
        ViewGroup rootView = (ViewGroup) decorView.findViewById(android.R.id.content);
        lockOverlay = findViewById(R.id.lockOverlay);
        blurView.setupWith(rootView)
                .setFrameClearDrawable(getWindow().getDecorView().getBackground())
                .setBlurRadius(25f);

        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(TasksActivity.this, Home.class));
            finish(); // חשוב כדי לא לחזור שוב למשימות
        });

        // ===== הרשאות =====
        resolvePermissionFromServer(
                AppPage.TASKS,
                groupId,
                userId,
                permission -> {

                    currentPermission = permission;

                    // 🔒 אין הרשאת צפייה
                    if (permission == PagePermission.LOCKED || permission == null) {

                        lockOverlay.setVisibility(View.VISIBLE);

                        fabAdd.setVisibility(View.GONE);

                        recyclerTasks.setEnabled(false);

                        adapter.setAllowEdit(false);
                        adapter.setAllowDelete(false);
                        adapter.setAllowToggle(false);

                        return;
                    }

                    // מסך פתוח
                    lockOverlay.setVisibility(View.GONE);

                    switch (permission) {

                        case VIEW_ONLY:

                            fabAdd.setVisibility(View.GONE);

                            adapter.setAllowEdit(false);
                            adapter.setAllowDelete(false);
                            adapter.setAllowToggle(false);

                            break;

                        case ADD_ONLY:

                            fabAdd.setVisibility(View.VISIBLE);

                            adapter.setAllowEdit(false);
                            adapter.setAllowDelete(false);
                            adapter.setAllowToggle(false);

                            break;

                        case ADD_EDIT:

                            fabAdd.setVisibility(View.VISIBLE);

                            adapter.setAllowEdit(true);
                            adapter.setAllowDelete(false);
                            adapter.setAllowToggle(true);

                            break;

                        case FULL_ACCESS:

                            fabAdd.setVisibility(View.VISIBLE);

                            adapter.setAllowEdit(true);
                            adapter.setAllowDelete(true);
                            adapter.setAllowToggle(true);

                            break;
                    }
                }
        );


        listenToTasks();
    }

    // ===========================
    // FIRESTORE
    // ===========================

    private void listenToTasks() {

        if (groupId == null) return;

        // סוגר listener קודם אם קיים
        if (tasksListener != null) {
            tasksListener.remove();
        }

        tasksListener = FirebaseFirestore.getInstance()
                .collection("groups")
                .document(groupId)
                .collection("home_tasks")
                .addSnapshotListener((snapshot, e) -> {

                    if (snapshot == null) return;

                    List<Task> active = new ArrayList<>();
                    List<Task> completed = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : snapshot) {

                        Task task = doc.toObject(Task.class);
                        task.setId(doc.getId());

                        // ===== פילטר קטגוריה =====
                        if (!"הכל".equals(selectedCategory)) {
                            if (task.getCategory() == null ||
                                    !selectedCategory.equals(task.getCategory())) {
                                continue;
                            }
                        }

                        // ===== חלוקה לפעיל / בוצע =====
                        if (task.isCompleted())
                            completed.add(task);
                        else
                            active.add(task);
                    }

                    // ===== מיון =====
                    sortTasks(active);
                    sortTasks(completed);

                    // ===== בניית הרשימה =====
                    tasks.clear();
                    tasks.addAll(active);

                    if (!completed.isEmpty()) {
                        tasks.add(null); // divider
                        tasks.addAll(completed);
                    }

                    adapter.notifyDataSetChanged();

                    // ===== עדכון סטטיסטיקות =====
                    tvActiveCount.setText(String.valueOf(active.size()));
                    tvCompletedCount.setText(String.valueOf(completed.size()));
                });
    }

    private void deleteCompletedTasks() {

        if (groupId == null) return;

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
    }

    // ===========================
    // SORT
    // ===========================

    private void sortTasks(List<Task> list) {
        Collections.sort(list, (t1, t2) -> {

            long today = System.currentTimeMillis();

            boolean t1NoDate = t1.getDueDate() == null;
            boolean t2NoDate = t2.getDueDate() == null;

            boolean t1Expired = t1.getDueDate() != null && t1.getDueDate() < today;
            boolean t2Expired = t2.getDueDate() != null && t2.getDueDate() < today;

            int group1 = getSortGroup(t1NoDate, t1Expired);
            int group2 = getSortGroup(t2NoDate, t2Expired);

            if (group1 != group2) {
                return group1 - group2;
            }

            if (t1.getDueDate() != null && t2.getDueDate() != null) {
                int dateCompare = Long.compare(t1.getDueDate(), t2.getDueDate());
                if (dateCompare != 0) return dateCompare;
            }

            return priorityWeight(t1.getPriority()) - priorityWeight(t2.getPriority());
        });
    }

    private int getSortGroup(boolean noDate, boolean expired) {
        if (noDate) return 0;     // בלי תאריך — הכי למעלה
        if (expired) return 1;    // תאריך שעבר — אחר כך למעלה
        return 2;                 // תאריכים עתידיים
    }

    private int priorityWeight(String priority) {
        if ("high".equals(priority)) return 0;
        if ("medium".equals(priority)) return 1;
        if ("low".equals(priority)) return 2;
        return 3;
    }

    // ===========================
    // CATEGORIES
    // ===========================

    private void setupCategoryButtons() {

        btnAll = findViewById(R.id.btnAll);
        btnFamily = findViewById(R.id.btnFamily);
        btnHome = findViewById(R.id.btnHome);
        btnWork = findViewById(R.id.btnWork);
        btnShopping = findViewById(R.id.btnShopping);
        btnPersonal = findViewById(R.id.btnPersonal);
        btnHealth = findViewById(R.id.btnHealth);
        btnOther = findViewById(R.id.btnOther);

        View.OnClickListener listener = v -> {

            resetCategoryButtons();

            Button clicked = (Button) v;
            clicked.setBackgroundResource(R.drawable.bg_category_selected);
            clicked.setTextColor(getColor(android.R.color.black));

            selectedCategory = clicked.getText().toString();
            listenToTasks();
        };

        btnAll.setOnClickListener(listener);
        btnFamily.setOnClickListener(listener);
        btnHome.setOnClickListener(listener);
        btnWork.setOnClickListener(listener);
        btnShopping.setOnClickListener(listener);
        btnPersonal.setOnClickListener(listener);
        btnHealth.setOnClickListener(listener);
        btnOther.setOnClickListener(listener);
    }

    private void resetCategoryButtons() {

        Button[] buttons = {
                btnAll, btnFamily, btnHome, btnWork,
                btnShopping, btnPersonal, btnHealth, btnOther
        };

        for (Button b : buttons) {
            b.setBackgroundResource(R.drawable.bg_category_unselected);
            b.setTextColor(getColor(R.color.gray_500));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (tasksListener != null) {
            tasksListener.remove();
        }
    }
}