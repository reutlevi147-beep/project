package com.mycasa.app;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SavingsGoalAdapter
        extends RecyclerView.Adapter<SavingsGoalAdapter.GoalViewHolder> {

    public interface OnGoalActionListener {
        void onAddAmountClicked(SavingsGoal goal);
        void onDeleteClicked(SavingsGoal goal);
    }

    private OnGoalActionListener listener;
    private final List<SavingsGoal> goals;

    public SavingsGoalAdapter(List<SavingsGoal> goals) {
        this.goals = goals;
    }

    // הגדרת מאזין לפעולות על יעד חיסכון
    public void setOnGoalActionListener(OnGoalActionListener listener) {
        this.listener = listener;
    }

    // יצירת ViewHolder עבור יעד חיסכון ברשימה
    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_savings_goal, parent, false);
        return new GoalViewHolder(view);
    }

    // הצגת נתוני יעד החיסכון ועדכון מצב ההתקדמות שלו
    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {

        SavingsGoal goal = goals.get(position);

        holder.tvTitle.setText(goal.getTitle());
        holder.tvCurrent.setText("₪" + goal.getCurrentAmount());
        holder.tvTarget.setText("₪" + goal.getTargetAmount());

        // ===== Progress =====
        holder.progressGoal.setMax(100);

        int progress = (int) (goal.getMoneyProgress() * 100);
        holder.progressGoal.setProgress(progress);

        int color = getProgressColor(goal);

        holder.progressGoal.setProgressTintList(
                android.content.res.ColorStateList.valueOf(color)
        );

        holder.tvStatus.setText(getStatusText(goal));

        holder.btnAddAmount.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddAmountClicked(goal);
            }
        });

        holder.btnDeleteGoal.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClicked(goal);
            }
        });
    }


    // החזרת כמות יעדי החיסכון ברשימה
    @Override
    public int getItemCount() {
        return goals.size();
    }

    static class GoalViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvCurrent, tvTarget, tvStatus;
        ProgressBar progressGoal;
        ImageButton btnAddAmount, btnDeleteGoal;

        GoalViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCurrent = itemView.findViewById(R.id.tvCurrent);
            tvTarget = itemView.findViewById(R.id.tvTarget);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            progressGoal = itemView.findViewById(R.id.progressGoal);
            btnAddAmount = itemView.findViewById(R.id.btnAddAmount);
            btnDeleteGoal = itemView.findViewById(R.id.btnDeleteGoal);
        }
    }

    // החזרת טקסט מצב היעד לפי סטטוס ההתקדמות
    private String getStatusText(SavingsGoal goal) {
        switch (goal.getStatus()) {
            case SUCCESS: return "הושג ✔";
            case FAILED:  return "נכשל ✖";
            case ACTIVE:
            default: return "בתהליך";
        }
    }

    // קביעת צבע ההתקדמות לפי קצב החיסכון והדדליין
    private int getProgressColor(SavingsGoal goal) {

        double moneyProgress = goal.getMoneyProgress(); // 0.0 → 1.0

        // ===== אם אין דדליין – רק לפי אחוז כסף =====
        if (goal.getDeadline() == null) {

            if (moneyProgress >= 1.0) {
                return Color.parseColor("#15803D"); // ירוק כהה – הושג
            }

            if (moneyProgress >= 0.8) {
                return Color.parseColor("#16A34A"); // ירוק
            }

            if (moneyProgress >= 0.5) {
                return Color.parseColor("#F59E0B"); // כתום
            }

            return Color.parseColor("#DC2626"); // אדום
        }

        // ===== אם יש דדליין – לפי זמן מול כסף =====

        double timeProgress = goal.getTimeProgress();

        if (moneyProgress >= timeProgress) {
            return Color.parseColor("#16A34A"); // ירוק
        }

        if (moneyProgress >= timeProgress * 0.7) {
            return Color.parseColor("#F59E0B"); // כתום
        }

        return Color.parseColor("#DC2626"); // אדום
    }



}
