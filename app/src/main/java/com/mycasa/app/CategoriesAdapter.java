package com.mycasa.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CategoriesAdapter
        extends RecyclerView.Adapter<CategoriesAdapter.ViewHolder> {

    private final ArrayList<ShoppingCategory> categories;

    // ✅ קטגוריה נבחרת לפי ID
    private String selectedCategoryId = null;

    public interface OnCategorySelected {
        void onSelected(ShoppingCategory category);
    }

    private final OnCategorySelected listener;

    public CategoriesAdapter(ArrayList<ShoppingCategory> categories,
                             OnCategorySelected listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShoppingCategory cat = categories.get(position);

        holder.tvIcon.setText(cat.getIcon());
        holder.tvName.setText(cat.getName());

        boolean isSelected = cat.getId().equals(selectedCategoryId);

        // 🎨 רקע לפי בחירה
        holder.itemView.setBackgroundResource(
                isSelected
                        ? R.drawable.bg_category_card_selected
                        : R.drawable.bg_category_card
        );

        // קצת הדגשה
        holder.itemView.setAlpha(isSelected ? 1f : 0.6f);

        holder.tvName.setTextColor(
                android.graphics.Color.parseColor("#111827")
        );

        holder.itemView.setOnClickListener(v -> {
            selectedCategoryId = cat.getId();
            notifyDataSetChanged();

            if (listener != null) {
                listener.onSelected(cat);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcon, tvName;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.tvIcon);
            tvName = itemView.findViewById(R.id.tvName);
        }
    }

    // ✅ מאפשר סימון אוטומטי (מה־✔️ / AI)
    public void setSelectedCategory(String categoryId) {
        this.selectedCategoryId = categoryId;
        notifyDataSetChanged();
    }
}
