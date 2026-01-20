package com.mycasa.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoryChipsAdapter
        extends RecyclerView.Adapter<CategoryChipsAdapter.ViewHolder> {

    public interface OnCategoryClickListener {
        void onCategoryClick(String category);
    }

    private final List<String> categories;
    private final OnCategoryClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public CategoryChipsAdapter(List<String> categories,
                                OnCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

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

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        String category = categories.get(position);
        holder.tvCategory.setText(category);

        // מצב בחירה
        holder.itemView.setSelected(position == selectedPosition);

        holder.itemView.setOnClickListener(v -> {
            int oldPos = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            notifyItemChanged(oldPos);
            notifyItemChanged(selectedPosition);

            if (listener != null) {
                listener.onCategoryClick(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvCategory;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(android.R.id.text1);

            // אם אין id – נופלים על TextView הראשון
            if (tvCategory == null && itemView instanceof ViewGroup) {
                tvCategory = (TextView) ((ViewGroup) itemView).getChildAt(0);
            }
        }
    }
}
