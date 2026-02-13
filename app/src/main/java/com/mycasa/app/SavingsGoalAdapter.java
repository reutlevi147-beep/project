package com.mycasa.app;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SavingsGoalAdapter
        extends RecyclerView.Adapter<SavingsGoalAdapter.VH> {

    // =========================
    // Listener – פעולות על מטרה
    // =========================
    public interface OnGoalActionListener {
        void onAddAmount(SavingsGoal goal);
        void onRestartGoal(SavingsGoal goal);
        void onDeleteGoal(SavingsGoal goal);
    }

    private OnGoalActionListener listener;
    private final List<SavingsGoal> goals;

    public SavingsGoalAdapter(List<SavingsGoal> goals) {
        this.goals = goals;
    }

    public void setOnGoalActionListener(OnGoalActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_savings_goal, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {

        SavingsGoal goal = goals.get(position);

        // ===== טקסטים =====
        h.tvTitle.setText(goal.getTitle());
        h.tvCurrent.setText("₪" + goal.getCurrentAmount());
        h.tvTarget.setText("₪" + goal.getTargetAmount());

        // ===== Progress =====
        int progress = 0;
        if (goal.getTargetAmount() > 0) {
            progress = (int) (
                    (goal.getCurrentAmount() * 100f)
                            / goal.getTargetAmount()
            );
        }
        h.progressBar.setProgress(Math.min(progress, 100));

        // ===== צבע פס =====
        if ("SAVE".equals(goal.getGoalMode())) {
            h.progressBar.setProgressTintList(
                    ColorStateList.valueOf(
                            h.itemView.getContext()
                                    .getColor(R.color.green_income)
                    )
            );
        } else {
            h.progressBar.setProgressTintList(
                    ColorStateList.valueOf(
                            goal.isOverTarget()
                                    ? h.itemView.getContext()
                                    .getColor(R.color.red_expense)
                                    : h.itemView.getContext()
                                    .getColor(android.R.color.darker_gray)
                    )
            );
        }

        // ===== סטטוס =====
        SavingsGoal.GoalStatus status = goal.getStatus();

        if (status == SavingsGoal.GoalStatus.ACTIVE) {

            // 🔓 פעיל
            h.overlayLocked.setVisibility(View.GONE);
            h.btnAddAmount.setEnabled(true);
            h.itemView.setAlpha(1f);

            if ("SAVE".equals(goal.getGoalMode())) {
                int remaining = goal.getTargetAmount() - goal.getCurrentAmount();
                h.tvStatus.setText("נותר ₪" + Math.max(remaining, 0));
            } else {
                h.tvStatus.setText(
                        goal.isOverTarget()
                                ? "חריגה ₪" + (goal.getCurrentAmount() - goal.getTargetAmount())
                                : "בתוך התקציב"
                );
            }

            h.btnAddAmount.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddAmount(goal);
                }
            });

        } else {

            // 🔒 נעול
            h.overlayLocked.setVisibility(View.VISIBLE);
            h.btnAddAmount.setEnabled(false);
            h.itemView.setAlpha(0.6f);

            if (status == SavingsGoal.GoalStatus.SUCCESS) {
                h.tvLockMessage.setText(
                        "SAVE".equals(goal.getGoalMode())
                                ? "🎉 היעד הושג"
                                : "👏 עמדת בתקציב"
                );
            } else {
                h.tvLockMessage.setText(
                        "SAVE".equals(goal.getGoalMode())
                                ? "⏰ הזמן נגמר לפני היעד"
                                : "⚠️ חרגת מהתקציב"
                );
            }

            h.btnRestart.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRestartGoal(goal);
                }
            });

            h.btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteGoal(goal);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return goals.size();
    }

    // =========================
    // ViewHolder
    // =========================
    static class VH extends RecyclerView.ViewHolder {

        TextView tvTitle, tvCurrent, tvTarget, tvStatus, tvLockMessage;
        ProgressBar progressBar;
        ImageButton btnAddAmount;
        ImageButton btnRestart, btnDelete;
        FrameLayout overlayLocked;

        VH(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvCurrent = v.findViewById(R.id.tvCurrent);
            tvTarget = v.findViewById(R.id.tvTarget);
            tvStatus = v.findViewById(R.id.tvStatus);
            tvLockMessage = v.findViewById(R.id.tvLockMessage);
            progressBar = v.findViewById(R.id.progressGoal);
            btnAddAmount = v.findViewById(R.id.btnAddAmount);
            overlayLocked = v.findViewById(R.id.overlayLocked);

            btnRestart = v.findViewById(R.id.btnRestartGoal);
            btnDelete = v.findViewById(R.id.btnDeleteGoal);
        }
    }
}
