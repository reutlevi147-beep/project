package com.mycasa.app;

import java.util.Date;

public class SavingsGoal {

    private String title;
    private int targetAmount;
    private int currentAmount;
    private String goalMode; // SAVE | LIMIT
    private Date deadline;

    // 🔹 חובה ל-Firebase
    public SavingsGoal() {
    }

    // 🔹 הקונסטרקטור שאת משתמשת בו
    public SavingsGoal(
            String title,
            int targetAmount,
            int currentAmount,
            String goalMode,
            Date deadline
    ) {
        this.title = title;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.goalMode = goalMode;
        this.deadline = deadline;
    }

    // ===== Getters =====
    public String getTitle() {
        return title;
    }

    public int getTargetAmount() {
        return targetAmount;
    }

    public int getCurrentAmount() {
        return currentAmount;
    }

    public String getGoalMode() {
        return goalMode;
    }

    public Date getDeadline() {
        return deadline;
    }

    // ===== Status =====
    public enum GoalStatus {
        ACTIVE,
        SUCCESS,
        FAILED
    }

    public GoalStatus getStatus() {

        Date now = new Date();
        boolean deadlinePassed =
                deadline != null && now.after(deadline);

        // =========================
        // SAVE – חיסכון / הכנסה
        // =========================
        if ("SAVE".equals(goalMode)) {

            if (currentAmount >= targetAmount) {
                return GoalStatus.SUCCESS;
            }

            if (deadlinePassed) {
                return GoalStatus.FAILED;
            }

            return GoalStatus.ACTIVE;
        }

        // =========================
        // LIMIT – הוצאה / תקציב
        // =========================
        if ("LIMIT".equals(goalMode)) {

            if (deadlinePassed) {
                // רק בסוף הזמן קובעים הצלחה / כישלון
                if (currentAmount <= targetAmount) {
                    return GoalStatus.SUCCESS;
                } else {
                    return GoalStatus.FAILED;
                }
            }

            // ⚠️ גם אם עבר יעד – עדיין ACTIVE
            return GoalStatus.ACTIVE;
        }

        return GoalStatus.ACTIVE;
    }

    // ===== עזר =====
    public boolean isOverTarget() {
        return currentAmount > targetAmount;
    }
}
