package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class Add_Shopping extends AppCompatActivity {

    // שם האוסף הראשי והמסמך הקבוע
    private static final String MAIN_COLLECTION = "shopping_lists";
    private static final String DOCUMENT_ID = "defaultList";
    private static final String ITEMS_SUB_COLLECTION = "items";

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_shopping);

        db = FirebaseFirestore.getInstance();  // Firestore

        EditText nameInput = findViewById(R.id.name);
        Button addToShoppingBtn = findViewById(R.id.addtoshopping);

        // כפתור "הוספת מוצר"
        addToShoppingBtn.setOnClickListener(v -> {

            String itemName = nameInput.getText().toString().trim();

            if (itemName.isEmpty()) {
                Toast.makeText(this, "נא להכניס שם מוצר", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, itemName, Toast.LENGTH_SHORT).show();
            addItemToFirestore(itemName);
        });

        // כפתור חזרה
        ImageButton returnToShoppingList = findViewById(R.id.Return);
        returnToShoppingList.setOnClickListener(v -> {
            startActivity(new Intent(this, Shopping_list.class));
        });
    }

    private void addItemToFirestore(String name) {

        var item = new java.util.HashMap<String, Object>();
        item.put("name", name);
        item.put("isPurchased", false);   // ←←← התיקון הקריטי !!!!
        item.put("quantity", 1);          // ערך ברירת מחדל
        item.put("createdAt", FieldValue.serverTimestamp());

        // שמירה בנתיב: shopping_lists/defaultList/items
        db.collection(MAIN_COLLECTION)
                .document(DOCUMENT_ID)
                .collection(ITEMS_SUB_COLLECTION)
                .add(item)
                .addOnSuccessListener(doc -> {

                    Toast.makeText(this, "המוצר נוסף בהצלחה!", Toast.LENGTH_SHORT).show();

                    // ניקוי הקלט
                    EditText nameInput = findViewById(R.id.name);
                    nameInput.setText("");

                    // מעבר לפעילות הרשימה
                    startActivity(new Intent(this, Shopping_list.class));
                })
                .addOnFailureListener(e -> {
                    Log.e("FIRESTORE_ERROR", "Error adding item: " + e.getMessage());
                    Toast.makeText(this, "שגיאה בהוספה: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
