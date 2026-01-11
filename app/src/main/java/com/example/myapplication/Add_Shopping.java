package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Add_Shopping extends AppCompatActivity {

    // ===== Fields =====
    private EditText etItemName, etQuantity, etNotes;
    private Switch switchUrgent;

    // ===== Categories =====
    private RecyclerView recyclerCategories;
    private CategoriesAdapter categoriesAdapter;
    private final ArrayList<ShoppingCategory> categories = new ArrayList<>();
    private String selectedCategoryId = null;

    // ===== Firestore =====
    private FirebaseFirestore db;

    private static final String MAIN_COLLECTION = "shopping_lists";
    private static final String DOCUMENT_ID = "defaultList";
    private static final String ITEMS_SUB_COLLECTION = "items";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_shopping);

        db = FirebaseFirestore.getInstance();

        // 🔙 Back
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // 📝 Fields
        etItemName = findViewById(R.id.etItemName);
        etQuantity = findViewById(R.id.etQuantity);
        etNotes = findViewById(R.id.etNotes);
        switchUrgent = findViewById(R.id.switchUrgent);

        // 🧩 Categories (Grid)
        recyclerCategories = findViewById(R.id.recyclerCategories);
        recyclerCategories.setLayoutManager(new GridLayoutManager(this, 2));

        categories.add(new ShoppingCategory("veg", "ירקות ופירות", "🥬"));
        categories.add(new ShoppingCategory("dairy", "מוצרי חלב", "🥛"));
        categories.add(new ShoppingCategory("meat", "בשרים ועופות", "🍖"));
        categories.add(new ShoppingCategory("dry", "יבשים", "🌾"));
        categories.add(new ShoppingCategory("cleaning", "ניקיון והיגיינה", "🧼"));
        categories.add(new ShoppingCategory("other", "אחר", "🛒"));

        categoriesAdapter = new CategoriesAdapter(categories, category ->
                selectedCategoryId = category.getId()
        );
        recyclerCategories.setAdapter(categoriesAdapter);

        // 🔘 Buttons
        Button btnCancel = findViewById(R.id.btnCancel);
        Button btnSave = findViewById(R.id.btnSave);

        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveItem());
    }

    // =====================
    // 💾 Save to Firestore
    // =====================
    private void saveItem() {

        String name = etItemName.getText().toString().trim();
        String qtyStr = etQuantity.getText().toString().trim();

        if (name.isEmpty()) {
            etItemName.setError("חובה להזין שם מוצר");
            return;
        }

        int quantity;
        if (qtyStr.isEmpty()) {
            quantity = 1; // ברירת מחדל
        } else {
            try {
                quantity = Integer.parseInt(qtyStr);
            } catch (NumberFormatException e) {
                etQuantity.setError("כמות לא תקינה");
                return;
            }
        }

        if (quantity <= 0) {
            etQuantity.setError("הכמות חייבת להיות לפחות 1");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("shopping_lists")
                .document("defaultList")
                .collection("items")
                .whereEqualTo("name", name)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot.isEmpty()) {
                        addNewItem(db, name, quantity);
                    } else {
                        DocumentSnapshot doc = snapshot.getDocuments().get(0);

                        Long qtyLong = doc.getLong("quantity");
                        int existingQty = qtyLong != null ? qtyLong.intValue() : 0;

                        showDuplicateDialog(db, doc.getId(), existingQty, quantity);
                    }
                });
    }


    private void showDuplicateDialog(FirebaseFirestore db,
                                     String docId,
                                     int existingQty,
                                     int addedQty) {

        new AlertDialog.Builder(this)
                .setTitle("המוצר כבר קיים")
                .setMessage("להוסיף את הכמות למוצר הקיים?")
                .setPositiveButton("כן", (dialog, which) -> {

                    int newQty = existingQty + addedQty;

                    db.collection("shopping_lists")
                            .document("defaultList")
                            .collection("items")
                            .document(docId)
                            .update("quantity", newQty);

                    Toast.makeText(this, "הכמות עודכנה ✔️", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("לא", null)
                .show();
    }



    private void addNewItem(FirebaseFirestore db, String name, int quantity) {

        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("quantity", quantity);
        item.put("isPurchased", false);
        item.put("createdAt", FieldValue.serverTimestamp());


        item.put("categoryId", selectedCategoryId);

        db.collection("shopping_lists")
                .document("defaultList")
                .collection("items")
                .add(item)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "המוצר נוסף ✔️", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }







}
