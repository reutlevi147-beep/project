package com.mycasa.app;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddGoalActivity extends AppCompatActivity {

    // ===== UI =====
    private EditText etTitle, etTarget, etCurrent, etDeadline;
    private Button btnSave;

    private CardView cardGoalSave, cardGoalLimit;

    // ===== State =====
    private String goalMode = "SAVE"; // SAVE | LIMIT
    private Calendar selectedDeadlineCalendar;

    // Firebase
    private FirebaseFirestore db;

    // אתחול מסך יצירת יעד חיסכון או תקציב
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_goal);

        db = FirebaseFirestore.getInstance();

        // ===== Bind Views =====
        etTitle    = findViewById(R.id.etGoalTitle);
        etTarget   = findViewById(R.id.etTargetAmount);
        etCurrent  = findViewById(R.id.etCurrentAmount);
        etDeadline = findViewById(R.id.etDeadline);

        btnSave = findViewById(R.id.btnSaveGoal);

        cardGoalSave  = findViewById(R.id.cardGoalSave);
        cardGoalLimit = findViewById(R.id.cardGoalLimit);

        // ===== Default selection =====
        selectGoalMode("SAVE");

        // ===== Click listeners =====
        cardGoalSave.setOnClickListener(v -> selectGoalMode("SAVE"));
        cardGoalLimit.setOnClickListener(v -> selectGoalMode("LIMIT"));

        etDeadline.setOnClickListener(v -> openDatePicker());

        btnSave.setOnClickListener(v -> saveGoalToFirebase());
    }

    // הגדרת מצב ברירת מחדל כמטרת חיסכון
    private void selectGoalMode(String mode) {
        goalMode = mode;

        if ("SAVE".equals(mode)) {
            cardGoalSave.setCardBackgroundColor(
                    getColor(R.color.green_light_bg)
            );
            cardGoalLimit.setCardBackgroundColor(
                    getColor(android.R.color.white)
            );
        } else {
            cardGoalLimit.setCardBackgroundColor(
                    getColor(R.color.blue_light_bg)
            );
            cardGoalSave.setCardBackgroundColor(
                    getColor(android.R.color.white)
            );
        }
    }

    // הצגת DatePicker לבחירת תאריך יעד
    private void openDatePicker() {

        Calendar cal = Calendar.getInstance();

        new DatePickerDialog(
                this,
                (view, year, month, day) -> {

                    selectedDeadlineCalendar = Calendar.getInstance();
                    selectedDeadlineCalendar.set(year, month, day, 0, 0, 0);

                    String formatted = new SimpleDateFormat(
                            "MMMM yyyy",
                            new Locale("he")
                    ).format(selectedDeadlineCalendar.getTime());

                    etDeadline.setText(formatted);
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }


    // בדיקת תקינות הנתונים ושמירת המטרה ב־Firestore
    private void saveGoalToFirebase() {

        String title = etTitle.getText().toString().trim();
        String targetStr = etTarget.getText().toString().trim();
        String currentStr = etCurrent.getText().toString().trim();
        Date deadlineDate = null;
        if (selectedDeadlineCalendar != null) {
            deadlineDate = selectedDeadlineCalendar.getTime();
        }

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(targetStr)) {
            Toast.makeText(this, "יש למלא שם וסכום יעד", Toast.LENGTH_SHORT).show();
            return;
        }

        int target = Integer.parseInt(targetStr);
        int current = TextUtils.isEmpty(currentStr) ? 0 : Integer.parseInt(currentStr);

        // ❗ ולידציות לפי סוג מטרה
        if ("SAVE".equals(goalMode) && current > target) {
            Toast.makeText(this,
                    "בחיסכון לא ניתן להתחיל מעל סכום היעד",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if ("LIMIT".equals(goalMode) && current > target) {
            Toast.makeText(this,
                    "חריגה מהתקציב כבר בשלב ההגדרה",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String groupId = AppSession.getGroupId();
        if (groupId == null) {
            Toast.makeText(this, "אין קבוצה פעילה", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("targetAmount", target);
        data.put("currentAmount", current);
        data.put("goalMode", goalMode); // SAVE | LIMIT
        data.put("deadline", deadlineDate);
        data.put("createdAt", FieldValue.serverTimestamp());
        data.put("lastProgressAlertDate", null);

        db.collection("groups")
                .document(groupId)
                .collection("savings_goals")
                .add(data)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "המטרה נשמרה 🎯", Toast.LENGTH_SHORT).show();
                    finish(); // חזרה לעמוד כלכלה
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "שגיאה בשמירה", Toast.LENGTH_LONG).show()
                );
    }
}
