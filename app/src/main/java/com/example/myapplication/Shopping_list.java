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

    private ShoppingListAdapter mAdapter;
    private List<ShoppingItem> mItems;

    private FirebaseFirestore db;
    private RecyclerView mRecyclerView;

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

        // ❗ חוסם אנימציות שינוי — חשוב ליציבות צ'קבוקסים
        RecyclerView.ItemAnimator animator = mRecyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        // טעינה יציבה
        loadItems();

        // כפתור פלוס
        ImageButton plos = findViewById(R.id.Plos);
        plos.setOnClickListener(v -> {
            Intent intent = new Intent(Shopping_list.this, Add_Shopping.class);
            startActivity(intent);
        });

        // מחיקה
        Button deletePurchasedBtn = findViewById(R.id.deletePurchasedBtn);
        deletePurchasedBtn.setOnClickListener(v -> deletePurchasedItems());
    }

    // ⭐ מחיקה יציבה
    private void deletePurchasedItems() {

        List<ShoppingItem> itemsToDelete = new ArrayList<>(mItems);

        for (ShoppingItem item : itemsToDelete) {
            if (item.isPurchased()) {

                // מחיקה מה-Firestore
                db.collection(MAIN_COLLECTION)
                        .document(DOCUMENT_ID)
                        .collection(ITEMS_SUB_COLLECTION)
                        .document(item.getDocumentId())
                        .delete();

                // מחיקה מידית מהמסך
                mItems.remove(item);
            }
        }

        mAdapter.notifyDataSetChanged();
        loadItems(); // טוען מחדש מ-Firebase כדי למנוע פערים

        Toast.makeText(this, "כל הפריטים שנקנו נמחקו", Toast.LENGTH_SHORT).show();
    }

    // ⭐ טעינה יציבה — לא SnapshotListener
    private void loadItems() {

        db.collection(MAIN_COLLECTION)
                .document(DOCUMENT_ID)
                .collection(ITEMS_SUB_COLLECTION)
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(value -> {

                    mItems.clear();

                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {

                            ShoppingItem item = doc.toObject(ShoppingItem.class);

                            if (item != null) {
                                item.setDocumentId(doc.getId());
                                mItems.add(item);
                            }
                        }
                    }

                    mAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("Shopping_list", "Load failed!", e));
    }
}
