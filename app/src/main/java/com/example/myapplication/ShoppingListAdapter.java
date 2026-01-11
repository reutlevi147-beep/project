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
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ShoppingListRow> rows;

    // ===== Listeners =====
    public interface OnQuantityChangeListener {
        void onQuantityChanged(ShoppingItem item, int newQuantity);
    }

    public interface OnItemCheckedChangeListener {
        void onItemCheckedChanged(ShoppingItem item, boolean checked);
    }

    private OnQuantityChangeListener quantityChangeListener;
    private OnItemCheckedChangeListener checkedChangeListener;

    public void setOnQuantityChangeListener(OnQuantityChangeListener l) {
        quantityChangeListener = l;
    }

    public void setOnItemCheckedChange(OnItemCheckedChangeListener l) {
        checkedChangeListener = l;
    }

    public ShoppingListAdapter(List<ShoppingListRow> rows) {
        this.rows = rows;
    }

    // ===== ViewHolders =====
    static class HeaderVH extends RecyclerView.ViewHolder {
        TextView title;
        HeaderVH(View v) {
            super(v);
            title = v.findViewById(R.id.tvHeaderTitle);
        }
    }

    static class ItemVH extends RecyclerView.ViewHolder {
        TextView tvName, tvCategoryIcon, tvQuantity;
        ImageButton btnPlus, btnMinus;
        ImageView imgCheck;

        ItemVH(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvItemName);
            tvCategoryIcon = v.findViewById(R.id.tvCategoryIcon);
            tvQuantity = v.findViewById(R.id.tvQuantity);
            btnPlus = v.findViewById(R.id.btnPlus);
            btnMinus = v.findViewById(R.id.btnMinus);
            imgCheck = v.findViewById(R.id.imgCheck);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        if (viewType == ShoppingListRow.TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_category_header, parent, false);
            return new HeaderVH(v);
        }

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.shopping_item, parent, false);
        return new ItemVH(v);
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder, int position) {

        ShoppingListRow row = rows.get(position);

        if (row.getType() == ShoppingListRow.TYPE_HEADER) {
            ((HeaderVH) holder).title.setText(row.getHeaderTitle());
            return;
        }

        ShoppingItem item = row.getItem();
        ItemVH h = (ItemVH) holder;

        h.tvName.setText(item.getName());
        h.tvCategoryIcon.setText(getCategoryIcon(item.getCategoryId()));
        h.tvQuantity.setText(String.valueOf(item.getQuantity()));

        h.imgCheck.setImageResource(
                item.isPurchased()
                        ? R.drawable.ic_check_circle
                        : R.drawable.ic_circle_empty
        );

        h.imgCheck.setOnClickListener(v -> {
            boolean newState = !item.isPurchased();
            item.setPurchased(newState);

            h.imgCheck.setImageResource(
                    newState
                            ? R.drawable.ic_check_circle
                            : R.drawable.ic_circle_empty
            );

            if (checkedChangeListener != null) {
                checkedChangeListener.onItemCheckedChanged(item, newState);
            }
        });

        h.btnPlus.setOnClickListener(v -> {
            int q = item.getQuantity() + 1;
            item.setQuantity(q);
            h.tvQuantity.setText(String.valueOf(q));
            if (quantityChangeListener != null) {
                quantityChangeListener.onQuantityChanged(item, q);
            }
        });

        h.btnMinus.setOnClickListener(v -> {
            if (item.getQuantity() <= 1) return;
            int q = item.getQuantity() - 1;
            item.setQuantity(q);
            h.tvQuantity.setText(String.valueOf(q));
            if (quantityChangeListener != null) {
                quantityChangeListener.onQuantityChanged(item, q);
            }
        });

        if (item.isPurchased()) {
            h.tvName.setPaintFlags(
                    h.tvName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            h.tvName.setAlpha(0.5f);
        } else {
            h.tvName.setPaintFlags(
                    h.tvName.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            h.tvName.setAlpha(1f);
        }
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    private String getCategoryIcon(String id) {
        if (id == null) return "🛒";
        switch (id) {
            case "veg": return "🥬";
            case "dairy": return "🥛";
            case "meat": return "🍖";
            case "dry": return "🌾";
            case "cleaning": return "🧼";
            default: return "🛒";
        }
    }
}
