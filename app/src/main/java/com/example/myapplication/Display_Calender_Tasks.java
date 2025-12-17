package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

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
    TextView btnShowAll;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_calender_tasks);

        Log.d("STEP1", "Display_Calender_Tasks onCreate");

        ImageButton plosTasks = findViewById(R.id.Plos);
        plosTasks.setOnClickListener(v ->
                startActivity(new Intent(this, Add_Tasks.class))
        );

        ImageButton plosCalendar = findViewById(R.id.plos);
        plosCalendar.setOnClickListener(v ->
                startActivity(new Intent(this, Add_Calender.class))
        );

        btnShowAll = findViewById(R.id.showMoreTasks);
        btnShowAll.setVisibility(View.GONE);
        btnShowAll.setOnClickListener(v -> openBottomSheet());

        recyclerTasks = findViewById(R.id.recyclerTasks);
        recyclerTasks.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TaskAdapter(taskList);
        recyclerTasks.setAdapter(adapter);

        loadTasksFromFirestore();
    }

    // ====================== טעינה מ־Firestore (מתוקן) ======================
    private void loadTasksFromFirestore() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("home_tasks")
                .document("defaultList")
                .collection("items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    taskList.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {

                        String title = doc.getString("title");
                        String assignedTo = doc.getString("assignedTo");
                        Boolean isDone = doc.getBoolean("isDone");

                        Task task = new Task(
                                doc.getId(),
                                title != null ? title : "",
                                assignedTo != null ? assignedTo : "",
                                isDone != null && isDone
                        );

                        taskList.add(task);
                    }

                    applyPreviewLimit();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "שגיאה בטעינת משימות",
                                Toast.LENGTH_SHORT).show()
                );
    }

    // ====================== תצוגה מקדימה ======================
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

    // ====================== BottomSheet ======================
    private void openBottomSheet() {

        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater()
                .inflate(R.layout.bottomsheet_all_tasks, null);

        RecyclerView fullList = sheetView.findViewById(R.id.recyclerAllTasks);
        fullList.setLayoutManager(new LinearLayoutManager(this));
        fullList.setAdapter(new TaskAdapter(taskList));

        bottomSheet.setContentView(sheetView);
        bottomSheet.show();
    }
}
