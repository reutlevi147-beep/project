package com.mycasa.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    private final Context context;
    private final List<User> users;
    private final String currentUserId;

    public UsersAdapter(Context context, List<User> users) {
        this.context = context;
        this.users = users;

        SharedPreferences prefs =
                context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        currentUserId = prefs.getString("user_id", null);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        MaterialCardView card;
        TextView tvName, tvRole, tvPhone, tvEmail, tvParentBadge;
        ImageView imgAvatar;
        Button btnEditProfile;
        View divider;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            card = (MaterialCardView) itemView;

            tvName = itemView.findViewById(R.id.tvName);
            tvRole = itemView.findViewById(R.id.tvRole);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvParentBadge = itemView.findViewById(R.id.tvParentBadge);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            btnEditProfile = itemView.findViewById(R.id.btnEditProfile);
            divider = itemView.findViewById(R.id.viewDivider);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_card, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        User user = users.get(position);

        holder.tvName.setText(user.getName());
        holder.tvRole.setText(user.getRole());
        holder.tvPhone.setText(user.getPhone());
        holder.tvEmail.setText(user.getEmail());

        applyAvatarColor(holder.imgAvatar, user.getAvatarColor());

        // 👑 Badge להורה
        if ("parent".equals(user.getRole())) {
            holder.tvParentBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvParentBadge.setVisibility(View.GONE);
        }

        if (user.getId() != null && user.getId().equals(currentUserId)) {

            // מסגרת מודגשת
            holder.card.setStrokeWidth(4);
            holder.card.setStrokeColor(
                    ContextCompat.getColor(context, R.color.avatar_teal)
            );

            // צל קצת יותר חזק
            holder.card.setCardElevation(12f);

            holder.divider.setVisibility(View.VISIBLE);
            holder.btnEditProfile.setVisibility(View.VISIBLE);

        } else {

            // בלי מסגרת
            holder.card.setStrokeWidth(0);
            holder.card.setCardElevation(6f);

            holder.divider.setVisibility(View.GONE);
            holder.btnEditProfile.setVisibility(View.GONE);
        }
        holder.btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditUserActivity.class);
            intent.putExtra("USER_ID", user.getId());
            intent.putExtra("GROUP_ID", getGroupIdFromPrefs());
            context.startActivity(intent);
        });
    }

    private String getGroupIdFromPrefs() {
        SharedPreferences prefs =
                context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        return prefs.getString("group_id", null);
    }

    private void applyAvatarColor(ImageView imgAvatar, String color) {

        imgAvatar.setImageResource(R.drawable.ic_user);
        imgAvatar.setColorFilter(
                ContextCompat.getColor(imgAvatar.getContext(), android.R.color.white)
        );

        int bgColor = R.color.avatar_beige;

        if (color != null) {
            switch (color) {
                case "blue":
                    bgColor = R.color.avatar_blue;
                    break;
                case "purple":
                    bgColor = R.color.avatar_purple;
                    break;
                case "red":
                    bgColor = R.color.avatar_red;
                    break;
                case "pink":
                    bgColor = R.color.avatar_pink;
                    break;
                case "teal":
                    bgColor = R.color.avatar_teal;
                    break;
            }
        }

        imgAvatar.setBackgroundTintList(
                ContextCompat.getColorStateList(
                        imgAvatar.getContext(), bgColor
                )
        );
    }

    @Override
    public int getItemCount() {
        return users.size();
    }
}