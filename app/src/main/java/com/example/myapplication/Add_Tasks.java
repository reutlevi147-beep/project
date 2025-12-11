package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ArrayAdapter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Add_Tasks extends AppCompatActivity {

    private EditText taskInput;
    private Spinner spinnerUsers;
    private FirebaseFirestore db;

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

        // 👤 Spinner של המשתמשים
        spinnerUsers = findViewById(R.id.spinnerUsers);

        // 📌 טעינת המשתמשים מ-Firestore לתוך Spinner
        loadUsersIntoSpinner();

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
    // 📌 פונקציה: טעינת המשתמשים מה-Firestore ל-Spinner
    // ------------------------------------------------------
    private void loadUsersIntoSpinner() {

        List<String> usersList = new ArrayList<>();
        usersList.add("מבצע המשימה"); // ברירת מחדל

        db.collection("users")
                .get()
                .addOnSuccessListener(query -> {

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        String name = doc.getString("name");
                        if (name != null && !name.isEmpty()) {
                            usersList.add(name);
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            Add_Tasks.this,
                            android.R.layout.simple_spinner_item,
                            usersList
                    );

                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerUsers.setAdapter(adapter);

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "שגיאה בטעינת משתמשים", Toast.LENGTH_SHORT).show()
                );
    }

    // ------------------------------------------------------
    // 📌 פונקציה: שמירת משימה ל-Firestore
    // ------------------------------------------------------
    private void saveTaskToFirestore() {

        String taskTitle = taskInput.getText().toString().trim();

        if (taskTitle.isEmpty()) {
            Toast.makeText(this, "נא להכניס משימה", Toast.LENGTH_SHORT).show();
            return;
        }

        String assignedUser = spinnerUsers.getSelectedItem().toString();

        if (assignedUser.equals("מבצע המשימה")) {
            Toast.makeText(this, "נא לבחור מבצע משימה", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✨ יצירת מבנה נתונים לשמירה
        Map<String, Object> taskData = new HashMap<>();
        taskData.put("title", taskTitle);
        taskData.put("assignedTo", assignedUser);
        taskData.put("isDone", false);
        taskData.put("createdAt", Timestamp.now());

        // 📌 שמירה ב-Firestore
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
}
