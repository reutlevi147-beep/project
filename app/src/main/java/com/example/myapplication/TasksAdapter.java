package com.example.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.TaskViewHolder> {

    private final Context context;
    private final List<Task> tasks;

    public TasksAdapter(Context context, List<Task> tasks) {
        this.context = context;
        this.tasks = tasks;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder h, int position) {
        Task task = tasks.get(position);

        // טקסטים
        h.tvTitle.setText(task.getTitle());
        h.tvCategory.setText(task.getCategory());
        h.tvDate.setText(task.getDueDate());
        h.tvPriority.setText(getPriorityLabel(task.getPriority()));

        // UI לפי סטטוס
        if (task.isCompleted()) {
            h.cardTask.setCardBackgroundColor(Color.parseColor("#F3F4F6"));
            h.tvTitle.setTextColor(Color.parseColor("#9CA3AF"));
            h.tvDate.setTextColor(Color.parseColor("#9CA3AF"));
            h.tvCategory.setAlpha(0.6f);
            h.imgCheck.setAlpha(1f);

            h.tvTitle.setPaintFlags(
                    h.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
            );
        } else {
            h.cardTask.setCardBackgroundColor(Color.WHITE);
            h.tvTitle.setTextColor(Color.parseColor("#111827"));
            h.tvDate.setTextColor(Color.parseColor("#6B7280"));
            h.tvCategory.setAlpha(1f);
            h.imgCheck.setAlpha(0.45f);

            h.tvTitle.setPaintFlags(
                    h.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG)
            );
        }

        // לחיצה על צ׳ק
        h.imgCheck.setOnClickListener(v -> {
            task.setCompleted(!task.isCompleted());
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    // ---------- ViewHolder ----------
    static class TaskViewHolder extends RecyclerView.ViewHolder {

        CardView cardTask;
        ImageView imgCheck;
        TextView tvTitle, tvCategory, tvDate, tvPriority;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cardTask = itemView.findViewById(R.id.cardTask);
            imgCheck = itemView.findViewById(R.id.imgCheck);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvPriority = itemView.findViewById(R.id.tvPriority);
        }
    }

    // ---------- Helpers ----------
    private String getPriorityLabel(String priority) {
        if (priority == null) return "";
        switch (priority) {
            case "high":
                return "גבוהה";
            case "medium":
                return "בינונית";
            case "low":
                return "נמוכה";
            default:
                return "";
        }
    }
}
