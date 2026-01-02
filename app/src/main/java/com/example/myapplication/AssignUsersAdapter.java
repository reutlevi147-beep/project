package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AssignUsersAdapter
        extends RecyclerView.Adapter<AssignUsersAdapter.UserViewHolder> {

    private final List<DocumentSnapshot> users = new ArrayList<>();
    private final Set<Integer> selectedPositions = new HashSet<>();

    static class UserViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbUser;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            cbUser = itemView.findViewById(R.id.cbSelectUser);
        }
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_assign_user, parent, false);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(
            @NonNull UserViewHolder holder,
            int position
    ) {
        DocumentSnapshot user = users.get(position);
        String name = user.getString("name");
        if (name == null) name = "משתמש";

        holder.cbUser.setOnCheckedChangeListener(null);
        holder.cbUser.setText(name);
        holder.cbUser.setChecked(selectedPositions.contains(position));

        holder.cbUser.setOnCheckedChangeListener((btn, isChecked) -> {
            if (isChecked) {
                selectedPositions.add(position);
            } else {
                selectedPositions.remove(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    // ===== Data =====

    public void setUsers(List<DocumentSnapshot> newUsers) {
        users.clear();
        users.addAll(newUsers);
        selectedPositions.clear();
        notifyDataSetChanged();
    }

    /** 🔹 IDs של משתמשים שנבחרו */
    public List<String> getSelectedUserIds() {
        List<String> ids = new ArrayList<>();
        for (Integer pos : selectedPositions) {
            ids.add(users.get(pos).getId());
        }
        return ids;
    }

    /** 🔹 שמות משתמשים שנבחרו */
    public List<String> getSelectedUserNamesList() {
        List<String> names = new ArrayList<>();

        for (Integer pos : selectedPositions) {
            String name = users.get(pos).getString("name");
            if (name != null && !name.isEmpty()) {
                names.add(name);
            }
        }

        return names;
    }

    /** 🔹 סימון משתמשים לעריכה */
    public void setSelectedUserIds(List<String> userIds) {
        selectedPositions.clear();

        for (int i = 0; i < users.size(); i++) {
            if (userIds.contains(users.get(i).getId())) {
                selectedPositions.add(i);
            }
        }

        notifyDataSetChanged();
    }
}
