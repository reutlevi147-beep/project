package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Shopping_list extends AppCompatActivity {

    private ShoppingListAdapter mAdapter;
    private List<ShoppingItem> mItems = new ArrayList<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecyclerView mRecyclerView;

    // נתיבים — תואם ל-Add_Shopping
    private static final String MAIN_COLLECTION = "shopping_lists";
    private static final String DOCUMENT_ID = "defaultList";
    private static final String ITEMS_SUB_COLLECTION = "items";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shopping_list);

        // RecyclerView
        mRecyclerView = findViewById(R.id.shoppingRecycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Adapter
        mAdapter = new ShoppingListAdapter(this, mItems);
        mRecyclerView.setAdapter(mAdapter);

        // טעינת פריטים מה־Firestore
        loadItems();

        // כפתור פלוס – מעבר להוספת מוצר
        ImageButton plos = findViewById(R.id.Plos);
        plos.setOnClickListener(v -> {
            Intent intent = new Intent(Shopping_list.this, Add_Shopping.class);
            startActivity(intent);
        });
    }

    private void loadItems() {

        // טעינה מהנתיב הנכון:
        // shopping_lists → defaultList → items
        db.collection(MAIN_COLLECTION)
                .document(DOCUMENT_ID)
                .collection(ITEMS_SUB_COLLECTION)
                .orderBy("name", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {

                    if (error != null) {
                        Log.e("Shopping_list", "Listen failed.", error);
                        return;
                    }

                    if (value == null) return;

                    mItems.clear();

                    for (QueryDocumentSnapshot doc : value) {
                        ShoppingItem item = doc.toObject(ShoppingItem.class);

                        if (item != null) {
                            item.setDocumentId(doc.getId()); // חשוב למחיקה / עדכון בעתיד
                            mItems.add(item);
                        }
                    }

                    // עדכון ה־RecyclerView
                    mAdapter.notifyDataSetChanged();
                });
    }
}
