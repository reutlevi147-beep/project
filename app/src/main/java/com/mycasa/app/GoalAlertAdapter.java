package com.mycasa.app;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GoalAlertAdapter extends RecyclerView.Adapter<GoalAlertAdapter.VH> {

    public interface OnAlertActionListener {
        void onAction(GoalAlert alert);
    }

    private OnAlertActionListener listener;
    private final List<GoalAlert> items;

    public GoalAlertAdapter(List<GoalAlert> items) {
        this.items = items;
    }

    public void setOnAlertActionListener(OnAlertActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_goal_alert, parent, false);

        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {

        GoalAlert alert = items.get(position);
        SavingsGoal goal = alert.getGoal();

        holder.tvTitle.setText(alert.getTitle());
        holder.tvSubtitle.setText(alert.getMessage());
        holder.btnAction.setText(alert.getActionText());

        if (listener != null) {
            holder.btnAction.setOnClickListener(v ->
                    listener.onAction(alert)
            );
        }

        // =====================================
        // 🎯 SMART COLOR SYSTEM
        // =====================================

        if (goal != null && goal.getDeadline() != null) {

            double timeProgress  = goal.getTimeProgress();
            double moneyProgress = goal.getMoneyProgress();

            int startColor;
            int endColor;
            int iconColor;
            int buttonColor;

            // 🟢 מעל הקצב
            if (moneyProgress >= timeProgress) {

                startColor  = Color.parseColor("#D1FAE5");
                endColor    = Color.parseColor("#10B981");
                iconColor   = Color.parseColor("#047857");
                buttonColor = Color.parseColor("#059669");

            }
            // 🟠 מעט מאחור
            else if (moneyProgress >= timeProgress * 0.7) {

                startColor  = Color.parseColor("#FEF3C7");
                endColor    = Color.parseColor("#F59E0B");
                iconColor   = Color.parseColor("#B45309");
                buttonColor = Color.parseColor("#F59E0B");

            }
            // 🔴 בסיכון
            else {

                startColor  = Color.parseColor("#FEE2E2");
                endColor    = Color.parseColor("#EF4444");
                iconColor   = Color.parseColor("#B91C1C");
                buttonColor = Color.parseColor("#DC2626");
            }

            // ===== Gradient Background =====
            GradientDrawable gradient = new GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    new int[]{startColor, endColor}
            );
            gradient.setCornerRadius(36f);
            holder.cardAlert.setBackground(gradient);

            // ===== Icon Color =====
            holder.imgIcon.setColorFilter(iconColor);

            // ===== Button Color =====
            holder.btnAction.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(buttonColor)
            );
        }
        else {
            // ברירת מחדל בלי דדליין
            holder.cardAlert.setCardBackgroundColor(
                    Color.parseColor("#EEF2FF")
            );
        }
    }


    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    // ================= ViewHolder =================

    static class VH extends RecyclerView.ViewHolder {

        TextView tvTitle;
        TextView tvSubtitle;
        Button btnAction;
        ImageView imgIcon;
        CardView cardAlert;

        VH(@NonNull View itemView) {
            super(itemView);

            cardAlert = itemView.findViewById(R.id.cardAlert);
            tvTitle = itemView.findViewById(R.id.tvAlertTitle);
            tvSubtitle = itemView.findViewById(R.id.tvAlertSubtitle);
            btnAction = itemView.findViewById(R.id.btnAlertAction);
            imgIcon = itemView.findViewById(R.id.imgAlertIcon);
        }
    }

}
