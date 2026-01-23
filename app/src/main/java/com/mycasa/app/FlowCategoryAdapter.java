package com.mycasa.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FlowCategoryAdapter
        extends RecyclerView.Adapter<FlowCategoryAdapter.CategoryVH> {

    public interface OnItemChangedListener {
        void onItemChanged();
    }

    private OnItemChangedListener listener;

    public void setOnItemChangedListener(OnItemChangedListener listener) {
        this.listener = listener;
    }

    private final Context context;
    private final List<FlowCategory> categories;
    private final List<FlowItem> allItems;
    private final LayoutInflater inflater;

    public FlowCategoryAdapter(
            Context context,
            List<FlowCategory> categories,
            List<FlowItem> allItems
    ) {
        this.context = context;
        this.categories = categories;
        this.allItems = allItems;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public CategoryVH onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View v = inflater.inflate(
                R.layout.item_flow_category,
                parent,
                false
        );
        return new CategoryVH(v);
    }

    @Override
    public void onBindViewHolder(
            @NonNull CategoryVH holder, int position) {

        FlowCategory category = categories.get(position);

        holder.tvTitle.setText(category.getTitle());

        boolean expanded = category.isExpanded();

        holder.imgChevron.setRotation(expanded ? 180f : 0f);
        holder.rvItems.setVisibility(expanded ? View.VISIBLE : View.GONE);

        if (expanded) {
            List<FlowItem> itemsForCategory = new ArrayList<>();
            for (FlowItem item : allItems) {
                if (category.getId().equals(item.getCategoryId())) {
                    itemsForCategory.add(item);
                }
            }

            FlowItemAdapter itemAdapter =
                    new FlowItemAdapter(itemsForCategory);

            // ⭐️ העברת אירוע שינוי למעלה
            itemAdapter.setOnItemChangedListener(() -> {
                if (listener != null) {
                    listener.onItemChanged();
                }
            });

            holder.rvItems.setAdapter(itemAdapter);
        }

        holder.header.setOnClickListener(v -> {
            category.setExpanded(!category.isExpanded());
            notifyItemChanged(holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }

    // =========================
    // ViewHolder
    // =========================
    static class CategoryVH extends RecyclerView.ViewHolder {

        View header;
        TextView tvTitle;
        ImageView imgChevron;
        RecyclerView rvItems;

        CategoryVH(@NonNull View itemView) {
            super(itemView);

            header = itemView.findViewById(R.id.header);
            tvTitle = itemView.findViewById(R.id.tvCategoryTitle);
            imgChevron = itemView.findViewById(R.id.imgChevron);

            rvItems = itemView.findViewById(R.id.containerItems);
            rvItems.setLayoutManager(
                    new LinearLayoutManager(itemView.getContext())
            );
            rvItems.setNestedScrollingEnabled(false);
        }
    }
}
