package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.util.Log;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Add_Shopping extends AppCompatActivity {

    private static final String MAIN_COLLECTION = "shopping_lists";
    private static final String DOCUMENT_ID = "defaultList";
    private static final String ITEMS_SUB_COLLECTION = "items";

    private FirebaseFirestore db;

    // ✅ תוספת
    private Spinner quantitySpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_shopping);

        db = FirebaseFirestore.getInstance();

        EditText nameInput = findViewById(R.id.name);
        Button addToShoppingBtn = findViewById(R.id.addtoshopping);

        // ✅ חיבור Spinner כמות
        quantitySpinner = findViewById(R.id.quantitySpinner);

        // ✅ מילוי 1–99
        List<Integer> quantities = new ArrayList<>();
        for (int i = 1; i <= 99; i++) {
            quantities.add(i);
        }

        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                quantities
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        quantitySpinner.setAdapter(adapter);
        quantitySpinner.setSelection(0); // ברירת מחדל = 1

        // כפתור "הוספת מוצר"
        addToShoppingBtn.setOnClickListener(v -> {

            String itemName = nameInput.getText().toString().trim();

            if (itemName.isEmpty()) {
                Toast.makeText(this, "נא להכניס שם מוצר", Toast.LENGTH_SHORT).show();
                return;
            }

            int quantity = (int) quantitySpinner.getSelectedItem();

            addItemToFirestore(itemName, quantity);
        });

        // כפתור חזרה
        ImageButton returnToShoppingList = findViewById(R.id.Return);
        returnToShoppingList.setOnClickListener(v ->
                startActivity(new Intent(this, Shopping_list.class))
        );
    }

    // ✅ עודכן לקבל כמות
    private void addItemToFirestore(String name, int quantity) {

        db.collection(MAIN_COLLECTION)
                .document(DOCUMENT_ID)
                .collection(ITEMS_SUB_COLLECTION)
                .whereEqualTo("name", name)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    // אם המוצר כבר קיים → מעדכנים כמות
                    if (!querySnapshot.isEmpty()) {

                        var doc = querySnapshot.getDocuments().get(0);
                        Long currentQty = doc.getLong("quantity");
                        long newQty = (currentQty != null ? currentQty : 1) + quantity;

                        doc.getReference().update("quantity", newQty)
                                .addOnSuccessListener(v -> {
                                    Toast.makeText(this, "הכמות עודכנה", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, Shopping_list.class));
                                });

                    } else {
                        // אם המוצר לא קיים → יוצרים חדש
                        HashMap<String, Object> item = new HashMap<>();
                        item.put("name", name);
                        item.put("isPurchased", false);
                        item.put("quantity", quantity);
                        item.put("createdAt", FieldValue.serverTimestamp());

                        db.collection(MAIN_COLLECTION)
                                .document(DOCUMENT_ID)
                                .collection(ITEMS_SUB_COLLECTION)
                                .add(item)
                                .addOnSuccessListener(doc -> {
                                    Toast.makeText(this, "המוצר נוסף בהצלחה!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, Shopping_list.class));
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FIRESTORE_ERROR", e.getMessage());
                    Toast.makeText(this, "שגיאה בהוספה", Toast.LENGTH_SHORT).show();
                });
    }

}
