package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
                .inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);

        holder.tvTitle.setText(task.getTitle());

        // כרגע דמו – אפשר לשפר בהמשך
        holder.tvPriority.setText(
                task.getPriority() != null ? task.getPriority() : "רגילה"
        );

        holder.tvCategory.setText(
                task.getCategory() != null ? task.getCategory() : "כללי"
        );

        holder.tvDate.setText(
                task.getDueDate() != null ? task.getDueDate() : ""
        );

        // מצב ויזואלי של סימון
        holder.imgCheck.setAlpha(task.isCompleted() ? 1f : 0.4f);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {

        ImageView imgCheck;
        TextView tvTitle, tvPriority, tvCategory, tvDate;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);

            imgCheck = itemView.findViewById(R.id.imgCheck);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPriority = itemView.findViewById(R.id.tvPriority);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}
