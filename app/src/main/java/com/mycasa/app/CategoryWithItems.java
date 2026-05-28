package com.mycasa.app;

import java.util.List;

public class CategoryWithItems {

    // מחלקת מודל המייצגת קטגוריה המכילה תתי־פריטים וסכום כולל

    public String categoryId;
    public String title;
    public int color;
    public double totalAmount;

    public List<SubCategoryItem> items;
    public boolean expanded = false;

    public CategoryWithItems(
            String categoryId,
            String title,
            int color,
            double totalAmount,
            List<SubCategoryItem> items
    ) {
        this.categoryId = categoryId;
        this.title = title;
        this.color = color;
        this.totalAmount = totalAmount;
        this.items = items;
    }
}
