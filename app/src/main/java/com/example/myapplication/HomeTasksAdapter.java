package com.example.myapplication;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class HomeTasksAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_TASK = 0;
    private static final int TYPE_DIVIDER = 1;

    private final List<Object> items;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

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
            return new DividerViewHolder(v);
        }

        View v = inflater.inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(v);
    }

    // ================= BIND =================

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof DividerViewHolder) {
            return;
        }

        TaskViewHolder h = (TaskViewHolder) holder;
        Task task = (Task) items.get(position);

        // ========= RESET מוחלט =========
        h.tvTitle.setPaintFlags(h.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        h.tvTitle.setTextColor(Color.parseColor("#111827"));
        h.tvPriority.setAlpha(1f);
        h.tvCategory.setAlpha(1f);
        h.imgCheck.setAlpha(1f);
        h.imgCheck.setColorFilter(null);
        h.imgCheck.setBackgroundResource(R.drawable.bg_check_circle);

        // ===== כותרת =====
        h.tvTitle.setText(task.getTitle());

        // ===== עדיפות =====
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

        // ===== קטגוריה =====
        String category = task.getCategory();
        h.tvCategory.setText(category != null ? category : "");
        h.tvCategory.setTextColor(Color.WHITE);
        h.tvCategory.setPadding(20, 8, 20, 8);

        if ("בית".equals(category)) {
            h.tvCategory.setBackgroundColor(Color.parseColor("#6366F1"));
        } else if ("עבודה".equals(category)) {
            h.tvCategory.setBackgroundColor(Color.parseColor("#F59E0B"));
        } else if ("קניות".equals(category)) {
            h.tvCategory.setBackgroundColor(Color.parseColor("#10B981"));
        } else if ("אישי".equals(category)) {
            h.tvCategory.setBackgroundColor(Color.parseColor("#EC4899"));
        } else {
            h.tvCategory.setBackgroundColor(Color.parseColor("#6B7280"));
        }

        // ===== בוצע / לא בוצע =====
        applyCompletedState(h, task.isCompleted());

        // ===== לחיצה =====
        h.imgCheck.setOnClickListener(v -> {
            boolean newState = !task.isCompleted();
            task.setCompleted(newState);

            applyCompletedState(h, newState);

            if (task.getId() != null) {
                db.collection("home_tasks")
                        .document("defaultList")
                        .collection("items")
                        .document(task.getId())
                        .update("completed", newState);
            }
        });
    }

    // ================= STATE =================

    private void applyCompletedState(TaskViewHolder h, boolean completed) {
        if (completed) {
            h.imgCheck.setBackgroundResource(R.drawable.bg_check_done);
            h.imgCheck.setColorFilter(Color.parseColor("#16A34A"));

            h.tvTitle.setPaintFlags(
                    h.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
            );
            h.tvTitle.setTextColor(Color.parseColor("#6B7280"));

            h.tvPriority.setAlpha(0.4f);
            h.tvCategory.setAlpha(0.4f);
        } else {
            h.imgCheck.setBackgroundResource(R.drawable.bg_check_circle);
            h.imgCheck.setColorFilter(Color.parseColor("#9CA3AF"));

            h.tvPriority.setAlpha(1f);
            h.tvCategory.setAlpha(1f);
        }
    }

    // ================= COUNT =================

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ================= HOLDERS =================

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCheck;
        TextView tvTitle, tvPriority, tvCategory;

        TaskViewHolder(@NonNull View v) {
            super(v);
            imgCheck = v.findViewById(R.id.imgCheck);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvPriority = v.findViewById(R.id.tvPriority);
            tvCategory = v.findViewById(R.id.tvCategory);
        }
    }

    static class DividerViewHolder extends RecyclerView.ViewHolder {
        DividerViewHolder(@NonNull View v) {
            super(v);
        }
    }
}
