package com.mycasa.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import eightbitlab.com.blurview.BlurView;

public class ShoppingListActivity extends BaseActivity {
    private static final String TAG = "SHOPPING";
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_GROUP_ID = "group_id";

    private RecyclerView recycler;
    private ShoppingRowsAdapter adapter;

    private final List<ShoppingListRow> rows = new ArrayList<>();
    private final List<ShoppingItem> allItems = new ArrayList<>();
    private View lockOverlay;
    // 🔢 COUNTS
    private TextView tvActiveCount;
    private TextView tvPurchasedCount;
    private BaseActivity.PagePermission currentPermission = PagePermission.VIEW_ONLY;
    private FirebaseFirestore db;
    private ListenerRegistration shoppingListener;
    private static final String KEY_USER_ID = "user_id";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shopping);

        db = FirebaseFirestore.getInstance();

        // ===== USER + GROUP =====
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String groupId = prefs.getString(KEY_GROUP_ID, null);
        String userId = prefs.getString(KEY_USER_ID, null);

        // ===== UI =====
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        recycler = findViewById(R.id.recyclerShopping);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ShoppingRowsAdapter(rows);
        recycler.setAdapter(adapter);

        adapter.setOnQuantityChangeListener(this::updateQuantity);
        adapter.setOnItemCheckedChange(this::updatePurchased);
        adapter.setOnClearPurchasedClick(this::clearPurchasedItems);

        // ===== COUNTERS =====
        View statActive = findViewById(R.id.statActive);
        tvActiveCount = statActive.findViewById(R.id.tvValue);
        ((TextView) statActive.findViewById(R.id.tvLabel)).setText("לקניה");
        lockOverlay = findViewById(R.id.lockOverlay);
        View statPurchased = findViewById(R.id.statCompleted);
        tvPurchasedCount = statPurchased.findViewById(R.id.tvValue);
        ((TextView) statPurchased.findViewById(R.id.tvLabel)).setText("נקנו");
        FloatingActionButton fab = findViewById(R.id.fabAdd);

        // ===== FAB CLICK =====
        fab.setOnClickListener(v ->
                startActivity(new Intent(this, Add_Shopping.class))
        );
        BlurView blurView = findViewById(R.id.lockOverlay);

        View decorView = getWindow().getDecorView();
        ViewGroup rootView = (ViewGroup) decorView.findViewById(android.R.id.content);

        blurView.setupWith(rootView)
                .setFrameClearDrawable(getWindow().getDecorView().getBackground())
                .setBlurRadius(25f);

        resolvePermissionFromServer(
                AppPage.SHOPPING,
                groupId,
                userId,
                permission -> {

                    currentPermission = permission;

                    if (permission == PagePermission.LOCKED || permission == null) {

                        lockOverlay.setVisibility(View.VISIBLE);

                        fab.setVisibility(View.GONE);

                        recycler.setEnabled(false);

                        return;
                    }

                    lockOverlay.setVisibility(View.GONE);

                    switch (permission) {

                        case VIEW_ONLY:

                            fab.setVisibility(View.GONE);

                            break;

                        case ADD_ONLY:

                            fab.setVisibility(View.VISIBLE);

                            break;

                        case ADD_EDIT:

                            fab.setVisibility(View.VISIBLE);

                            break;

                        case FULL_ACCESS:

                            fab.setVisibility(View.VISIBLE);

                            break;
                    }
                }
        );

        // ===== DATA LISTENER =====
        startShoppingListener();
    }
    // =========================
    // groupId
    // =========================
    private String getGroupId() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(KEY_GROUP_ID, null);
    }

    // =========================
    // Firestore Listener
    // =========================
    private void startShoppingListener() {

        String groupId = getGroupId();
        if (groupId == null) return;

        shoppingListener =
                db.collection("groups")
                        .document(groupId)
                        .collection("shopping")
                        .addSnapshotListener((snapshots, e) -> {

                            if (e != null || snapshots == null) {
                                Log.e(TAG, "Snapshot error", e);
                                return;
                            }

                            allItems.clear();

                            for (QueryDocumentSnapshot doc : snapshots) {
                                ShoppingItem item = new ShoppingItem();
                                item.setDocumentId(doc.getId());
                                item.setName(doc.getString("name"));

                                Long q = doc.getLong("quantity");
                                item.setQuantity(q == null ? 1 : q.intValue());

                                Boolean purchased = doc.getBoolean("isPurchased");
                                item.setPurchased(purchased != null && purchased);

                                item.setCategoryId(doc.getString("categoryId"));

                                allItems.add(item);
                            }

                            rebuildRows();
                        });
    }

    // =========================
    // Build rows + COUNTS ✅
    // =========================
    private void rebuildRows() {

        rows.clear();

        int activeCount = 0;
        int purchasedCount = 0;

        rows.add(ShoppingListRow.header("לקנות"));

        Map<String, List<ShoppingItem>> byCategory = new LinkedHashMap<>();

        for (ShoppingItem item : allItems) {
            if (item.isPurchased()) {
                purchasedCount++;
                continue;
            }

            activeCount++;

            String cat = item.getCategoryId() == null ? "other" : item.getCategoryId();
            byCategory.computeIfAbsent(cat, k -> new ArrayList<>()).add(item);
        }

        for (String cat : byCategory.keySet()) {
            rows.add(ShoppingListRow.header(getCategoryTitle(cat)));
            for (ShoppingItem item : byCategory.get(cat)) {
                rows.add(ShoppingListRow.item(item));
            }
        }

        List<ShoppingItem> purchased = new ArrayList<>();
        for (ShoppingItem item : allItems) {
            if (item.isPurchased()) purchased.add(item);
        }

        if (!purchased.isEmpty()) {
            rows.add(ShoppingListRow.clearPurchased());
            for (ShoppingItem item : purchased) {
                rows.add(ShoppingListRow.item(item));
            }
        }

        // 🔢 UPDATE COUNTS
        tvActiveCount.setText(String.valueOf(activeCount));
        tvPurchasedCount.setText(String.valueOf(purchasedCount));

        adapter.notifyDataSetChanged();
    }

    // =========================
    // Updates
    // =========================
    private void updateQuantity(ShoppingItem item, int q) {
        if(currentPermission == PagePermission.VIEW_ONLY) return;
        String groupId = getGroupId();
        if (groupId == null || item.getDocumentId() == null) return;

        db.collection("groups")
                .document(groupId)
                .collection("shopping")
                .document(item.getDocumentId())
                .set(Map.of("quantity", q), SetOptions.merge());
    }

    private void updatePurchased(ShoppingItem item, boolean checked) {
        if(currentPermission == PagePermission.VIEW_ONLY) return;
        String groupId = getGroupId();
        if (groupId == null || item.getDocumentId() == null) return;

        db.collection("groups")
                .document(groupId)
                .collection("shopping")
                .document(item.getDocumentId())
                .set(Map.of("isPurchased", checked), SetOptions.merge());

        if (checked) {
            incrementShoppingStat(item.getName());
        }
    }

    // =========================
    // Shopping stats
    // =========================
    private void incrementShoppingStat(String name) {

        String groupId = getGroupId();
        if (groupId == null || name == null) return;

        DocumentReference ref =
                db.collection("groups")
                        .document(groupId)
                        .collection("shopping_stats")
                        .document(name);

        db.runTransaction(tx -> {
            DocumentSnapshot doc = tx.get(ref);

            if (doc.exists()) {
                Long count = doc.getLong("count");
                tx.update(ref,
                        "count", count == null ? 1 : count + 1,
                        "lastBoughtAt", FieldValue.serverTimestamp()
                );
            } else {
                Map<String, Object> data = new HashMap<>();
                data.put("name", name);
                data.put("count", 1);
                data.put("lastBoughtAt", FieldValue.serverTimestamp());
                tx.set(ref, data);
            }
            return null;
        });
    }

    private void clearPurchasedItems() {
        if(currentPermission != PagePermission.FULL_ACCESS) return;
        String groupId = getGroupId();
        if (groupId == null) return;

        WriteBatch batch = db.batch();

        for (ShoppingItem item : allItems) {
            if (!item.isPurchased() || item.getDocumentId() == null) continue;

            DocumentReference ref =
                    db.collection("groups")
                            .document(groupId)
                            .collection("shopping")
                            .document(item.getDocumentId());

            batch.delete(ref);
        }

        batch.commit();
    }

    private String getCategoryTitle(String id) {
        if (id == null) return "אחר";

        switch (id) {
            case "veg": return "ירקות ופירות";
            case "dairy": return "מוצרי חלב";
            case "meat": return "בשרים ועופות";
            case "dry": return "יבשים";
            case "bakery": return "מאפים ולחמים";
            case "frozen": return "קפואים";
            case "drinks": return "שתייה";
            case "cleaning": return "ניקיון והיגיינה";
            case "snacks": return "מתוקים וחטיפים";
            default: return "אחר";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (shoppingListener != null) {
            shoppingListener.remove();
        }
    }
}
