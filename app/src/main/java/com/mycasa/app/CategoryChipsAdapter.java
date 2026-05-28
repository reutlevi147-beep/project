package com.mycasa.app;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryChipsAdapter
        extends RecyclerView.Adapter<CategoryChipsAdapter.ViewHolder> {

    public interface OnCategoryClickListener {
        void onCategoryClick(String categoryId);
    }

    private final List<String> categoryIds;
    private final OnCategoryClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    // 🔹 מיפוי ID → עברית
    private static final Map<String, String> CATEGORY_TITLES = new HashMap<>();
    static {
        CATEGORY_TITLES.put("income_salary", "משכורת");
        CATEGORY_TITLES.put("income_bonus", "בונוס");
        CATEGORY_TITLES.put("income_other", "הכנסה אחרת");

        CATEGORY_TITLES.put("expense_food", "אוכל");
        CATEGORY_TITLES.put("expense_housing", "דיור");
        CATEGORY_TITLES.put("expense_transport", "תחבורה");
        CATEGORY_TITLES.put("expense_health", "בריאות");
        CATEGORY_TITLES.put("expense_leisure", "פנאי");
        CATEGORY_TITLES.put("expense_other", "שונות");
    }

    public CategoryChipsAdapter(
            List<String> categoryIds,
            OnCategoryClickListener listener
    ) {
        this.categoryIds = categoryIds;
        this.listener = listener;
    }

    // יצירת ViewHolder עבור קטגוריה ברשימה
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_chip, parent, false);
        return new ViewHolder(view);
    }

    // הצגת נתוני הקטגוריה ועדכון מצב הבחירה שלה
    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        String categoryId = categoryIds.get(position);
        String title = CATEGORY_TITLES.get(categoryId);

        holder.tvCategory.setText(
                title != null ? title : categoryId
        );

        boolean selected = position == selectedPosition;

        // 🎨 עיצוב בחירה
        holder.itemView.setBackgroundResource(
                selected
                        ? R.drawable.bg_chip_selected
                        : R.drawable.bg_chip_normal
        );

        holder.tvCategory.setTextColor(
                selected ? Color.WHITE : Color.parseColor("#374151")
        );

        holder.itemView.setOnClickListener(v -> {
            int oldPos = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            notifyItemChanged(oldPos);
            notifyItemChanged(selectedPosition);

            if (listener != null) {
                listener.onCategoryClick(categoryId); // 🔑 ID אמיתי
            }
        });
    }

    // החזרת כמות הקטגוריות ברשימה
    @Override
    public int getItemCount() {
        return categoryIds.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvCategory;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvCategory = itemView.findViewById(R.id.tvCategory);

            // 🛡️ גיבוי – אם מישהו שכח id ב-XML
            if (tvCategory == null && itemView instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) itemView;
                if (vg.getChildCount() > 0 && vg.getChildAt(0) instanceof TextView) {
                    tvCategory = (TextView) vg.getChildAt(0);
                }
            }
        }
    }

}
