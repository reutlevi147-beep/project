package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class HomeTasksAdapter extends RecyclerView.Adapter<HomeTasksAdapter.TaskViewHolder> {

    private final List<Task> tasks;

    public HomeTasksAdapter(List<Task> tasks) {
        this.tasks = tasks;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);

        holder.taskTitle.setText(task.getTaskName());

        // מניעת טריגר כפול
        holder.checkDone.setOnCheckedChangeListener(null);

        holder.checkDone.setChecked(task.isCompleted());

        // ✅ שלב 1: עדכון Firestore בלחיצה
        holder.checkDone.setOnCheckedChangeListener((buttonView, isChecked) -> {

            FirebaseFirestore.getInstance()
                    .collection("home_tasks")
                    .document("defaultList")
                    .collection("items")
                    .document(task.getId())
                    .update("isDone", isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {

        TextView taskTitle;
        CheckBox checkDone;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.txtTaskTitle);
            checkDone = itemView.findViewById(R.id.checkDone);
        }
    }
}
