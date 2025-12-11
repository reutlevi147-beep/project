package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;

    public TaskAdapter(List<Task> taskList) {
        this.taskList = taskList;
    }

    @NonNull
    @Override

    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.txtTaskName.setText(task.getTaskName());
        holder.txtAssignedTo.setText("עבור: " + task.getAssignedTo());
        holder.checkDone.setChecked(task.isCompleted());
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {

        CheckBox checkDone;
        TextView txtTaskName, txtAssignedTo;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);

            checkDone = itemView.findViewById(R.id.checkDone);
            txtTaskName = itemView.findViewById(R.id.txtTaskName);
            txtAssignedTo = itemView.findViewById(R.id.txtAssignedTo);

        }
    }
}


