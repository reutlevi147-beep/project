package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AssignUsersAdapter
        extends RecyclerView.Adapter<AssignUsersAdapter.ViewHolder> {

    private final List<DocumentSnapshot> users = new ArrayList<>();
    private final Set<Integer> selectedPositions = new HashSet<>();

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_assign_user, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        DocumentSnapshot user = users.get(position);
        String name = user.getString("name");
        if (name == null) name = "משתמש";

        holder.tvUserName.setText(name);

        boolean isSelected = selectedPositions.contains(position);

        holder.itemView.setBackgroundResource(
                isSelected
                        ? R.drawable.bg_user_card_selected
                        : R.drawable.bg_user_card
        );

        holder.itemView.setOnClickListener(v -> {
            if (selectedPositions.contains(position)) {
                selectedPositions.remove(position);
            } else {
                selectedPositions.add(position);
            }
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void setUsers(List<DocumentSnapshot> newUsers) {
        users.clear();
        users.addAll(newUsers);
        selectedPositions.clear();
        notifyDataSetChanged();
    }

    public List<String> getSelectedUserIds() {
        List<String> ids = new ArrayList<>();
        for (Integer pos : selectedPositions) {
            ids.add(users.get(pos).getId());
        }
        return ids;
    }

    public void setSelectedUserIds(List<String> userIds) {
        selectedPositions.clear();
        for (int i = 0; i < users.size(); i++) {
            if (userIds.contains(users.get(i).getId())) {
                selectedPositions.add(i);
            }
        }
        notifyDataSetChanged();
    }

    public List<DocumentSnapshot> getUsers() {
        return users;
    }

    // ===== בחירת כל המשתמשים =====
    public void selectAll() {
        selectedPositions.clear();

        for (int i = 0; i < users.size(); i++) {
            selectedPositions.add(i);
        }

        notifyDataSetChanged();
    }

}
