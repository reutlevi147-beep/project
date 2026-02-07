package com.mycasa.app;

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

    public interface OnGoalActionListener {
        void onAddAmount(SavingsGoal goal);
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

        // לא חוסמים מעל 100 – רק ויזואלית
        h.progressBar.setProgress(Math.min(progress, 100));

        // ===== צבע פס לפי סוג =====
        if ("SAVE".equals(goal.getGoalMode())) {

            // חיסכון – תמיד ירוק
            h.progressBar.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(
                            h.itemView.getContext()
                                    .getColor(R.color.green_income)
                    )
            );

        } else { // LIMIT – הוצאה

            if (goal.isOverTarget()) {
                // חריגה – אדום
                h.progressBar.setProgressTintList(
                        android.content.res.ColorStateList.valueOf(
                                h.itemView.getContext()
                                        .getColor(R.color.red_expense)
                        )
                );
            } else {
                // בתוך היעד – אפור
                h.progressBar.setProgressTintList(
                        android.content.res.ColorStateList.valueOf(
                                h.itemView.getContext()
                                        .getColor(android.R.color.darker_gray)
                        )
                );
            }
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
                if (goal.isOverTarget()) {
                    h.tvStatus.setText("חריגה ₪" +
                            (goal.getCurrentAmount() - goal.getTargetAmount()));
                } else {
                    h.tvStatus.setText("בתוך התקציב");
                }
            }

            h.tvStatus.setTextColor(
                    h.itemView.getContext()
                            .getColor(android.R.color.darker_gray)
            );

            h.btnAddAmount.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddAmount(goal);
                }
            });

        } else {

            // 🔒 נעול – רק כשהתאריך עבר
            h.overlayLocked.setVisibility(View.VISIBLE);
            h.btnAddAmount.setEnabled(false);
            h.itemView.setAlpha(0.6f);

            if (status == SavingsGoal.GoalStatus.SUCCESS) {

                if ("SAVE".equals(goal.getGoalMode())) {
                    h.tvLockMessage.setText("🎉 היעד הושג");
                } else {
                    h.tvLockMessage.setText("👏 עמדת בתקציב");
                }

                h.tvStatus.setTextColor(
                        h.itemView.getContext()
                                .getColor(R.color.green_income)
                );

            } else { // FAILED

                if ("SAVE".equals(goal.getGoalMode())) {
                    h.tvLockMessage.setText("⏰ הזמן נגמר לפני היעד");
                } else {
                    h.tvLockMessage.setText("⚠️ חרגת מהתקציב");
                }

                h.tvStatus.setTextColor(
                        h.itemView.getContext()
                                .getColor(R.color.red_expense)
                );
            }
        }
    }


    @Override
    public int getItemCount() {
        return goals.size();
    }

    public interface OnGoalManageListener {
        void onRestart(SavingsGoal goal);
        void onDelete(SavingsGoal goal);
    }

    private OnGoalManageListener manageListener;

    public void setOnGoalManageListener(OnGoalManageListener l) {
        this.manageListener = l;
    }

    // =========================
    static class VH extends RecyclerView.ViewHolder {

        TextView tvTitle, tvCurrent, tvTarget, tvStatus;
        ProgressBar progressBar;
        ImageButton btnAddAmount;
        FrameLayout overlayLocked;


        VH(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvCurrent = v.findViewById(R.id.tvCurrent);
            tvTarget = v.findViewById(R.id.tvTarget);
            tvStatus = v.findViewById(R.id.tvStatus);
            progressBar = v.findViewById(R.id.progressGoal);
            btnAddAmount = v.findViewById(R.id.btnAddAmount);
            overlayLocked = v.findViewById(R.id.overlayLocked);
        }
    }
}
