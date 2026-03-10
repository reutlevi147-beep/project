package com.mycasa.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TasksAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_TASK = 0;
    private static final int TYPE_DIVIDER = 1;

    private final Context context;
    private final List<Task> tasks;

    private boolean allowEdit = true;
    private boolean allowDelete = true;
    private boolean allowToggle = true;

    public void setAllowEdit(boolean value) { allowEdit = value; }
    public void setAllowDelete(boolean value) { allowDelete = value; }
    public void setAllowToggle(boolean value) { allowToggle = value; }

    public interface OnTaskCheckedChangeListener {
        void onTaskChecked(Task task, boolean completed);
    }

    public interface OnTaskDeleteListener {
        void onTaskDelete(Task task);
    }

    private OnTaskCheckedChangeListener checkedListener;
    private OnTaskDeleteListener deleteListener;

    public void setOnTaskCheckedChangeListener(OnTaskCheckedChangeListener l) {
        checkedListener = l;
    }

    public void setOnTaskDeleteListener(OnTaskDeleteListener l) {
        deleteListener = l;
    }

    public TasksAdapter(Context context, List<Task> tasks) {
        this.context = context;
        this.tasks = tasks;
    }

    @Override
    public int getItemViewType(int position) {
        return tasks.get(position) == null ? TYPE_DIVIDER : TYPE_TASK;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        if (viewType == TYPE_DIVIDER) {
            View v = LayoutInflater.from(context)
                    .inflate(R.layout.item_tasks_divider, parent, false);
            return new DividerHolder(v);
        }

        View view = LayoutInflater.from(context)
                .inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder,
            int position) {

        if (getItemViewType(position) == TYPE_DIVIDER) return;

        TaskViewHolder h = (TaskViewHolder) holder;
        Task task = tasks.get(position);

        h.tvTitle.setText(task.getTitle());
        h.tvCategory.setText(task.getCategory());

        if (task.getDueDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            h.tvDate.setText(
                    sdf.format(new Date(task.getDueDate())));
        } else {
            h.tvDate.setText("");
        }

        // UI completed
        if (task.isCompleted()) {
            h.cardTask.setCardBackgroundColor(Color.parseColor("#F3F4F6"));
            h.tvTitle.setTextColor(Color.parseColor("#9CA3AF"));
            h.tvTitle.setPaintFlags(
                    h.tvTitle.getPaintFlags()
                            | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            h.cardTask.setCardBackgroundColor(Color.WHITE);
            h.tvTitle.setTextColor(Color.parseColor("#111827"));
            h.tvTitle.setPaintFlags(
                    h.tvTitle.getPaintFlags()
                            & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        // Edit
        if (allowEdit) {
            h.itemView.setOnClickListener(v -> {
                Intent i = new Intent(context, Add_Tasks.class);
                i.putExtra("taskId", task.getId());
                context.startActivity(i);
            });
        } else {
            h.itemView.setOnClickListener(null);
        }

        // Delete
        if (h.btnDelete != null) {
            h.btnDelete.setVisibility(
                    allowDelete ? View.VISIBLE : View.GONE);

            h.btnDelete.setOnClickListener(v -> {
                if (deleteListener != null)
                    deleteListener.onTaskDelete(task);
            });
        }

        // Toggle
        if (allowToggle) {
            h.imgCheck.setOnClickListener(v -> {
                boolean newState = !task.isCompleted();
                if (checkedListener != null)
                    checkedListener.onTaskChecked(task, newState);
            });
        } else {
            h.imgCheck.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder
            extends RecyclerView.ViewHolder {

        CardView cardTask;
        ImageView imgCheck;
        ImageButton btnDelete;
        TextView tvTitle, tvCategory, tvDate;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cardTask = itemView.findViewById(R.id.cardTask);
            imgCheck = itemView.findViewById(R.id.imgCheck);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }

    static class DividerHolder
            extends RecyclerView.ViewHolder {

        DividerHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}