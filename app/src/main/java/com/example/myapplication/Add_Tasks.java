package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.CheckBox;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Add_Tasks extends AppCompatActivity {

    private EditText taskInput;
    private FirebaseFirestore db;

    // ✅ צ׳קבוקסים
    private CheckBox checkAll;
    private CheckBox checkUser1;
    private CheckBox checkUser2;
    private CheckBox checkUser3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_tasks);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        // 📝 שדה המשימה
        taskInput = findViewById(R.id.task);

        // ✅ חיבור הצ׳קבוקסים
        checkAll = findViewById(R.id.check_all);
        checkUser1 = findViewById(R.id.check_user1);
        checkUser2 = findViewById(R.id.check_user2);
        checkUser3 = findViewById(R.id.check_user3);

        // ✅ "כולם" מסמן / מבטל את כולם
        checkAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            checkUser1.setChecked(isChecked);
            checkUser2.setChecked(isChecked);
            checkUser3.setChecked(isChecked);
        });

        // 🔹 כפתור "הוספת משימה"
        Button addTask = findViewById(R.id.add_to_task);
        addTask.setOnClickListener(v -> saveTaskToFirestore());

        // 🔹 כפתור "Return"
        ImageButton returnToCalendar = findViewById(R.id.Return);
        returnToCalendar.setOnClickListener(v ->
                startActivity(new Intent(this, Display_Calender_Tasks.class))
        );
    }

    // ------------------------------------------------------
    // 📌 שמירת משימה + המבצעים שנבחרו
    // ------------------------------------------------------
    private void saveTaskToFirestore() {

        String taskTitle = taskInput.getText().toString().trim();

        if (taskTitle.isEmpty()) {
            Toast.makeText(this, "נא להכניס משימה", Toast.LENGTH_SHORT).show();
            return;
        }

        String assignedTo = getSelectedUsers();

        if (assignedTo.isEmpty()) {
            Toast.makeText(this, "נא לבחור מבצע משימה", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> taskData = new HashMap<>();
        taskData.put("title", taskTitle);
        taskData.put("assignedTo", assignedTo);
        taskData.put("isDone", false);
        taskData.put("createdAt", Timestamp.now());

        db.collection("home_tasks")
                .document("defaultList")
                .collection("items")
                .add(taskData)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "המשימה נשמרה!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, Display_Calender_Tasks.class));
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "שגיאה בשמירת משימה", Toast.LENGTH_SHORT).show()
                );
    }

    // ------------------------------------------------------
    // 📌 בניית מחרוזת מבצעים
    // ------------------------------------------------------
    private String getSelectedUsers() {

        if (checkAll.isChecked()) {
            return "כולם";
        }

        StringBuilder result = new StringBuilder();

        if (checkUser1.isChecked()) result.append("דנה, ");
        if (checkUser2.isChecked()) result.append("רון, ");
        if (checkUser3.isChecked()) result.append("יעל, ");

        if (result.length() > 0) {
            result.setLength(result.length() - 2); // הסרת פסיק ורווח
        }

        return result.toString();
    }
}
