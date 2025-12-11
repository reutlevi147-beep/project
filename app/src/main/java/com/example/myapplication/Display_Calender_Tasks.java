package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class Display_Calender_Tasks extends AppCompatActivity {

    RecyclerView recyclerTasks;
    TaskAdapter adapter;
    List<Task> taskList = new ArrayList<>();
    TextView btnShowAll;   // ← שינוי קטן: TextView כי זה מה שיש ב־XML

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_calender_tasks);

        // ---------------------- כפתור הוספת משימה ----------------------
        ImageButton plosTasks = findViewById(R.id.Plos);
        plosTasks.setOnClickListener(v ->
                startActivity(new Intent(this, Add_Tasks.class))
        );

        // ---------------------- כפתור הוספת אירוע ליומן ----------------------
        ImageButton plosCalendar = findViewById(R.id.plos);
        plosCalendar.setOnClickListener(v ->
                startActivity(new Intent(this, Add_Calender.class))
        );

        // ---------------------- "הצג את כל המשימות" ----------------------
        btnShowAll = findViewById(R.id.showMoreTasks);   // ← תוקן!!
        btnShowAll.setVisibility(View.GONE);

        btnShowAll.setOnClickListener(v -> openBottomSheet());

        // ---------------------- RecyclerView ----------------------
        recyclerTasks = findViewById(R.id.recyclerTasks);
        recyclerTasks.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TaskAdapter(taskList);
        recyclerTasks.setAdapter(adapter);

        // ---------------------- טעינת משימות ----------------------
        loadTasksFromFirestore();
    }

    private void loadTasksFromFirestore() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("home_tasks")
                .document("defaultList")
                .collection("items")
                .get()
                .addOnSuccessListener(query -> {

                    taskList.clear();

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        String id = doc.getId();
                        String title = doc.getString("title");
                        String assignedTo = doc.getString("assignedTo");
                        Boolean done = doc.getBoolean("isDone");

                        boolean isDone = (done != null) ? done : false;

                        taskList.add(new Task(id, title, assignedTo, isDone));
                    }

                    applyPreviewLimit();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "שגיאה בטעינת משימות", Toast.LENGTH_SHORT).show()
                );
    }

    private void applyPreviewLimit() {

        List<Task> previewList = new ArrayList<>();

        if (taskList.size() > 3) {
            previewList.add(taskList.get(0));
            previewList.add(taskList.get(1));
            previewList.add(taskList.get(2));

            btnShowAll.setVisibility(View.VISIBLE);
        } else {
            previewList.addAll(taskList);
            btnShowAll.setVisibility(View.GONE);
        }

        adapter = new TaskAdapter(previewList);
        recyclerTasks.setAdapter(adapter);
    }

    private void openBottomSheet() {

        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.bottomsheet_all_tasks, null);

        RecyclerView fullList = sheetView.findViewById(R.id.recyclerAllTasks);
        fullList.setLayoutManager(new LinearLayoutManager(this));

        fullList.setAdapter(new TaskAdapter(taskList));

        bottomSheet.setContentView(sheetView);
        bottomSheet.show();
    }
}
