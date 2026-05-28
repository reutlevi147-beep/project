package com.mycasa.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
    public interface OnDeleteCompletedListener {
        void onDeleteCompleted();
    }
    public interface OnTaskEditListener {
        void onTaskEdit(Task task);
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

    private OnDeleteCompletedListener deleteCompletedListener;

    public void setOnDeleteCompletedListener(OnDeleteCompletedListener l) {
        deleteCompletedListener = l;
    }

    public TasksAdapter(Context context, List<Task> tasks) {
        this.context = context;
        this.tasks = tasks;
    }

    @Override
    public int getItemViewType(int position) {
        return tasks.get(position) == null ? TYPE_DIVIDER : TYPE_TASK;
    }

    // יצירת ViewHolder מתאים לפי סוג השורה
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

    // הצגת נתוני משימה וניהול פעולות המשתמש
    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder,
            int position) {

        if (getItemViewType(position) == TYPE_DIVIDER) {

            DividerHolder h = (DividerHolder) holder;

            h.btnDeleteCompleted.setOnClickListener(v -> {
                if (deleteCompletedListener != null) {
                    deleteCompletedListener.onDeleteCompleted();
                }
            });

            return;
        }

        TaskViewHolder h = (TaskViewHolder) holder;
        Task task = tasks.get(position);

        h.tvTitle.setText(task.getTitle());

        setPriorityBadge(h, task);
        setCheckIcon(h, task.isCompleted());

        if (task.getDueDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            h.tvDate.setText(sdf.format(new Date(task.getDueDate())));
        } else {
            h.tvDate.setText("");
        }

        if (task.isCompleted()) {
            h.cardTask.setCardBackgroundColor(Color.parseColor("#F3F4F6"));
            h.tvTitle.setTextColor(Color.parseColor("#9CA3AF"));
            h.tvCategory.setTextColor(Color.parseColor("#6B7280"));

            h.tvTitle.setPaintFlags(
                    h.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
            );
        } else {
            h.cardTask.setCardBackgroundColor(Color.WHITE);
            h.tvTitle.setTextColor(Color.parseColor("#111827"));
            h.tvCategory.setTextColor(Color.parseColor("#6B7280"));

            h.tvTitle.setPaintFlags(
                    h.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG)
            );
        }

        if (allowEdit) {
            h.itemView.setOnClickListener(v -> {
                Intent i = new Intent(context, Add_Tasks.class);
                i.putExtra("taskId", task.getId());
                context.startActivity(i);
            });
        } else {
            h.itemView.setOnClickListener(null);
        }

        if (h.btnDelete != null) {
            h.btnDelete.setVisibility(allowDelete ? View.VISIBLE : View.GONE);

            h.btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onTaskDelete(task);
                }
            });
        }

        // Toggle - סימון משימה כבוצעה בלחיצה על העיגול
        if (allowToggle) {

            h.imgCheck.setEnabled(true);
            h.imgCheck.setClickable(true);
            h.imgCheck.setAlpha(task.isCompleted() ? 1f : 0.6f);

            h.imgCheck.setOnClickListener(v -> {

                boolean newState = !task.isCompleted();

                task.setCompleted(newState);
                setCheckIcon(h, newState);

                if (checkedListener != null) {
                    checkedListener.onTaskChecked(task, newState);
                }
            });

        } else {

            h.imgCheck.setEnabled(false);
            h.imgCheck.setClickable(false);
            h.imgCheck.setAlpha(0.4f);
            h.imgCheck.setOnClickListener(null);
        }
    }

    // עדכון תג עדיפות המשימה לפי רמת החשיבות
    private void setPriorityBadge(TaskViewHolder h, Task task) {
        String priority = task.getPriority();

        if ("high".equals(priority)) {
            h.tvPriority.setText("⚑ גבוהה");
            h.tvPriority.setTextColor(Color.parseColor("#EF4444"));
            h.tvPriority.setBackgroundResource(R.drawable.bg_priority_high);

        } else if ("medium".equals(priority)) {
            h.tvPriority.setText("⚑ בינונית");
            h.tvPriority.setTextColor(Color.parseColor("#F59E0B"));
            h.tvPriority.setBackgroundResource(R.drawable.bg_priority_medium);

        } else if ("low".equals(priority)) {
            h.tvPriority.setText("⚑ נמוכה");
            h.tvPriority.setTextColor(Color.parseColor("#22C55E"));
            h.tvPriority.setBackgroundResource(R.drawable.bg_priority_low);

        } else {
            h.tvPriority.setText("⚑ גבוהה");
            h.tvPriority.setTextColor(Color.parseColor("#EF4444"));
            h.tvPriority.setBackgroundResource(R.drawable.bg_priority_high);
        }

        h.tvCategory.setText(task.getCategory());
    }

    // עדכון אייקון הסימון לפי מצב המשימה
    private void setCheckIcon(TaskViewHolder h, boolean completed) {
        if (completed) {
            // משימה שבוצעה = וי ירוק
            h.imgCheck.setImageResource(R.drawable.ic_check_circle);
            h.imgCheck.setAlpha(1f);
        } else {
            // משימה פעילה = עיגול ריק
            h.imgCheck.setImageResource(R.drawable.ic_circle_empty);
            h.imgCheck.setAlpha(0.6f);
        }

        h.imgCheck.clearColorFilter();
        h.imgCheck.setBackground(null);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {

        CardView cardTask;
        ImageView imgCheck;
        ImageButton btnDelete;
        TextView tvTitle, tvCategory, tvDate, tvPriority;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);

            cardTask = itemView.findViewById(R.id.cardTask);
            imgCheck = itemView.findViewById(R.id.imgCheck);
            btnDelete = itemView.findViewById(R.id.btnDelete);

            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvPriority = itemView.findViewById(R.id.tvPriority);
        }
    }

    static class DividerHolder extends RecyclerView.ViewHolder {

        Button btnDeleteCompleted;

        DividerHolder(@NonNull View itemView) {
            super(itemView);
            btnDeleteCompleted = itemView.findViewById(R.id.btnDeleteCompleted);
        }
    }
}