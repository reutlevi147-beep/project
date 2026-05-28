package com.mycasa.app;

import java.util.Date;
import java.util.Calendar;

public class SavingsGoal {

    // ================================
    // Fields
    // ================================

    private String title;
    private int targetAmount;
    private int currentAmount;
    private Date lastPeriodDecision;
    private Date createdAt;

    private GoalMode goalMode;
    private GoalType goalType;
    private PeriodType periodType;
    private String goalId;
    private String linkedCategoryId;
    private Date deadline;
    private boolean successHandled;
    private Date lastProgressAlertDate;

    // ================================
    // Enums
    // ================================

    public enum GoalMode {
        SAVE,   // חיסכון
        LIMIT   // הגבלת הוצאה
    }

    public enum GoalType {
        TARGET,   // יעד עם דדליין (טיול, רכב וכו')
        PERIOD,   // יעד חוזר (חודשי / שנתי)
        EVENT     // יעד לאירוע ספציפי
    }

    public enum PeriodType {
        NONE,
        MONTHLY,
        YEARLY
    }

    public enum GoalStatus {
        ACTIVE,
        SUCCESS,
        FAILED
    }

    public enum SmartStatus {
        ON_TRACK,
        WARNING,
        DANGER
    }

    // ================================
    // Firebase empty constructor
    // ================================

    public SavingsGoal() {}

    // ================================
    // Main constructor
    // ================================

    public SavingsGoal(
            String goalId,
            String title,
            int targetAmount,
            int currentAmount,
            GoalMode goalMode,
            GoalType goalType,
            PeriodType periodType,
            String linkedCategoryId,
            Date deadline
    ) {
        this.goalId = goalId;
        this.title = title;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.goalMode = goalMode;
        this.goalType = goalType;
        this.periodType = periodType;
        this.linkedCategoryId = linkedCategoryId;
        this.deadline = deadline;
    }


    public Date getCreatedAt() {
        return createdAt;
    }


    // ================================
    // Getters
    // ================================
    public String getGoalId() {
        return goalId;
    }
    public boolean isSuccessHandled() {
        return successHandled;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getLastProgressAlertDate() {
        return lastProgressAlertDate;
    }

    public void setLastProgressAlertDate(Date lastProgressAlertDate) {
        this.lastProgressAlertDate = lastProgressAlertDate;
    }


    public void setSuccessHandled(boolean successHandled) {
        this.successHandled = successHandled;
    }

    public String getTitle() { return title; }

    public int getTargetAmount() { return targetAmount; }

    public int getCurrentAmount() { return currentAmount; }

    public GoalMode getGoalMode() { return goalMode; }

    public GoalType getGoalType() { return goalType; }

    public PeriodType getPeriodType() { return periodType; }

    public String getLinkedCategoryId() { return linkedCategoryId; }

    public Date getDeadline() { return deadline; }

    // חישוב מצב יעד החיסכון לפי סכום, סוג ודדליין
    public GoalStatus getStatus() {

        Date now = new Date();
        boolean deadlinePassed =
                deadline != null && now.after(deadline);

        // SAVE
        if (goalMode == GoalMode.SAVE) {

            if (currentAmount >= targetAmount) {
                return GoalStatus.SUCCESS;
            }

            if (deadlinePassed) {
                return GoalStatus.FAILED;
            }

            return GoalStatus.ACTIVE;
        }

        // LIMIT
        if (goalMode == GoalMode.LIMIT) {

            if (deadlinePassed) {

                if (currentAmount <= targetAmount) {
                    return GoalStatus.SUCCESS;
                } else {
                    return GoalStatus.FAILED;
                }
            }

            return GoalStatus.ACTIVE;
        }

        return GoalStatus.ACTIVE;
    }

    public Date getLastPeriodDecision() {
        return lastPeriodDecision;
    }

    public void setLastPeriodDecision(Date lastPeriodDecision) {
        this.lastPeriodDecision = lastPeriodDecision;
    }

    // חישוב הסכום שנותר להשלמת היעד
    public int getRemainingAmount() {
        return Math.max(targetAmount - currentAmount, 0);
    }

    // חישוב מספר החודשים שנותרו עד הדדליין
    public int getMonthsLeft() {

        if (deadline == null) return -1;

        Calendar now = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        end.setTime(deadline);

        int months =
                (end.get(Calendar.YEAR) - now.get(Calendar.YEAR)) * 12 +
                        (end.get(Calendar.MONTH) - now.get(Calendar.MONTH));

        return Math.max(months, 0);
    }

    // חישוב הסכום החודשי הנדרש לעמידה ביעד
    public double getRequiredPerMonth() {

        int monthsLeft = getMonthsLeft();
        if (monthsLeft <= 0) return 0;

        return (double) getRemainingAmount() / monthsLeft;
    }


    private SmartStatus smartStatus = SmartStatus.ON_TRACK;

    // הערכת מצב ההתקדמות של היעד לפי קצב החיסכון בפועל
    public SmartStatus getSmartStatus(double actualMonthlyAverage) {

        double required = getRequiredPerMonth();

        if (required == 0) return SmartStatus.ON_TRACK;

        if (actualMonthlyAverage >= required) {
            return SmartStatus.ON_TRACK;
        } else if (actualMonthlyAverage >= required * 0.8) {
            return SmartStatus.WARNING;
        } else {
            return SmartStatus.DANGER;
        }
    }

    public void setSmartEvaluation(double actualMonthlyAverage) {
        smartStatus = getSmartStatus(actualMonthlyAverage);
    }

    public SmartStatus getSmartStatusValue() {
        return smartStatus;
    }

    // חישוב אחוז ההתקדמות הכספית של היעד
    public double getMoneyProgress() {

        if (targetAmount <= 0) return 0;

        return Math.min(1.0, (double) currentAmount / targetAmount);
    }

    public int getAmountToBeOnTrack() {

        double timeProgress = getTimeProgress();
        int expectedAmount = (int) (targetAmount * timeProgress);

        int diff = expectedAmount - currentAmount;

        return Math.max(diff, 0);
    }

    // חישוב אחוז הזמן שעבר מתוך תקופת היעד
    public double getTimeProgress() {

        if (createdAt == null || deadline == null) return 0;

        long total = deadline.getTime() - createdAt.getTime();
        long passed = new Date().getTime() - createdAt.getTime();

        if (total <= 0) return 1;

        return Math.min(1.0, Math.max(0, (double) passed / total));
    }


    public boolean isOverTarget() {
        return currentAmount > targetAmount;
    }
}
