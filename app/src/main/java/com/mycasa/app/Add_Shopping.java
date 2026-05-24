package com.mycasa.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Add_Shopping extends AppCompatActivity {

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_GROUP_ID = "group_id";

    private EditText etItemName, etNotes;
    private Switch switchUrgent;

    // ✅ כמות עם + / -
    private int quantity = 1;
    private TextView tvQuantity;
    private ImageButton btnPlus, btnMinus;

    // קטגוריות
    private RecyclerView recyclerCategories;
    private CategoriesAdapter categoriesAdapter;
    private final ArrayList<ShoppingCategory> categories = new ArrayList<>();
    private String selectedCategoryId = null;

    // הצעות
    private RecyclerView recyclerSuggestions;
    private SuggestedItemsAdapter suggestedItemsAdapter;
    private TextView tvSuggestedTitle;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_shopping);

        db = FirebaseFirestore.getInstance();

        // חזרה
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Views
        etItemName = findViewById(R.id.etItemName);
        etNotes = findViewById(R.id.etNotes);
        switchUrgent = findViewById(R.id.switchUrgent);
        tvSuggestedTitle = findViewById(R.id.tvSuggestedTitle);

        // כמות
        tvQuantity = findViewById(R.id.tvQuantity);
        btnPlus = findViewById(R.id.btnPlus);
        btnMinus = findViewById(R.id.btnMinus);

        tvQuantity.setText(String.valueOf(quantity));

        btnPlus.setOnClickListener(v -> {
            if (quantity < 100) {
                quantity++;
                tvQuantity.setText(String.valueOf(quantity));
            }
        });

        btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
            }
        });

        Button btnCancel = findViewById(R.id.btnCancel);

        btnCancel.setOnClickListener(v -> {
            finish();
        });

        ImageButton btnConfirmName = findViewById(R.id.btnConfirmName);
        Button btnSave = findViewById(R.id.btnSave);

        // קטגוריות
        recyclerCategories = findViewById(R.id.recyclerCategories);
        recyclerCategories.setLayoutManager(new GridLayoutManager(this, 2));

        categories.add(new ShoppingCategory("veg", "ירקות ופירות", "🥬"));
        categories.add(new ShoppingCategory("dairy", "מוצרי חלב", "🥛"));
        categories.add(new ShoppingCategory("meat", "בשרים עופות ודגים", "🍖"));
        categories.add(new ShoppingCategory("bakery", "מאפים ולחם", "🥖"));
        categories.add(new ShoppingCategory("dry", "יבשים ומזווה", "🌾"));
        categories.add(new ShoppingCategory("snacks", "מתוקים וחטיפים", "🍫"));
        categories.add(new ShoppingCategory("frozen", "קפואים", "🧊"));
        categories.add(new ShoppingCategory("drinks", "שתייה", "🥤"));
        categories.add(new ShoppingCategory("cleaning", "ניקיון והיגיינה", "🧼"));
        categories.add(new ShoppingCategory("other", "אחר", "🛒"));

        categoriesAdapter = new CategoriesAdapter(
                categories,
                category -> selectedCategoryId = category.getId()
        );
        recyclerCategories.setAdapter(categoriesAdapter);

        // הצעות
        recyclerSuggestions = findViewById(R.id.recyclerSuggestions);
        recyclerSuggestions.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerSuggestions.setVisibility(View.GONE);
        tvSuggestedTitle.setVisibility(View.GONE);
        loadSuggestedItems();

        // אישור שם מוצר
        btnConfirmName.setOnClickListener(v -> {
            String name = etItemName.getText().toString().trim();
            if (name.isEmpty()) return;

            etItemName.clearFocus();

            InputMethodManager imm =
                    (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }

            detectCategorySmart(name);
        });

        btnSave.setOnClickListener(v -> saveItem());
    }

    private String getGroupId() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(KEY_GROUP_ID, null);
    }

    // ================= SAVE =================

    private void saveItem() {
        String groupId = getGroupId();
        if (groupId == null) {
            Toast.makeText(this, "❌ לא נמצא קוד קבוצה", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = etItemName.getText().toString().trim();
        if (name.isEmpty()) {
            etItemName.setError("יש להזין שם מוצר");
            return;
        }

        final int finalQty = quantity;

        db.collection("groups")
                .document(groupId)
                .collection("shopping")
                .whereEqualTo("name", name)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot.isEmpty()) {
                        addNewItem(groupId, name, finalQty);
                    } else {
                        DocumentSnapshot doc = snapshot.getDocuments().get(0);
                        Long q = doc.getLong("quantity");
                        int existingQty = q != null ? q.intValue() : 0;

                        showDuplicateDialog(
                                groupId,
                                doc.getId(),
                                existingQty,
                                finalQty,
                                name
                        );
                    }
                });
    }

    private void addNewItem(String groupId, String name, int quantity) {
        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("quantity", quantity);
        item.put("isPurchased", false);
        item.put("categoryId", selectedCategoryId != null ? selectedCategoryId : "other");
        item.put("urgent", switchUrgent.isChecked());
        item.put("notes", etNotes.getText().toString().trim());
        item.put("createdAt", FieldValue.serverTimestamp());

        db.collection("groups")
                .document(groupId)
                .collection("shopping")
                .add(item)
                .addOnSuccessListener(d -> finish());
    }

    private void showDuplicateDialog(
            String groupId,
            String docId,
            int existingQty,
            int addedQty,
            String name
    ) {
        new AlertDialog.Builder(this)
                .setTitle("המוצר כבר קיים")
                .setMessage("להוסיף את הכמות למוצר הקיים?")
                .setPositiveButton("כן", (d, w) -> {
                    int newQty = existingQty + addedQty;
                    db.collection("groups")
                            .document(groupId)
                            .collection("shopping")
                            .document(docId)
                            .update("quantity", newQty)
                            .addOnSuccessListener(v -> finish());
                })
                .setNegativeButton("לא", null)
                .show();
    }

    private void detectCategorySmart(String name) {
        String groupId = getGroupId();
        if (groupId == null) return;

        String docId = name.toLowerCase().trim();

        db.collection("groups")
                .document(groupId)
                .collection("shopping_stats")
                .document(docId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (doc.exists()) {
                        String cat = doc.getString("categoryId");
                        if (cat != null) {
                            selectedCategoryId = cat;
                            categoriesAdapter.setSelectedCategory(cat);
                            return;
                        }
                    }

                    String local = detectCategoryLocal(name);
                    if (local != null) {
                        selectedCategoryId = local;
                        categoriesAdapter.setSelectedCategory(local);
                    }
                });
    }

    private void loadSuggestedItems() {
        String groupId = getGroupId();
        if (groupId == null) return;

        Date sixtyDaysAgo = new Date(
                System.currentTimeMillis() - 60L * 24 * 60 * 60 * 1000
        );

        db.collection("groups")
                .document(groupId)
                .collection("shopping_stats")
                .whereGreaterThanOrEqualTo("count", 3)
                .whereGreaterThanOrEqualTo("lastAddedAt", sixtyDaysAgo)
                .orderBy("lastAddedAt", Query.Direction.DESCENDING)
                .limit(8)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        showSuggestions(snapshot.getDocuments());
                    }
                });
    }

    private void showSuggestions(List<DocumentSnapshot> suggestions) {

        if (suggestions == null || suggestions.isEmpty()) {
            tvSuggestedTitle.setVisibility(View.GONE);
            recyclerSuggestions.setVisibility(View.GONE);
            return;
        }

        tvSuggestedTitle.setVisibility(View.VISIBLE);
        recyclerSuggestions.setVisibility(View.VISIBLE);

        suggestedItemsAdapter = new SuggestedItemsAdapter(
                suggestions,
                item -> {
                    etItemName.setText(item.getString("name"));

                    String cat = item.getString("categoryId");
                    if (cat != null) {
                        selectedCategoryId = cat;
                        categoriesAdapter.setSelectedCategory(cat);
                    }

                    quantity = 1;
                    tvQuantity.setText("1");
                }
        );

        recyclerSuggestions.setAdapter(suggestedItemsAdapter);
    }

    private String detectCategoryLocal(String name) {
        name = name.toLowerCase();

        if (name.contains("חלב") || name.contains("גבינה") || name.contains("יוגורט"))
            return "dairy";
        if (name.contains("לחם") || name.contains("פיתה") || name.contains("באגט"))
            return "bakery";
        if (name.contains("עגב") || name.contains("מלפפון") || name.contains("פלפל"))
            return "veg";
        if (name.contains("בננה") || name.contains("תפוח") || name.contains("תפוז"))
            return "veg";
        if (name.contains("עוף") || name.contains("בשר") || name.contains("דג"))
            return "meat";
        if (name.contains("שוקולד") || name.contains("ביסלי") || name.contains("חטיף"))
            return "snacks";
        if (name.contains("קולה") || name.contains("מים") || name.contains("מיץ"))
            return "drinks";

        return null;
    }


}
