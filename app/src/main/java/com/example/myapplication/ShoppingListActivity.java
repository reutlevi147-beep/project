package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ShoppingListActivity extends AppCompatActivity {

    // 🛒 לקנות (מקובץ)
    private RecyclerView recyclerToBuy;
    private ShoppingListAdapter toBuyAdapter;
    private final List<ShoppingListRow> toBuyRows = new ArrayList<>();

    // ✅ נקנו
    private RecyclerView recyclerPurchased;
    private ShoppingListAdapter purchasedAdapter;
    private final List<ShoppingListRow> purchasedRows = new ArrayList<>();

    // 🔢 סיכום
    private TextView tvToBuyCount, tvPurchasedCount;
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

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        recyclerToBuy = findViewById(R.id.recyclerToBuy);
        recyclerPurchased = findViewById(R.id.recyclerPurchased);
        completedSection = findViewById(R.id.completedSection);
        btnClearPurchased = findViewById(R.id.btnClearPurchased);
        recyclerToBuy.setHasFixedSize(false);
        recyclerPurchased.setHasFixedSize(false);

        tvToBuyCount = findViewById(R.id.cardToBuy).findViewById(R.id.tvCount);
        tvPurchasedCount = findViewById(R.id.cardPurchased).findViewById(R.id.tvCount);

        recyclerToBuy.setLayoutManager(new LinearLayoutManager(this));
        recyclerPurchased.setLayoutManager(new LinearLayoutManager(this));

        toBuyAdapter = new ShoppingListAdapter(toBuyRows);
        purchasedAdapter = new ShoppingListAdapter(purchasedRows);

        recyclerToBuy.setAdapter(toBuyAdapter);
        recyclerPurchased.setAdapter(purchasedAdapter);

        disableChangeAnimations(recyclerToBuy);
        disableChangeAnimations(recyclerPurchased);

        attachAdapterListeners(toBuyAdapter);
        attachAdapterListeners(purchasedAdapter);

        btnClearPurchased.setOnClickListener(v -> {
            for (ShoppingListRow row : purchasedRows) {
                if (row.getType() == ShoppingListRow.TYPE_ITEM) {
                    ShoppingItem item = row.getItem();
                    db.collection(MAIN_COLLECTION)
                            .document(DOCUMENT_ID)
                            .collection(ITEMS_SUB_COLLECTION)
                            .document(item.getDocumentId())
                            .delete();
                }
            }
        });

        findViewById(R.id.fabAdd).setOnClickListener(v ->
                startActivity(new Intent(this, Add_Shopping.class))
        );
    }

    private void attachAdapterListeners(ShoppingListAdapter adapter) {

        adapter.setOnQuantityChangeListener((item, newQuantity) ->
                db.collection(MAIN_COLLECTION)
                        .document(DOCUMENT_ID)
                        .collection(ITEMS_SUB_COLLECTION)
                        .document(item.getDocumentId())
                        .update("quantity", newQuantity)
        );

        adapter.setOnItemCheckedChange((item, checked) -> {
            Map<String, Object> updates = new java.util.HashMap<>();
            updates.put("isPurchased", checked);
            updates.put("purchased", checked);

            db.collection(MAIN_COLLECTION)
                    .document(DOCUMENT_ID)
                    .collection(ITEMS_SUB_COLLECTION)
                    .document(item.getDocumentId())
                    .update(updates);
        });
    }

    private void disableChangeAnimations(RecyclerView rv) {
        RecyclerView.ItemAnimator animator = rv.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
    }

    // 🔄 Firestore Listener
    private void listenToShoppingItems() {

        shoppingListener = db
                .collection(MAIN_COLLECTION)
                .document(DOCUMENT_ID)
                .collection(ITEMS_SUB_COLLECTION)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {

                    if (e != null || snapshots == null) {
                        Log.e("SHOPPING", "Listen failed", e);
                        return;
                    }

                    toBuyRows.clear();
                    purchasedRows.clear();

                    Map<String, List<ShoppingItem>> grouped = new LinkedHashMap<>();

                    for (QueryDocumentSnapshot doc : snapshots) {
                        ShoppingItem item = doc.toObject(ShoppingItem.class);
                        if (item == null) continue;

                        item.setDocumentId(doc.getId());

                        Boolean p = doc.getBoolean("isPurchased");
                        if (p == null) p = doc.getBoolean("purchased");
                        item.setPurchased(p != null && p);

                        if (item.isPurchased()) {
                            purchasedRows.add(ShoppingListRow.item(item));
                        } else {
                            grouped
                                    .computeIfAbsent(item.getCategoryId(), k -> new ArrayList<>())
                                    .add(item);
                        }
                    }

                    int toBuyCount = 0;

                    for (Map.Entry<String, List<ShoppingItem>> entry : grouped.entrySet()) {
                        if (entry.getValue().isEmpty()) continue;

                        toBuyRows.add(
                                ShoppingListRow.header(getCategoryName(entry.getKey()))
                        );

                        for (ShoppingItem item : entry.getValue()) {
                            toBuyRows.add(ShoppingListRow.item(item));
                            toBuyCount++;
                        }
                    }

                    tvToBuyCount.setText(String.valueOf(toBuyCount));
                    tvPurchasedCount.setText(String.valueOf(purchasedRows.size()));
                    completedSection.setVisibility(
                            purchasedRows.isEmpty() ? View.GONE : View.VISIBLE
                    );

                    toBuyAdapter.notifyDataSetChanged();
                    purchasedAdapter.notifyDataSetChanged();
                });
    }

    private String getCategoryName(String id) {
        if (id == null) return "אחר";
        switch (id) {
            case "veg": return "ירקות ופירות";
            case "dairy": return "מוצרי חלב";
            case "meat": return "בשרים ועופות";
            case "dry": return "יבשים";
            case "cleaning": return "ניקיון והיגיינה";
            default: return "אחר";
        }
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
