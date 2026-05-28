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

    // הגדרת מאזין לעדכון שינויים בפריטי הקטגוריה
    public void setOnItemChangedListener(OnItemChangedListener listener) {
        this.listener = listener;
    }
    public interface OnAddIncomeClickListener {
        void onAddIncomeClicked();
    }

    private final Context context;
    private final List<FlowCategory> categories;
    private final List<FlowItem> allItems;
    private final LayoutInflater inflater;
    private OnAddIncomeClickListener addIncomeListener;

    // הגדרת מאזין להוספת שורת הכנסה חדשה
    public void setOnAddIncomeClickListener(OnAddIncomeClickListener l) {
        this.addIncomeListener = l;
    }

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

    // יצירת ViewHolder עבור קטגוריה פיננסית
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

    // הצגת נתוני הקטגוריה וטעינת הפריטים המשויכים אליה
    @Override
    public void onBindViewHolder(
            @NonNull CategoryVH holder, int position) {

        FlowCategory category = categories.get(position);

        holder.tvTitle.setText(category.getTitle());

        boolean expanded = category.isExpanded();

        holder.imgChevron.setRotation(expanded ? 180f : 0f);
        holder.rvItems.setVisibility(expanded ? View.VISIBLE : View.GONE);

        // 👇 רק ל־income_work
        if (category.getId().equals("income_work") && expanded) {

            holder.showAddIncome(true);

            holder.btnAddIncome.setOnClickListener(v -> {
                if (addIncomeListener != null) {
                    addIncomeListener.onAddIncomeClicked();
                }
            });

        } else {
            holder.showAddIncome(false);
        }

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

    // החזרת כמות הקטגוריות ברשימה
    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }


    static class CategoryVH extends RecyclerView.ViewHolder {

        View header;
        TextView tvTitle;
        ImageView imgChevron;
        RecyclerView rvItems;

        // ⭐️ חדש
        LinearLayout addIncomeRow;
        TextView btnAddIncome;

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

            // ⭐️ חיבור רכיבי "הוסף משכורת"
            addIncomeRow = itemView.findViewById(R.id.rowAddIncome);
            btnAddIncome = itemView.findViewById(R.id.btnAddIncome);
        }

        // הצגה או הסתרה של כפתור הוספת הכנסה
        void showAddIncome(boolean show) {
            if (addIncomeRow != null) {
                addIncomeRow.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        }
    }


}
