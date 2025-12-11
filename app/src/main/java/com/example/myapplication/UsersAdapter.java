package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private Context context;
    private List<AppUser> userList;

    // ✔ מאפשר להסתיר את אייקון המחיקה בעמוד ההגדרות
    private boolean hideDeleteIcon = false;

    public void setHideDeleteIcon(boolean hide) {
        this.hideDeleteIcon = hide;
    }

    // 🎨 צבעים שונים לאייקון המשתמש
    private int[] userColors = {
            0xFF4AC7D1, // טורקיז בהיר
            0xFF3BA0C3, // טורקיז־כחול
            0xFF2E79B7, // כחול בינוני
            0xFF2A5EA8, // כחול כהה
            0xFF455A8A  // כחול-סגול
    };

    public UsersAdapter(Context context, List<AppUser> userList) {
        this.context = context;
        this.userList = userList;
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        ImageView userIcon, deleteIcon;
        TextView userNameText;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            userIcon = itemView.findViewById(R.id.userIcon);
            deleteIcon = itemView.findViewById(R.id.deleteIcon);
            userNameText = itemView.findViewById(R.id.userNameText);
        }
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        AppUser currentUser = userList.get(position);

        // ✔ מציג את שם המשתמש שהוזן מתוך Firestore
        holder.userNameText.setText(currentUser.getName());

        // 🎨 צבע אייקון מתחלף
        int colorIndex = position % userColors.length;
        holder.userIcon.setColorFilter(userColors[colorIndex]);

        // ✔ אם hideDeleteIcon = true → מסתירים את הפח
        if (hideDeleteIcon) {
            holder.deleteIcon.setVisibility(View.GONE);
        } else {
            holder.deleteIcon.setVisibility(View.VISIBLE);

            // 🗑️ מחיקה בלחיצה על הפח (קיים כמו שהיה)
            holder.deleteIcon.setOnClickListener(v -> {

                String docId = currentUser.getDocumentId();
                if (docId == null || docId.isEmpty()) return;

                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(docId)
                        .delete()
                        .addOnSuccessListener(aVoid -> {

                            userList.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, userList.size());
                        });
            });
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}
