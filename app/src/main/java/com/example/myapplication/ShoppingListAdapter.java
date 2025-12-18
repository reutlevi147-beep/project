package com.example.myapplication;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
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

        TextView nameTextView;
        TextView quantityTextView;
        CheckBox doneCheckBox;
        ImageButton btnPlus;
        ImageButton btnMinus;

        public ShoppingItemViewHolder(View itemView) {
            super(itemView);

            nameTextView = itemView.findViewById(R.id.textViewItemName);
            quantityTextView = itemView.findViewById(R.id.textViewQuantity);
            doneCheckBox = itemView.findViewById(R.id.checkBoxDone);

            // ✅ חיבור כפתורי פלוס מינוס
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnMinus = itemView.findViewById(R.id.btnMinus);
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

        ShoppingItem item = mShoppingList.get(position);

        holder.doneCheckBox.setOnCheckedChangeListener(null);

        holder.nameTextView.setText(item.getName());
        holder.quantityTextView.setText("x" + item.getQuantity());
        holder.doneCheckBox.setChecked(item.isPurchased());

        // קו חוצה
        if (item.isPurchased()) {
            holder.nameTextView.setPaintFlags(
                    holder.nameTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
            );
        } else {
            holder.nameTextView.setPaintFlags(
                    holder.nameTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG)
            );
        }

        isBinding = false;

        // ✅ צ׳קבוקס – שמירה ל-Firebase
        holder.doneCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isBinding) return;

            item.setPurchased(isChecked);

            FirebaseFirestore.getInstance()
                    .collection("shopping_lists")
                    .document("defaultList")
                    .collection("items")
                    .document(item.getDocumentId())
                    .update("isPurchased", isChecked);
        });

        // ➕ פלוס
        holder.btnPlus.setOnClickListener(v -> {
            int newQty = item.getQuantity() + 1;
            item.setQuantity(newQty);
            holder.quantityTextView.setText("x" + newQty);

            FirebaseFirestore.getInstance()
                    .collection("shopping_lists")
                    .document("defaultList")
                    .collection("items")
                    .document(item.getDocumentId())
                    .update("quantity", newQty);
        });

        // ➖ מינוס (לא יורד מתחת ל־1)
        holder.btnMinus.setOnClickListener(v -> {
            if (item.getQuantity() <= 1) return;

            int newQty = item.getQuantity() - 1;
            item.setQuantity(newQty);
            holder.quantityTextView.setText("x" + newQty);

            FirebaseFirestore.getInstance()
                    .collection("shopping_lists")
                    .document("defaultList")
                    .collection("items")
                    .document(item.getDocumentId())
                    .update("quantity", newQty);
        });
    }

    @Override
    public int getItemCount() {
        return mShoppingList.size();
    }
}
