package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class Display_Calender_Tasks extends AppCompatActivity {

    RecyclerView recyclerTasks;
    TaskAdapter adapter;
    List<Task> taskList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_display_calender_tasks);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // כפתור להוספת משימה
        ImageButton plosTasks = findViewById(R.id.Plos);
        plosTasks.setOnClickListener(v ->
                startActivity(new Intent(this, Add_Tasks.class))
        );

        // כפתור להוספת אירוע ביומן
        ImageButton plosCalendar = findViewById(R.id.plos);
        plosCalendar.setOnClickListener(v ->
                startActivity(new Intent(this, Add_Calender.class))
        );


        // ===================== RecyclerView =====================
        recyclerTasks = findViewById(R.id.recyclerTasks);
        recyclerTasks.setLayoutManager(new LinearLayoutManager(this));

        // נתוני דמה לבדיקה
        taskList.add(new Task("1", "לסדר את הסלון", "אמא", false));
        taskList.add(new Task("2", "להוציא את זואי", "אני", true));
        taskList.add(new Task("3", "להכין אוכל", "אבא", false));

        adapter = new TaskAdapter(taskList);
        recyclerTasks.setAdapter(adapter);
    }
}
