package com.mycasa.app;

import java.util.Date;
import java.util.Map;

public class FinanceSnapshot {

    private String month; // למשל: 2026-01
    private Date createdAt;

    private double totalIncome;
    private double totalExpense;
    private double balance;

    // סיכום לפי קטגוריות (expense_housing → 4280)
    private Map<String, Double> expenseByCategory;

    public FinanceSnapshot() {
        // חובה ל-Firebase
    }

    public FinanceSnapshot(
            String month,
            double totalIncome,
            double totalExpense,
            Map<String, Double> expenseByCategory
    ) {
        this.month = month;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.balance = totalIncome - totalExpense;
        this.expenseByCategory = expenseByCategory;
        this.createdAt = new Date();
    }

    public String getMonth() {
        return month;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public double getTotalIncome() {
        return totalIncome;
    }

    public double getTotalExpense() {
        return totalExpense;
    }

    public double getBalance() {
        return balance;
    }

    public Map<String, Double> getExpenseByCategory() {
        return expenseByCategory;
    }
}
