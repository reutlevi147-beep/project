package com.example.myapplication;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ShoppingItemViewHolder> {

    private List<ShoppingItem> mShoppingList;
    private Context mContext;
    private boolean isBinding;
    public ShoppingListAdapter(Context context, List<ShoppingItem> shoppingList) {
        mContext = context;
        mShoppingList = shoppingList;
    }

    public static class ShoppingItemViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView quantityTextView;
        public CheckBox doneCheckBox;

        public ShoppingItemViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.textViewItemName);
            quantityTextView = itemView.findViewById(R.id.textViewQuantity);
            doneCheckBox = itemView.findViewById(R.id.checkBoxDone);
        }
    }

    @NonNull
    @Override
    public ShoppingItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new ShoppingItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ShoppingItemViewHolder holder, int position) {

        isBinding = true;

        ShoppingItem currentItem = mShoppingList.get(position);

        // ניתוק listener לפני כל שינוי כדי למנוע הפעלה בטעות
        holder.doneCheckBox.setOnCheckedChangeListener(null);

        // מילוי הנתונים
        holder.nameTextView.setText(currentItem.getName());
        holder.quantityTextView.setText("x" + currentItem.getQuantity());
        holder.doneCheckBox.setChecked(currentItem.isPurchased());

        // קו חוצה
        if (currentItem.isPurchased()) {
            holder.nameTextView.setPaintFlags(
                    holder.nameTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
            );
        } else {
            holder.nameTextView.setPaintFlags(
                    holder.nameTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG)
            );
        }

        isBinding = false;

        // מאזין חדש — פועל רק על לחיצה אמיתית
        holder.doneCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isBinding) return;

            // עדכון מקומי
            currentItem.setPurchased(isChecked);

            // עדכון ב-Firestore
            FirebaseFirestore.getInstance()
                    .collection("shopping_lists")
                    .document("defaultList")
                    .collection("items")
                    .document(currentItem.getDocumentId())
                    .update("isPurchased", isChecked);
        });
    }


    @Override
    public int getItemCount() {
        return mShoppingList.size();
    }
}
