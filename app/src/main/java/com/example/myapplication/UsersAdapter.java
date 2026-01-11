package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    private final Context context;
    private final List<User> users;

    public UsersAdapter(Context context, List<User> users) {
        this.context = context;
        this.users = users;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvRole, tvPhone, tvEmail;
        ImageView imgAvatar;
        Button btnEditProfile;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvName);
            tvRole = itemView.findViewById(R.id.tvRole);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            btnEditProfile = itemView.findViewById(R.id.btnEditProfile);
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

        // ✅ צבע אווטאר מתוך Firestore
        applyAvatarColor(holder.imgAvatar, user.getAvatarColor());

        holder.btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditUserActivity.class);
            intent.putExtra("userId", user.getId());
            context.startActivity(intent);
        });
    }


    private void applyAvatarColor(ImageView imgAvatar, String color) {

        // אייקון תמיד לבן
        imgAvatar.setImageResource(R.drawable.ic_user);
        imgAvatar.setColorFilter(
                ContextCompat.getColor(imgAvatar.getContext(), android.R.color.white)
        );

        int bgColor = R.color.avatar_beige; // ברירת מחדל עדינה

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
