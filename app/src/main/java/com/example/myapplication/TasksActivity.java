package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TasksActivity extends AppCompatActivity {

    private RecyclerView recyclerTasks;
    private HomeTasksAdapter adapter;
    private ArrayList<Task> tasks;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        findViewById(R.id.fabAddTask).setOnClickListener(v -> {
            Intent intent = new Intent(TasksActivity.this, Add_Tasks.class);
            startActivity(intent);
        });


        // כפתור חזרה
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // RecyclerView
        recyclerTasks = findViewById(R.id.recyclerTasks);
        recyclerTasks.setLayoutManager(new LinearLayoutManager(this));
        recyclerTasks.setHasFixedSize(false);

        // נתוני דמו (רק לשלב הזה)
        tasks = new ArrayList<>();
        tasks.add(new Task("ארגון ערב משפחתי", false));
        tasks.add(new Task("תשלום חשבון חשמל", false));
        tasks.add(new Task("קניית מתנה ליום הולדת", true));

        adapter = new HomeTasksAdapter(tasks);
        recyclerTasks.setAdapter(adapter);
    }
}
