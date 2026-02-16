package com.mycasa.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TransactionCategoryAdapter
        extends RecyclerView.Adapter<TransactionCategoryAdapter.VH> {

    public interface OnCategoryClickListener {
        void onClick(String categoryId);
    }

    private final List<String> categories;
    private final OnCategoryClickListener listener;
    private String selectedId = null;

    public TransactionCategoryAdapter(List<String> categories,
                                      OnCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {

        String categoryId = categories.get(position);

        holder.tvName.setText(
                FinanceCatalog.getCategoryTitle(categoryId)
        );

        holder.tvIcon.setText(
                FinanceCatalog.getCategoryIcon(categoryId)
        );

        holder.itemView.setOnClickListener(v -> {
            selectedId = categoryId;
            notifyDataSetChanged();
            listener.onClick(categoryId);
        });

        if (categoryId.equals(selectedId)) {
            holder.itemView.setAlpha(1f);
        } else {
            holder.itemView.setAlpha(0.5f);
        }
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        TextView tvIcon, tvName;

        VH(@NonNull View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.tvIcon);
            tvName = itemView.findViewById(R.id.tvName);
        }
    }
}
