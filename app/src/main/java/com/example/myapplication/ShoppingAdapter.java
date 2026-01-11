package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ShoppingAdapter extends RecyclerView.Adapter<ShoppingAdapter.ViewHolder> {

    private List<ShoppingItem> items;

    public ShoppingAdapter(List<ShoppingItem> items) {
        this.items = items;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvName);

        }
    }

    @NonNull
    @Override
    public ShoppingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.shopping_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShoppingAdapter.ViewHolder holder, int position) {
        ShoppingItem item = items.get(position);
        String icon = getCategoryIcon(item.getCategoryId());
        holder.name.setText(icon + "  " + item.getName());

    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    private String getCategoryIcon(String categoryId) {
        if (categoryId == null) return "🛒";

        switch (categoryId) {
            case "veg": return "🥬";
            case "dairy": return "🥛";
            case "meat": return "🍖";
            case "dry": return "🌾";
            case "cleaning": return "🧼";
            case "other": return "🛒";
            default: return "🛒";
        }
    }

}
