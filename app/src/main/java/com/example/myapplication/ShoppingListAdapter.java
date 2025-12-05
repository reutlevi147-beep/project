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
import java.util.List;

public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ShoppingItemViewHolder> {

    private List<ShoppingItem> mShoppingList;
    private Context mContext;

    // הקונסטרוקטור: מקבל את רשימת הנתונים
    public ShoppingListAdapter(Context context, List<ShoppingItem> shoppingList) {
        mContext = context;
        mShoppingList = shoppingList;
    }

    // ********* 1. הגדרת ה-ViewHolder הפנימי *********
    // מחזיק את ה-Views שנוצרו ב-list_item.xml
    public static class ShoppingItemViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView quantityTextView;
        public CheckBox doneCheckBox;

        public ShoppingItemViewHolder(View itemView) {
            super(itemView);
            // מאתר את הרכיבים מתוך list_item.xml
            nameTextView = itemView.findViewById(R.id.textViewItemName);
            quantityTextView = itemView.findViewById(R.id.textViewQuantity);
            doneCheckBox = itemView.findViewById(R.id.checkBoxDone);

            // הערה: כאן ניתן להוסיף Event Listeners לפריט כולו (אם לוחצים עליו)
        }
    }

    // ********* 2. יצירת View Holder *********
    @NonNull
    @Override
    public ShoppingItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // מנפח (Inflate) את קובץ ה-XML של השורה הבודדת
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_item, parent, false);
        return new ShoppingItemViewHolder(v);
    }

    // ********* 3. קשירת הנתונים ל-Views *********
    @Override
    public void onBindViewHolder(@NonNull ShoppingItemViewHolder holder, int position) {
        // קבלת הפריט הנוכחי מהרשימה
        ShoppingItem currentItem = mShoppingList.get(position);

        // מילוי ה-Views בנתונים
        holder.nameTextView.setText(currentItem.getName());
        holder.quantityTextView.setText("x" + currentItem.getQuantity());
        holder.doneCheckBox.setChecked(currentItem.isPurchased());

        // (בונוס) קו דק על הפריט אם הוא סומן כנקנה
        if (currentItem.isPurchased()) {
            holder.nameTextView.setPaintFlags(holder.nameTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.nameTextView.setPaintFlags(holder.nameTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        // הערה: כאן נוסיף Event Listeners ל-CheckBox בשלב 7 (עריכה)
    }

    // ********* 4. החזרת גודל הרשימה *********
    @Override
    public int getItemCount() {
        return mShoppingList.size();
    }
}