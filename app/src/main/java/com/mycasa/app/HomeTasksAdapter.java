package com.mycasa.app;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HomeTasksAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_TASK = 0;
    private static final int TYPE_DIVIDER = 1;

    private final List<Object> items;

    // ===== listeners =====
    public interface OnTaskCheckedChangeListener {
        void onTaskChecked(Task task, boolean completed);
    }

    public interface OnDeleteCompletedClickListener {
        void onDeleteCompleted();
    }

    private OnTaskCheckedChangeListener checkedListener;
    private OnDeleteCompletedClickListener deleteListener;

    public void setOnTaskCheckedChangeListener(OnTaskCheckedChangeListener l) {
        checkedListener = l;
    }

    public void setOnDeleteCompletedClickListener(OnDeleteCompletedClickListener l) {
        deleteListener = l;
    }

    public HomeTasksAdapter(List<Object> items) {
        this.items = items;
    }

    // ================= VIEW TYPE =================

    @Override
    public int getItemViewType(int position) {
        return (items.get(position) instanceof String)
                ? TYPE_DIVIDER
                : TYPE_TASK;
    }

    // ================= CREATE =================

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_DIVIDER) {
            View v = inflater.inflate(R.layout.item_tasks_divider, parent, false);
            return new DividerVH(v);
        }

        View v = inflater.inflate(R.layout.task_item, parent, false);
        return new TaskVH(v);
    }

    // ================= BIND =================

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof DividerVH) {
            DividerVH h = (DividerVH) holder;
            h.btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDeleteCompleted();
                }
            });
            return;
        }

        TaskVH h = (TaskVH) holder;
        Task task = (Task) items.get(position);

        // RESET
        h.tvTitle.setPaintFlags(h.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        h.tvTitle.setTextColor(Color.parseColor("#111827"));
        h.tvCategory.setAlpha(1f);
        h.tvPriority.setAlpha(1f);

        h.tvTitle.setText(task.getTitle());
        h.tvCategory.setText(task.getCategory() != null ? task.getCategory() : "");

        // PRIORITY
        String priority = task.getPriority();
        if ("high".equals(priority)) {
            h.tvPriority.setText("גבוהה");
            h.tvPriority.setBackgroundResource(R.drawable.bg_priority_high);
            h.tvPriority.setTextColor(Color.parseColor("#DC2626"));
        } else if ("medium".equals(priority)) {
            h.tvPriority.setText("בינונית");
            h.tvPriority.setBackgroundResource(R.drawable.bg_priority_medium);
            h.tvPriority.setTextColor(Color.parseColor("#F59E0B"));
        } else if ("low".equals(priority)) {
            h.tvPriority.setText("נמוכה");
            h.tvPriority.setBackgroundResource(R.drawable.bg_priority_low);
            h.tvPriority.setTextColor(Color.parseColor("#16A34A"));
        } else {
            h.tvPriority.setText("");
            h.tvPriority.setBackground(null);
        }

        applyCompletedState(h, task.isCompleted());

        h.imgCheck.setOnClickListener(v -> {
            boolean newState = !task.isCompleted();
            task.setCompleted(newState);
            applyCompletedState(h, newState);

            if (checkedListener != null) {
                checkedListener.onTaskChecked(task, newState);
            }
        });
    }

    private void applyCompletedState(TaskVH h, boolean completed) {
        if (completed) {
            h.imgCheck.setBackgroundResource(R.drawable.bg_check_done);
            h.imgCheck.setColorFilter(Color.parseColor("#16A34A"));
            h.tvTitle.setPaintFlags(
                    h.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
            );
            h.tvTitle.setTextColor(Color.parseColor("#6B7280"));
            h.tvCategory.setAlpha(0.5f);
            h.tvPriority.setAlpha(0.5f);
        } else {
            h.imgCheck.setBackgroundResource(R.drawable.bg_check_circle);
            h.imgCheck.setColorFilter(Color.parseColor("#9CA3AF"));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ================= HOLDERS =================

    static class TaskVH extends RecyclerView.ViewHolder {
        ImageView imgCheck;
        TextView tvTitle, tvCategory, tvPriority;

        TaskVH(@NonNull View v) {
            super(v);
            imgCheck = v.findViewById(R.id.imgCheck);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvCategory = v.findViewById(R.id.tvCategory);
            tvPriority = v.findViewById(R.id.tvPriority);
        }
    }

    static class DividerVH extends RecyclerView.ViewHolder {
        Button btnDelete;

        DividerVH(@NonNull View v) {
            super(v);
            btnDelete = v.findViewById(R.id.btnDeleteCompleted);
        }
    }
}
