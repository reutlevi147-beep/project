package com.example.myapplication;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ShoppingListAdapter
        extends RecyclerView.Adapter<ShoppingListAdapter.ViewHolder> {

    private final List<ShoppingItem> items;
    private boolean isBinding;

    // ✔️ סימון נקנה
    public interface OnItemCheckedChange {
        void onCheckedChanged(ShoppingItem item, boolean checked);
    }

    // 🔢 שינוי כמות
    public interface OnQuantityChangeListener {
        void onQuantityChanged(ShoppingItem item, int newQuantity);
    }

    private OnItemCheckedChange checkedListener;
    private OnQuantityChangeListener quantityListener;

    public void setOnItemCheckedChange(OnItemCheckedChange listener) {
        this.checkedListener = listener;
    }

    public void setOnQuantityChangeListener(OnQuantityChangeListener listener) {
        this.quantityListener = listener;
    }

    public ShoppingListAdapter(List<ShoppingItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.shopping_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShoppingItem item = items.get(position);

        isBinding = true;

        // ===== טקסטים =====
        holder.tvItemName.setText(item.getName());
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
        holder.tvCategoryIcon.setText(getIconForCategory(item.getCategoryId()));

        // ===== סימון נקנה =====
        updateCheckIcon(holder.imgCheck, item.isPurchased());

        if (item.isPurchased()) {
            holder.tvItemName.setPaintFlags(
                    holder.tvItemName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
            );
            holder.tvItemName.setAlpha(0.5f);
        } else {
            holder.tvItemName.setPaintFlags(
                    holder.tvItemName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG)
            );
            holder.tvItemName.setAlpha(1f);
        }

        isBinding = false;

        // ===== ✔️ שינוי מצב נקנה =====
        holder.imgCheck.setOnClickListener(v -> {
            if (isBinding || checkedListener == null) return;

            checkedListener.onCheckedChanged(item, !item.isPurchased());
        });

        // ===== ➕ הגדלת כמות =====
        holder.btnPlus.setOnClickListener(v -> {
            if (quantityListener == null) return;

            int newQty = item.getQuantity() + 1;
            quantityListener.onQuantityChanged(item, newQty);
        });

        // ===== ➖ הקטנת כמות =====
        holder.btnMinus.setOnClickListener(v -> {
            if (quantityListener == null) return;
            if (item.getQuantity() <= 1) return;

            int newQty = item.getQuantity() - 1;
            quantityListener.onQuantityChanged(item, newQty);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ===== helpers =====
    private void updateCheckIcon(ImageView img, boolean purchased) {
        img.setImageResource(
                purchased
                        ? R.drawable.ic_check_circle     // ✔️ ירוק
                        : R.drawable.ic_circle_empty     // ⭕ ריק
        );
    }

    private String getIconForCategory(String categoryId) {
        if (categoryId == null) return "🛒";
        switch (categoryId) {
            case "veg": return "🥬";
            case "dairy": return "🥛";
            case "meat": return "🍖";
            case "dry": return "🌾";
            default: return "🛒";
        }
    }

    // ===== ViewHolder =====
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryIcon, tvItemName, tvQuantity;
        ImageView imgCheck;
        ImageButton btnPlus, btnMinus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryIcon = itemView.findViewById(R.id.tvCategoryIcon);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            imgCheck = itemView.findViewById(R.id.imgCheck);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnMinus = itemView.findViewById(R.id.btnMinus);
        }
    }
}
