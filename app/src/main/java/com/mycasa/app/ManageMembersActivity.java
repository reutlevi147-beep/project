package com.mycasa.app;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageMembersActivity extends AppCompatActivity {

    private RecyclerView recyclerManageMembers;
    private FirebaseFirestore db;
    private String groupId;
    private String currentUserId;
    private String currentUserRole;

    private final List<Member> membersList = new ArrayList<>();
    private MembersAdapter adapter;

    // אתחול מסך ניהול חברי הקבוצה והגדרת רשימת המשתמשים
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_members);

        recyclerManageMembers = findViewById(R.id.recyclerManageMembers);
        recyclerManageMembers.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();

        // 🔹 קבלת groupId מה-Intent
        groupId = getIntent().getStringExtra("GROUP_ID");

        if (groupId == null) {
            Toast.makeText(this, "חסר groupId", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 🔹 קבלת userId מה-SharedPrefs
        SharedPreferences prefs =
                getSharedPreferences("app_prefs", MODE_PRIVATE);
        currentUserId = prefs.getString("user_id", null);

        adapter = new MembersAdapter();
        recyclerManageMembers.setAdapter(adapter);

        loadCurrentUserRole();
    }

    // טעינת תפקיד המשתמש המחובר מהשרת
    private void loadCurrentUserRole() {

        db.collection("groups")
                .document(groupId)
                .collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {
                        currentUserRole = documentSnapshot.getString("role");
                        loadMembers();
                    }
                });
    }

    // טעינת כל חברי הקבוצה ממסד הנתונים
    private void loadMembers() {

        db.collection("groups")
                .document(groupId)
                .collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    membersList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                        String name = doc.getString("name");
                        String role = doc.getString("role");
                        String userId = doc.getId();

                        membersList.add(new Member(userId, name, role));
                    }

                    adapter.notifyDataSetChanged();
                });
    }


    private class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.MemberViewHolder> {

        @NonNull
        @Override
        public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_manage_member, parent, false);

            return new MemberViewHolder(view);
        }

        // הצגת נתוני חבר קבוצה והגדרת פעולות מחיקה והרשאות
        @Override
        public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {

            Member member = membersList.get(position);

            holder.tvName.setText(member.name);

            // אות ראשונה באווטר
            if (member.name != null && member.name.length() > 0) {
                holder.avatarCircle.setText(member.name.substring(0, 1));
            }

            // תגית תפקיד
            if ("parent".equals(member.role)) {
                holder.tvRole.setText("הורה");
            } else {
                holder.tvRole.setText("ילד");
            }

            // ======================
            // הצגת אייקון מחיקה
            // רק אם הצופה הוא parent
            // ולא למחוק את עצמו
            // ======================
            if ("parent".equals(currentUserRole) &&
                    !member.userId.equals(currentUserId)) {

                holder.iconDelete.setVisibility(View.VISIBLE);
            } else {
                holder.iconDelete.setVisibility(View.GONE);
            }

            // ======================
            // הצגת אייקון הגדרות
            // רק ליד ילד
            // ======================
            if ("child".equals(member.role)) {
                holder.iconSettings.setVisibility(View.VISIBLE);
            } else {
                holder.iconSettings.setVisibility(View.GONE);
            }

            // ======================
            // פעולת מחיקה
            // ======================
            holder.iconDelete.setOnClickListener(v -> {
                showDeleteDialog(member);

            });

            // ======================
            // פעולת כניסה להרשאות ילד
            // ======================
            holder.iconSettings.setOnClickListener(v -> {

                Toast.makeText(
                        ManageMembersActivity.this,
                        "מעבר למסך הרשאות של " + member.name,
                        Toast.LENGTH_SHORT
                ).show();
                Intent intent = new Intent(holder.itemView.getContext(), ChildPermissionsActivity.class);
                intent.putExtra("GROUP_ID", groupId);
                intent.putExtra("USER_ID", member.userId);
                holder.itemView.getContext().startActivity(intent);
            });
        }

        // החזרת כמות חברי הקבוצה ברשימה
        @Override
        public int getItemCount() {
            return membersList.size();
        }

        class MemberViewHolder extends RecyclerView.ViewHolder {

            TextView tvName, tvRole, avatarCircle;
            ImageView iconDelete, iconSettings;

            public MemberViewHolder(@NonNull View itemView) {
                super(itemView);

                tvName = itemView.findViewById(R.id.tvName);
                tvRole = itemView.findViewById(R.id.tvRole);
                avatarCircle = itemView.findViewById(R.id.avatarCircle);
                iconDelete = itemView.findViewById(R.id.iconDelete);
                iconSettings = itemView.findViewById(R.id.iconSettings);
            }
        }
    }


    private static class Member {

        String userId;
        String name;
        String role;

        Member(String userId, String name, String role) {
            this.userId = userId;
            this.name = name;
            this.role = role;
        }
    }

    // הצגת חלון אישור למחיקת חבר קבוצה
    private void showDeleteDialog(Member member) {

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_delete_member);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView tvMessage = dialog.findViewById(R.id.tvDeleteMessage);
        tvMessage.setText("האם להסיר את " + member.name + " מהקבוצה?");

        MaterialButton btnConfirm = dialog.findViewById(R.id.btnConfirmDelete);
        MaterialButton btnCancel = dialog.findViewById(R.id.btnCancelDelete);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {

            db.collection("groups")
                    .document(groupId)
                    .collection("users")
                    .document(member.userId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        dialog.dismiss();
                        loadMembers();
                        Toast.makeText(this, "המשתמש הוסר", Toast.LENGTH_SHORT).show();
                    });
        });

        dialog.show();
    }

}