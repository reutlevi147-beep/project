package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class Add_Shopping extends AppCompatActivity {

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_shopping);

        db = FirebaseFirestore.getInstance();  // Firestore

        EditText nameInput = findViewById(R.id.name);
        Button addToShoppingBtn = findViewById(R.id.addtoshopping);

        // כאשר לוחצים על "הוספת מוצר"
        addToShoppingBtn.setOnClickListener(v -> {

            String itemName = nameInput.getText().toString().trim();

            if (itemName.isEmpty()) {
                Toast.makeText(this, "נא להכניס שם מוצר", Toast.LENGTH_SHORT).show();
                return;
            }

            addItemToFirestore(itemName);
        });

        // כפתור חזרה
        ImageButton returnToShoppingList = findViewById(R.id.Return);
        returnToShoppingList.setOnClickListener(v ->
                startActivity(new Intent(this, Shopping_list.class))
        );
    }

    private void addItemToFirestore(String name) {

        var item = new java.util.HashMap<String, Object>();
        item.put("name", name);
        item.put("checked", false);
        item.put("createdAt", FieldValue.serverTimestamp());

        db.collection("shopping_list")
                .add(item)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "המוצר נוסף!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, Shopping_list.class));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "שגיאה בהוספה", Toast.LENGTH_SHORT).show();
                });
    }
}
