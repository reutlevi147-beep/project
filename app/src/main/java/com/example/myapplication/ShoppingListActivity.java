package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ShoppingListActivity extends AppCompatActivity {

    // 🛒 לקנות
    private RecyclerView recyclerToBuy;
    private ShoppingListAdapter toBuyAdapter;
    private final List<ShoppingItem> toBuyItems = new ArrayList<>();

    // ✅ נקנו
    private RecyclerView recyclerPurchased;
    private ShoppingListAdapter purchasedAdapter;
    private final List<ShoppingItem> purchasedItems = new ArrayList<>();

    // 🔢 סיכום
    private TextView tvToBuyCount, tvPurchasedCount;
    private TextView tvToBuyLabel, tvPurchasedLabel;

    // UI
    private LinearLayout completedSection;
    private TextView btnClearPurchased;

    private FirebaseFirestore db;
    private ListenerRegistration shoppingListener;

    private static final String MAIN_COLLECTION = "shopping_lists";
    private static final String DOCUMENT_ID = "defaultList";
    private static final String ITEMS_SUB_COLLECTION = "items";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shopping);

        db = FirebaseFirestore.getInstance();

        // 🔙 חזרה
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // ===== כרטיסי סיכום =====
        View cardToBuy = findViewById(R.id.cardToBuy);
        View cardPurchased = findViewById(R.id.cardPurchased);

        tvToBuyCount = cardToBuy.findViewById(R.id.tvCount);
        tvToBuyLabel = cardToBuy.findViewById(R.id.tvLabel);

        tvPurchasedCount = cardPurchased.findViewById(R.id.tvCount);
        tvPurchasedLabel = cardPurchased.findViewById(R.id.tvLabel);

        tvToBuyLabel.setText("לקנות");
        tvPurchasedLabel.setText("נקנו");

        // ===== Views =====
        recyclerToBuy = findViewById(R.id.recyclerToBuy);
        recyclerPurchased = findViewById(R.id.recyclerPurchased);
        completedSection = findViewById(R.id.completedSection);
        btnClearPurchased = findViewById(R.id.btnClearPurchased);

        // ===== RecyclerViews =====
        recyclerToBuy.setLayoutManager(new LinearLayoutManager(this));
        recyclerPurchased.setLayoutManager(new LinearLayoutManager(this));

        toBuyAdapter = new ShoppingListAdapter(toBuyItems);
        purchasedAdapter = new ShoppingListAdapter(purchasedItems);

        recyclerToBuy.setAdapter(toBuyAdapter);
        recyclerPurchased.setAdapter(purchasedAdapter);

        disableChangeAnimations(recyclerToBuy);
        disableChangeAnimations(recyclerPurchased);

        attachAdapterListeners(toBuyAdapter);
        attachAdapterListeners(purchasedAdapter);

        // 🗑️ מחיקת כל הנקנו
        btnClearPurchased.setOnClickListener(v -> {
            for (ShoppingItem item : purchasedItems) {
                db.collection(MAIN_COLLECTION)
                        .document(DOCUMENT_ID)
                        .collection(ITEMS_SUB_COLLECTION)
                        .document(item.getDocumentId())
                        .delete();
            }
        });

        // ➕ הוספת מוצר
        findViewById(R.id.fabAdd).setOnClickListener(v ->
                startActivity(new Intent(this, Add_Shopping.class))
        );
    }

    private void attachAdapterListeners(ShoppingListAdapter adapter) {

        // 🔢 שינוי כמות
        adapter.setOnQuantityChangeListener((item, newQuantity) ->
                db.collection(MAIN_COLLECTION)
                        .document(DOCUMENT_ID)
                        .collection(ITEMS_SUB_COLLECTION)
                        .document(item.getDocumentId())
                        .update("quantity", newQuantity)
        );

        // ✔️ סימון נקנה – ✔️ שדה נכון
        adapter.setOnItemCheckedChange((item, checked) ->
                db.collection(MAIN_COLLECTION)
                        .document(DOCUMENT_ID)
                        .collection(ITEMS_SUB_COLLECTION)
                        .document(item.getDocumentId())
                        .update("purchased", checked)
        );
    }


    private void disableChangeAnimations(RecyclerView rv) {
        RecyclerView.ItemAnimator animator = rv.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
    }

    // =====================
    // 🔄 SnapshotListener
    // =====================
    private void listenToShoppingItems() {
        shoppingListener = db
                .collection(MAIN_COLLECTION)
                .document(DOCUMENT_ID)
                .collection(ITEMS_SUB_COLLECTION)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {

                    if (e != null) {
                        Log.e("SHOPPING", "Listen failed", e);
                        return;
                    }

                    if (snapshots == null) return;

                    toBuyItems.clear();
                    purchasedItems.clear();

                    for (QueryDocumentSnapshot doc : snapshots) {
                        ShoppingItem item = doc.toObject(ShoppingItem.class);
                        if (item == null) continue;

                        item.setDocumentId(doc.getId());

                        if (item.isPurchased()) {
                            purchasedItems.add(item);
                        } else {
                            toBuyItems.add(item);
                        }
                    }

                    // 🔢 עדכון סיכום
                    tvToBuyCount.setText(String.valueOf(toBuyItems.size()));
                    tvPurchasedCount.setText(String.valueOf(purchasedItems.size()));

                    completedSection.setVisibility(
                            purchasedItems.isEmpty() ? View.GONE : View.VISIBLE
                    );

                    toBuyAdapter.notifyDataSetChanged();
                    purchasedAdapter.notifyDataSetChanged();
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        listenToShoppingItems();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (shoppingListener != null) {
            shoppingListener.remove();
            shoppingListener = null;
        }
    }
}
