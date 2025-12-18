package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Shopping_list extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ShoppingListAdapter mAdapter;
    private List<ShoppingItem> mItems;
    private FirebaseFirestore db;

    private static final String MAIN_COLLECTION = "shopping_lists";
    private static final String DOCUMENT_ID = "defaultList";
    private static final String ITEMS_SUB_COLLECTION = "items";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shopping_list);

        db = FirebaseFirestore.getInstance();
        mItems = new ArrayList<>();

        // RecyclerView
        mRecyclerView = findViewById(R.id.shoppingRecycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new ShoppingListAdapter(this, mItems);
        mRecyclerView.setAdapter(mAdapter);

        // ❗ חשוב – מונע קפיצות בצ׳קבוקסים
        RecyclerView.ItemAnimator animator = mRecyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        // טעינת נתונים
        loadItems();

        // ➕ מעבר להוספת מוצר
        ImageButton plos = findViewById(R.id.Plos);
        plos.setOnClickListener(v ->
                startActivity(new Intent(this, Add_Shopping.class))
        );

        // 🗑 מחיקת פריטים שנקנו
        Button deletePurchasedBtn = findViewById(R.id.deletePurchasedBtn);
        deletePurchasedBtn.setOnClickListener(v -> deletePurchasedItems());
    }

    // ======================
    // טעינת פריטים
    // ======================
    private void loadItems() {

        db.collection(MAIN_COLLECTION)
                .document(DOCUMENT_ID)
                .collection(ITEMS_SUB_COLLECTION)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(value -> {

                    mItems.clear();

                    for (QueryDocumentSnapshot doc : value) {
                        ShoppingItem item = doc.toObject(ShoppingItem.class);
                        if (item != null) {
                            item.setDocumentId(doc.getId());
                            mItems.add(item);
                        }
                    }

                    mAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Log.e("Shopping_list", "Load failed", e)
                );
    }

    // ======================
    // מחיקת פריטים שנקנו
    // ======================
    private void deletePurchasedItems() {

        List<ShoppingItem> copy = new ArrayList<>(mItems);

        for (ShoppingItem item : copy) {
            if (item.isPurchased()) {

                db.collection(MAIN_COLLECTION)
                        .document(DOCUMENT_ID)
                        .collection(ITEMS_SUB_COLLECTION)
                        .document(item.getDocumentId())
                        .delete();

                mItems.remove(item);
            }
        }

        mAdapter.notifyDataSetChanged();
        Toast.makeText(this, "פריטים שנקנו נמחקו", Toast.LENGTH_SHORT).show();
    }
}
