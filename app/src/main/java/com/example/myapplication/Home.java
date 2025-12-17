package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class Home extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // כפתור מעבר לעמוד כלכלת הבית
        ImageButton economyBtn = findViewById(R.id.H_Economy);
        economyBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, Economy.class);
            startActivity(intent);
        });

        // כפתור מעבר לעמוד קניות
        View shoppingBox = findViewById(R.id.shoppingBox);
        shoppingBox.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, Shopping.class);
            startActivity(intent);
        });

        // ✅ כפתור מעבר לעמוד משימות בית (מתוקן)
        LinearLayout tasksBtn = findViewById(R.id.tasksBox);
        tasksBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, Display_Calender_Tasks.class);
            startActivity(intent);
        });

        // כפתור מעבר לעמוד לו״ז
        ImageButton CalendersBtn = findViewById(R.id.HCalender);
        CalendersBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, Display_Calender_Tasks.class);
            startActivity(intent);
        });

        // כפתור מעבר לעמוד הגדרות
        LinearLayout settingsBox = findViewById(R.id.settingsBox);
        settingsBox.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, Settings.class);
            startActivity(intent);
        });

        // ----------------------------------------------------
        // טעינת המשתמשים לריבוע "הגדרות"
        // ----------------------------------------------------
        RecyclerView rv = findViewById(R.id.settingsUsersList);

        if (rv != null) {
            rv.setLayoutManager(new LinearLayoutManager(this));

            List<AppUser> users = new ArrayList<>();
            UsersAdapter adapter = new UsersAdapter(this, users);
            adapter.setHideDeleteIcon(true);
            rv.setAdapter(adapter);

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .get()
                    .addOnSuccessListener(query -> {
                        users.clear();

                        for (DocumentSnapshot doc : query) {
                            AppUser user = doc.toObject(AppUser.class);
                            user.setDocumentId(doc.getId());
                            users.add(user);
                        }

                        adapter.notifyDataSetChanged();
                    });
        }

        // ----------------------------------------------------
        // טעינת 4 פריטי הקניות האחרונים לריבוע "קניות"
        // ----------------------------------------------------
        RecyclerView shoppingRv = findViewById(R.id.homeShoppingPreview);
        TextView noItemsText = findViewById(R.id.noItemsText);

        if (shoppingRv != null) {
            shoppingRv.setLayoutManager(new LinearLayoutManager(this));

            List<ShoppingItem> previewList = new ArrayList<>();
            HomeShoppingAdapter previewAdapter = new HomeShoppingAdapter(previewList);
            shoppingRv.setAdapter(previewAdapter);

            FirebaseFirestore.getInstance()
                    .collection("shopping_lists")
                    .document("defaultList")
                    .collection("items")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(4)
                    .addSnapshotListener((querySnapshot, error) -> {
                        if (error != null || querySnapshot == null) return;

                        previewList.clear();

                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            ShoppingItem item = doc.toObject(ShoppingItem.class);
                            previewList.add(item);
                        }

                        previewAdapter.notifyDataSetChanged();

                        if (previewList.isEmpty()) {
                            noItemsText.setVisibility(View.VISIBLE);
                            shoppingRv.setVisibility(View.GONE);
                        } else {
                            noItemsText.setVisibility(View.GONE);
                            shoppingRv.setVisibility(View.VISIBLE);
                        }
                    });
        }
    }
}
