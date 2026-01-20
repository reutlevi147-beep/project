package com.mycasa.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FlowCategoryAdapter
        extends RecyclerView.Adapter<FlowCategoryAdapter.CategoryViewHolder> {

    private final Context context;
    private final List<FlowCategory> categories;

    public FlowCategoryAdapter(Context context, List<FlowCategory> categories) {
        this.context = context;
        this.categories = categories;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_flow_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull CategoryViewHolder holder, int position) {

        FlowCategory category = categories.get(position);

        holder.tvTitle.setText(category.title);

        // פתיחה / סגירה
        holder.containerItems.setVisibility(
                category.isExpanded ? View.VISIBLE : View.GONE
        );

        holder.imgChevron.setRotation(
                category.isExpanded ? 180f : 0f
        );

        // ניקוי פריטים ישנים
        holder.containerItems.removeAllViews();

        // הוספת פריטים
        for (FlowItem item : category.items) {
            View itemView = LayoutInflater.from(context)
                    .inflate(R.layout.item_flow_item, holder.containerItems, false);

            ImageView icon = itemView.findViewById(R.id.imgIcon);
            TextView name = itemView.findViewById(R.id.tvName);
            TextView amount = itemView.findViewById(R.id.tvAmount);

            icon.setImageResource(item.iconRes);
            name.setText(item.name);
            amount.setText(
                    item.amount > 0 ? "₪" + (int) item.amount : "לא הוגדר"
            );

            holder.containerItems.addView(itemView);
        }

        // קליק על כותרת
        holder.header.setOnClickListener(v -> {
            category.isExpanded = !category.isExpanded;
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    // =========================
    // ViewHolder
    // =========================
    static class CategoryViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle;
        ImageView imgChevron;
        LinearLayout containerItems;
        View header;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);

            header = itemView.findViewById(R.id.header);
            tvTitle = itemView.findViewById(R.id.tvCategoryTitle);
            imgChevron = itemView.findViewById(R.id.imgChevron);
            containerItems = itemView.findViewById(R.id.containerItems);
        }
    }
}
