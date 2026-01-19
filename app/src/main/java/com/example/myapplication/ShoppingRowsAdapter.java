package com.example.myapplication;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ShoppingRowsAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ShoppingListRow> rows;

    private ShoppingListAdapter.OnQuantityChangeListener quantityListener;
    private ShoppingListAdapter.OnItemCheckedChangeListener checkedListener;
    private Runnable clearPurchasedListener;

    public ShoppingRowsAdapter(List<ShoppingListRow> rows) {
        this.rows = rows;
    }

    // ===== Listeners =====
    public void setOnQuantityChangeListener(
            ShoppingListAdapter.OnQuantityChangeListener l) {
        quantityListener = l;
    }

    public void setOnItemCheckedChange(
            ShoppingListAdapter.OnItemCheckedChangeListener l) {
        checkedListener = l;
    }

    public void setOnClearPurchasedClick(Runnable r) {
        clearPurchasedListener = r;
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == ShoppingListRow.TYPE_HEADER) {
            View v = inflater.inflate(
                    R.layout.shopping_category_header, parent, false);
            return new HeaderVH(v);

        } else if (viewType == ShoppingListRow.TYPE_CLEAR_PURCHASED) {
            View v = inflater.inflate(
                    R.layout.row_purchased_header, parent, false);
            return new ClearPurchasedVH(v);

        } else {
            View v = inflater.inflate(
                    R.layout.shopping_item, parent, false);
            return new ItemVH(v);
        }
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder, int position) {

        ShoppingListRow row = rows.get(position);

        // ===== כותרת קטגוריה =====
        if (holder instanceof HeaderVH) {
            ((HeaderVH) holder).title.setText(row.getTitle());
            return;
        }

        // ===== נקנו + נקה הכל =====
        if (holder instanceof ClearPurchasedVH) {
            ((ClearPurchasedVH) holder).btnClear.setOnClickListener(v -> {
                if (clearPurchasedListener != null) {
                    clearPurchasedListener.run();
                }
            });
            return;
        }

        // ===== פריט =====
        ItemVH h = (ItemVH) holder;
        ShoppingItem item = row.getItem();

        h.tvName.setText(item.getName());
        h.tvQuantity.setText(String.valueOf(item.getQuantity()));
        h.tvCategoryIcon.setText(getCategoryIcon(item.getCategoryId()));

        h.imgCheck.setImageResource(
                item.isPurchased()
                        ? R.drawable.ic_check_circle
                        : R.drawable.ic_circle_empty
        );

        if (item.isPurchased()) {
            h.tvName.setPaintFlags(
                    h.tvName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            h.tvName.setAlpha(0.5f);
        } else {
            h.tvName.setPaintFlags(
                    h.tvName.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            h.tvName.setAlpha(1f);
        }

        h.imgCheck.setOnClickListener(v -> {
            boolean newState = !item.isPurchased();
            item.setPurchased(newState);
            if (checkedListener != null) {
                checkedListener.onItemCheckedChanged(item, newState);
            }
        });

        h.btnPlus.setOnClickListener(v -> {
            int q = item.getQuantity() + 1;
            item.setQuantity(q);
            if (quantityListener != null) {
                quantityListener.onQuantityChanged(item, q);
            }
        });

        h.btnMinus.setOnClickListener(v -> {
            if (item.getQuantity() <= 1) return;
            int q = item.getQuantity() - 1;
            item.setQuantity(q);
            if (quantityListener != null) {
                quantityListener.onQuantityChanged(item, q);
            }
        });
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    // ===== ViewHolders =====
    static class HeaderVH extends RecyclerView.ViewHolder {
        TextView title;
        HeaderVH(View v) {
            super(v);
            title = v.findViewById(R.id.tvHeader);
        }
    }

    static class ClearPurchasedVH extends RecyclerView.ViewHolder {
        Button btnClear;
        ClearPurchasedVH(View v) {
            super(v);
            btnClear = v.findViewById(R.id.btnClearPurchased);
        }
    }

    static class ItemVH extends RecyclerView.ViewHolder {
        TextView tvName, tvQuantity, tvCategoryIcon;
        ImageView imgCheck;
        ImageButton btnPlus, btnMinus;

        ItemVH(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvItemName);
            tvQuantity = v.findViewById(R.id.tvQuantity);
            tvCategoryIcon = v.findViewById(R.id.tvCategoryIcon);
            imgCheck = v.findViewById(R.id.imgCheck);
            btnPlus = v.findViewById(R.id.btnPlus);
            btnMinus = v.findViewById(R.id.btnMinus);
        }
    }

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
            case "other": return "🛒";
            default: return "🛒";
        }
    }

}
