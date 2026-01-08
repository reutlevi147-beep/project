package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class UsersAssignAdapter
        extends RecyclerView.Adapter<UsersAssignAdapter.ViewHolder> {

    private final ArrayList<AppUser> users;

    // ⭐ זה היה חסר – רשימת נבחרים
    private final ArrayList<String> selectedUserIds = new ArrayList<>();

    public UsersAssignAdapter(ArrayList<AppUser> users) {
        this.users = users;
    }

    // מאפשר ל־Activity לקרוא מי נבחר
    public ArrayList<String> getSelectedUserIds() {
        return selectedUserIds;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_assign, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppUser user = users.get(position);
        holder.tvUserName.setText(user.getName());

        boolean isSelected = selectedUserIds.contains(user.getDocumentId());

        // ⭐ כאן הציור הוויזואלי
        holder.itemView.setBackgroundResource(
                isSelected
                        ? R.drawable.bg_user_card_selected
                        : R.drawable.bg_user_card
        );

        holder.itemView.setOnClickListener(v -> {
            if (selectedUserIds.contains(user.getDocumentId())) {
                selectedUserIds.remove(user.getDocumentId());
            } else {
                selectedUserIds.add(user.getDocumentId());
            }

            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
        }
    }
    public void selectAll() {
        selectedUserIds.clear();

        for (AppUser user : users) {
            selectedUserIds.add(user.getDocumentId());
        }

        notifyDataSetChanged(); // ⭐ חובה לריענון ה־UI
    }


}
