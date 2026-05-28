package com.mycasa.app;

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

    // ===== Listeners =====
    public interface OnQuantityChangeListener {
        void onQuantityChanged(ShoppingItem item, int newQuantity);
    }

    public interface OnItemCheckedChangeListener {
        void onItemCheckedChanged(ShoppingItem item, boolean checked);
    }

    private OnQuantityChangeListener quantityListener;
    private OnItemCheckedChangeListener checkedListener;

    public void setOnQuantityChangeListener(OnQuantityChangeListener l) {
        quantityListener = l;
    }

    public void setOnItemCheckedChange(OnItemCheckedChangeListener l) {
        checkedListener = l;
    }

    public ShoppingListAdapter(List<ShoppingItem> items) {
        this.items = items;
    }

    // ===== ViewHolder =====
    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvQuantity, tvCategoryIcon;
        ImageView imgCheck;
        ImageButton btnPlus, btnMinus;

        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvItemName);
            tvQuantity = v.findViewById(R.id.tvQuantity);
            tvCategoryIcon = v.findViewById(R.id.tvCategoryIcon);
            imgCheck = v.findViewById(R.id.imgCheck);
            btnPlus = v.findViewById(R.id.btnPlus);
            btnMinus = v.findViewById(R.id.btnMinus);
        }
    }

    // יצירת ViewHolder עבור פריט קנייה ברשימה
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.shopping_item, parent, false);
        return new ViewHolder(v);
    }

    // הצגת נתוני פריט קנייה וניהול פעולות סימון וכמות
    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {

        ShoppingItem item = items.get(position);

        // ===== טקסטים =====
        h.tvName.setText(item.getName());
        h.tvQuantity.setText(String.valueOf(item.getQuantity()));
        h.tvCategoryIcon.setText(getCategoryIcon(item.getCategoryId()));

        // ===== אייקון סימון =====
        h.imgCheck.setImageResource(
                item.isPurchased()
                        ? R.drawable.ic_check_circle
                        : R.drawable.ic_circle_empty
        );

        // ===== עיצוב נקנה =====
        if (item.isPurchased()) {
            h.tvName.setPaintFlags(
                    h.tvName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
            );
            h.tvName.setAlpha(0.5f);
        } else {
            h.tvName.setPaintFlags(
                    h.tvName.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG
            );
            h.tvName.setAlpha(1f);
        }

        // ===== לחיצה על סימון =====
        h.imgCheck.setOnClickListener(v -> {
            boolean newState = !item.isPurchased();

            // ✅ שינוי מקומי (Optimistic UI)
            item.setIsPurchased(newState);


            if (checkedListener != null) {
                checkedListener.onItemCheckedChanged(item, newState);
            }

            notifyItemChanged(h.getAdapterPosition());
        });


        // ===== כמות + =====
        h.btnPlus.setOnClickListener(v -> {
            int newQty = item.getQuantity() + 1;

            // ✅ שינוי מקומי
            item.setQuantity(newQty);

            if (quantityListener != null) {
                quantityListener.onQuantityChanged(item, newQty);
            }

            notifyItemChanged(h.getAdapterPosition());
        });


        // ===== כמות - =====
        h.btnMinus.setOnClickListener(v -> {
            if (item.getQuantity() <= 1) return;

            int newQty = item.getQuantity() - 1;

            // ✅ שינוי מקומי
            item.setQuantity(newQty);

            if (quantityListener != null) {
                quantityListener.onQuantityChanged(item, newQty);
            }

            notifyItemChanged(h.getAdapterPosition());
        });

    }


    // החזרת כמות פריטי הקנייה ברשימה
    @Override
    public int getItemCount() {
        return items.size();
    }

    // החזרת אייקון מתאים לפי קטגוריית הקנייה
    private String getCategoryIcon(String id) {
        if (id == null) return "🛒";

        switch (id) {
            case "veg": return "🥬";
            case "dairy": return "🥛";
            case "meat": return "🍖";
            case "bakery": return "🥖";
            case "dry": return "🌾";
            case "snacks": return "🍫";
            case "frozen": return "🧊";
            case "drinks": return "🥤";
            case "cleaning": return "🧼";
            case "other": default: return "🛒";
        }
    }

}
