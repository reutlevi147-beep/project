package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HomeShoppingAdapter extends RecyclerView.Adapter<HomeShoppingAdapter.ViewHolder> {

    private List<ShoppingItem> list;

    public HomeShoppingAdapter(List<ShoppingItem> list) {
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textViewNamePreview);
        }
    }

    @NonNull
    @Override
    public HomeShoppingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.shopping_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeShoppingAdapter.ViewHolder holder, int position) {
        ShoppingItem item = list.get(position);
        holder.name.setText(item.getName());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
