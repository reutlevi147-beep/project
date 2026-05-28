package com.mycasa.app;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
public class FixedExpenseDrillAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_PARENT = 0;
    private static final int TYPE_CHILD = 1;
    private final List<CategoryWithItems> categories;

    private final List<CategoryWithItems> allCategories = new ArrayList<>();
    private final List<Object> rows = new ArrayList<>();

    public FixedExpenseDrillAdapter(List<CategoryWithItems> categories) {
        this.categories = categories;

        Log.d("ADAPTER_DEBUG", "parents=" + categories.size());
        for (CategoryWithItems c : categories) {
            Log.d("ADAPTER_DEBUG", "parent=" + c.title + " children=" + c.items.size());
        }
        buildRows();
        Log.d("ADAPTER_DEBUG", "rows=" + rows.size());
    }



    // בניית רשימת השורות להצגה לפי מצב פתיחה וסגירה של קטגוריות
    private void buildRows() {
        rows.clear();
        for (CategoryWithItems cat : categories) {
            rows.add(cat);
            if (cat.expanded) {
                rows.addAll(cat.items);
            }
        }
    }

    // קביעת סוג השורה: קטגוריה ראשית או תת־קטגוריה
    @Override
    public int getItemViewType(int position) {
        return rows.get(position) instanceof CategoryWithItems
                ? TYPE_PARENT
                : TYPE_CHILD;
    }

    // יצירת ViewHolder מתאים לפי סוג השורה
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_PARENT) {
            View v = inflater.inflate(
                    R.layout.item_category_parent, parent, false);
            return new ParentVH(v);
        } else {
            View v = inflater.inflate(
                    R.layout.item_category_child, parent, false);
            return new ChildVH(v);
        }
    }

    // הצגת נתוני השורה לפי סוג הפריט
    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder, int position) {

        Object row = rows.get(position);
        android.util.Log.d("ADAPTER_ROWS",
                "pos=" + position + " type=" + row.getClass().getSimpleName());

        if (holder instanceof ParentVH) {
            bindParent((ParentVH) holder, (CategoryWithItems) row);
        } else {
            bindChild((ChildVH) holder, (SubCategoryItem) row);
        }
    }

    // הצגת קטגוריה ראשית וטיפול בפתיחה וסגירה שלה
    private void bindParent(ParentVH h, CategoryWithItems cat) {

        h.tvTitle.setText(cat.title);
        h.tvAmount.setText("₪" +
                String.format(Locale.US, "%,.0f", cat.totalAmount));

        h.dot.setBackgroundColor(cat.color);
        h.arrow.setRotation(cat.expanded ? 180 : 0);

        // 👈👈👈 כאן ה־onClick
        h.itemView.setOnClickListener(v -> {
            cat.expanded = !cat.expanded;
            buildRows();
            notifyDataSetChanged();
        });
    }

    // הצגת תת־קטגוריה וסכום ההוצאה שלה
    private void bindChild(ChildVH h, SubCategoryItem item) {
        h.tvTitle.setText(item.title);
        h.tvAmount.setText("₪" +
                String.format(Locale.US, "%,.0f", item.amount));
    }

    // החזרת כמות השורות המוצגות ברשימה
    @Override
    public int getItemCount() {
        android.util.Log.d("ADAPTER_ROWS", "rows.size=" + rows.size());
        return rows.size();
    }


    static class ParentVH extends RecyclerView.ViewHolder {
        View dot;
        TextView tvTitle, tvAmount;
        ImageView arrow;

        ParentVH(View v) {
            super(v);
            dot = v.findViewById(R.id.viewColorDot);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvAmount = v.findViewById(R.id.tvAmount);
            arrow = v.findViewById(R.id.imgArrow);
        }
    }

    static class ChildVH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAmount;

        ChildVH(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvChildTitle);
            tvAmount = v.findViewById(R.id.tvChildAmount);
        }
    }

}
