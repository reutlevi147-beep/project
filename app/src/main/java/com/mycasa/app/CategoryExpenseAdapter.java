package com.mycasa.app;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class CategoryExpenseAdapter
        extends RecyclerView.Adapter<CategoryExpenseAdapter.VH> {

    private final List<CategoryExpenseItem> items;

    public CategoryExpenseAdapter(List<CategoryExpenseItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_expense, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        CategoryExpenseItem item = items.get(position);

        h.tvTitle.setText(item.title);
        h.tvAmount.setText("₪" + String.format(Locale.US, "%,.0f", item.amount));
        GradientDrawable bg = (GradientDrawable) h.dot.getBackground();
        bg.setColor(item.color);

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        View dot;
        TextView tvTitle, tvAmount;

        VH(View v) {
            super(v);
            dot = v.findViewById(R.id.viewColorDot);
            tvTitle = v.findViewById(R.id.tvCategoryTitle);
            tvAmount = v.findViewById(R.id.tvCategoryAmount);
        }
    }
}
