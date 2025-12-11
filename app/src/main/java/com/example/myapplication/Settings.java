package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class Settings extends AppCompatActivity {

    private RecyclerView usersRecycler;
    private UsersAdapter adapter;
    private List<AppUser> usersList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        // 🔹 כפתור פלוס גדול - מעבר לעמוד הוספת משתמש
         ImageButton plos = findViewById(R.id.addUserSmall);
        plos.setOnClickListener(v ->
                startActivity(new Intent(this, Add_Users.class))
        );

        // 🔹 כפתור פלוס קטן בשורת הכותרת
        ImageButton addUserSmall = findViewById(R.id.addUserSmall);
        addUserSmall.setOnClickListener(v ->
                startActivity(new Intent(this, Add_Users.class))
        );

        // 🔹 RecyclerView הגדרת
        usersRecycler = findViewById(R.id.usersRecycler);
        usersRecycler.setLayoutManager(new LinearLayoutManager(this));

        // 🔹 יצירת מתאם
        adapter = new UsersAdapter(this, usersList);
        usersRecycler.setAdapter(adapter);

        // 🔹 טעינת משתמשים מפיירסטור
        loadUsersFromFirestore();
    }

    private void loadUsersFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .addSnapshotListener((value, error) -> {

                    if (error != null || value == null)
                        return;

                    usersList.clear();

                    for (DocumentSnapshot doc : value.getDocuments()) {

                        AppUser user = doc.toObject(AppUser.class);

                        if (user != null) {
                            user.setDocumentId(doc.getId()); // חובה למחיקה
                            usersList.add(user);
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}
