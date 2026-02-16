package com.mycasa.app;

public class GoalAlert {

    public enum AlertType {
        URGENT,
        WARNING,
        SUGGESTION,
        SUCCESS
    }

    private SavingsGoal goal;   // 🔥 חשוב
    private String title;
    private String message;
    private String actionText;
    private AlertType type;

    public GoalAlert(SavingsGoal goal,
                     String title,
                     String message,
                     String actionText,
                     AlertType type) {

        this.goal = goal;
        this.title = title;
        this.message = message;
        this.actionText = actionText;
        this.type = type;
    }

    public SavingsGoal getGoal() { return goal; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getActionText() { return actionText; }
    public AlertType getType() { return type; }
}
