package com.example.myapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Add_Tasks extends AppCompatActivity {

    private EditText etTitle;
    private EditText etDate;

    private FirebaseFirestore db;

    private String selectedPriority = null;
    private String selectedCategory = null;
    private View selectedCategoryView = null;

    private Long selectedDateMillis = null;

    // 👥 Users
    private RecyclerView recyclerUsers;
    private UsersAssignAdapter usersAdapter;
    private final ArrayList<AppUser> usersList = new ArrayList<>();

    // נשמור כאן את מי שנבחר (בהמשך)
    private final ArrayList<String> selectedUserIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_tasks);

        db = FirebaseFirestore.getInstance();

        etTitle = findViewById(R.id.etTitle);
        etDate = findViewById(R.id.etDate);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnSave).setOnClickListener(v -> saveTaskToFirestore());

        setupPrioritySelection();
        setupCategorySelection();

        findViewById(R.id.btnSelectAllUsers).setOnClickListener(v -> {
            usersAdapter.selectAll();
        });

        // 👥 RecyclerView users
        recyclerUsers = findViewById(R.id.recyclerUsers);
        recyclerUsers.setLayoutManager(new GridLayoutManager(this, 2));
        usersAdapter = new UsersAssignAdapter(usersList);
        recyclerUsers.setAdapter(usersAdapter);

        loadUsersFromFirestore();

        etDate.setOnClickListener(v -> showDatePicker());
    }

    // ================= USERS =================

    private void loadUsersFromFirestore() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String groupId = prefs.getString("group_id", null);

        if (groupId == null) {
            Toast.makeText(this, "❌ group_id לא קיים", Toast.LENGTH_LONG).show();
            return;
        }

        db.collection("groups")
                .document(groupId)
                .collection("users")
                .get()
                .addOnSuccessListener(snapshot -> {

                    usersList.clear();

                    for (DocumentSnapshot doc : snapshot) {
                        AppUser user = doc.toObject(AppUser.class);
                        if (user == null || user.getName() == null) continue;

                        user.setDocumentId(doc.getId());
                        usersList.add(user);
                    }

                    usersAdapter.notifyDataSetChanged();

                    if (usersList.isEmpty()) {
                        Toast.makeText(this, "⚠️ אין משתמשים בקבוצה", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "שגיאה בטעינת משתמשים", Toast.LENGTH_SHORT).show()
                );
    }

    // ================= TASK =================

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
        task.put("assignedUserIds", selectedUserIds);
        task.put("createdAt", Timestamp.now());
        task.put("isDone", false);

        if (selectedDateMillis != null) {
            task.put("dueDate", selectedDateMillis);
        }

        db.collection("home_tasks")
                .document("defaultList")
                .collection("items")
                .add(task)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "המשימה נוספה", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    // ================= PRIORITY =================

    private void setupPrioritySelection() {
        View high = findViewById(R.id.circleHigh);
        View medium = findViewById(R.id.circleMedium);
        View low = findViewById(R.id.circleLow);

        findViewById(R.id.cardPriorityHigh)
                .setOnClickListener(v -> selectPriority("high", high, medium, low));
        findViewById(R.id.cardPriorityMedium)
                .setOnClickListener(v -> selectPriority("medium", medium, high, low));
        findViewById(R.id.cardPriorityLow)
                .setOnClickListener(v -> selectPriority("low", low, high, medium));
    }

    private void selectPriority(String value, View selected, View o1, View o2) {
        selected.setBackgroundResource(R.drawable.bg_selectable_circle_selected);
        o1.setBackgroundResource(R.drawable.bg_selectable_circle);
        o2.setBackgroundResource(R.drawable.bg_selectable_circle);
        selectedPriority = value;
    }

    // ================= CATEGORY =================

    private void setupCategorySelection() {
        setupCategoryClick(R.id.cardCatHome, "בית");
        setupCategoryClick(R.id.cardCatWork, "עבודה");
        setupCategoryClick(R.id.cardCatShopping, "קניות");
        setupCategoryClick(R.id.cardCatPersonal, "אישי");
        setupCategoryClick(R.id.cardCatHealth, "בריאות");
        setupCategoryClick(R.id.cardCatOther, "אחר");
    }

    private void setupCategoryClick(int viewId, String category) {
        View card = findViewById(viewId);

        card.setOnClickListener(v -> {
            if (selectedCategoryView != null) {
                selectedCategoryView.setForeground(null);
            }

            v.setForeground(getDrawable(R.drawable.bg_category_selected));
            selectedCategoryView = v;
            selectedCategory = category;
        });
    }

    // ================= DATE =================

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {

                    Calendar selectedCal = Calendar.getInstance();
                    selectedCal.set(year, month, dayOfMonth, 0, 0, 0);

                    selectedDateMillis = selectedCal.getTimeInMillis();
                    etDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }
}
