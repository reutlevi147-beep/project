package com.mycasa.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public class SuggestedItemsAdapter
        extends RecyclerView.Adapter<SuggestedItemsAdapter.ViewHolder> {

    public interface OnSuggestionClickListener {
        void onClick(DocumentSnapshot item);
    }

    private final List<DocumentSnapshot> items;
    private final OnSuggestionClickListener listener;

    public SuggestedItemsAdapter(
            List<DocumentSnapshot> items,
            OnSuggestionClickListener listener
    ) {
        this.items = items;
        this.listener = listener;
    }

    // יצירת ViewHolder עבור פריט מוצע ברשימה
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_suggested_product, parent, false);
        return new ViewHolder(view);
    }

    // הצגת נתוני פריט מוצע וטיפול בלחיצה עליו
    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        DocumentSnapshot doc = items.get(position);

        String name = doc.getString("name");
        holder.tvName.setText(name != null ? name : "");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(doc);
            }
        });
    }

    // הצגת נתוני פריט מוצע וטיפול בלחיצה עליו
    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
        }
    }
}
