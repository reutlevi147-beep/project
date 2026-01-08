package com.example.myapplication;

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

    // ❗ אין בחירה בהתחלה
    private int selectedPosition = RecyclerView.NO_POSITION;

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

        boolean selected = position == selectedPosition;

        holder.itemView.setBackgroundResource(
                selected
                        ? R.drawable.bg_category_card_selected
                        : R.drawable.bg_category_card
        );

        // צבע טקסט כהה – קריא על שני הרקעים
        holder.tvName.setTextColor(
                android.graphics.Color.parseColor("#111827")
        );


        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            int oldPos = selectedPosition;
            selectedPosition = pos;

            if (oldPos != RecyclerView.NO_POSITION) {
                notifyItemChanged(oldPos);
            }
            notifyItemChanged(pos);

            if (listener != null) {
                listener.onSelected(categories.get(pos));
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
}
