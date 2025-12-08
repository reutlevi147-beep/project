package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Add_Users extends AppCompatActivity {

    private EditText userNameInput, phoneInput;
    private FirebaseFirestore db;

    private static final String USERS_COLLECTION = "users";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_users);

        // Firebase
        db = FirebaseFirestore.getInstance();

        // חיבור לשדות מה-XML
        userNameInput = findViewById(R.id.user_name);
        phoneInput = findViewById(R.id.phone_number);

        // כפתור "הוספת משתמש"
        Button addUser = findViewById(R.id.add_user);
        addUser.setOnClickListener(v -> saveUser());

        // כפתור "חזרה" – חוזר למסך הגדרות
        ImageButton returnBtn = findViewById(R.id.Return);
        returnBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Add_Users.this, Settings.class);
            startActivity(intent);
            finish();
        });
    }

    private void saveUser() {

        String name = userNameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();

        // בדיקות בסיסיות
        if (name.isEmpty()) {
            Toast.makeText(this, "נא להזין שם משתמש", Toast.LENGTH_SHORT).show();
            return;
        }

        if (phone.isEmpty()) {
            Toast.makeText(this, "נא להזין מספר טלפון", Toast.LENGTH_SHORT).show();
            return;
        }

        // יצירת map לאחסון ב-Firestore
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("phoneNumber", phone);
        user.put("createdAt", Timestamp.now());

        // שמירה ב-Firestore (לא מחכים לתשובה)
        db.collection(USERS_COLLECTION).add(user);

        Toast.makeText(this, "המשתמש נשמר!", Toast.LENGTH_SHORT).show();

        // מיד חוזרים למסך הגדרות
        Intent intent = new Intent(Add_Users.this, Settings.class);
        startActivity(intent);
        finish();
    }
}
