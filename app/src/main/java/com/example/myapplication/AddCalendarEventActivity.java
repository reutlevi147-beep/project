package com.example.myapplication;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class AddCalendarEventActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private MaterialButton btnSave;

    private EditText etEventTitle, etEventDate, etEndDate, etStartTime, etEndTime;

    private FrameLayout frameIndigo, frameTeal, frameAmber,
            frameRose, frameSlate, frameEmerald;

    private String selectedColor = "indigo";
    private String editingEventId = null;

    private final Calendar startCalendar = Calendar.getInstance();
    private final Calendar endCalendar = Calendar.getInstance();

    private RecyclerView rvAssignUsers;
    private AssignUsersAdapter assignUsersAdapter;

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_GROUP_ID = "group_id";

    private MaterialAutoCompleteTextView spReminder;
    private String selectedRepeatType = "once";

    private final List<String> selectedUserIds = new ArrayList<>();
    private List<String> pendingSelectedUserIds = null;

    // ================= Lifecycle =================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_calendar_event);

        editingEventId = getIntent().getStringExtra("eventId");

        btnBack = findViewById(R.id.btnBack);
        btnSave = findViewById(R.id.btnSave);

        etEventTitle = findViewById(R.id.etEventTitle);
        etEventDate = findViewById(R.id.etEventDate);
        etEndDate = findViewById(R.id.etEndDate);
        etStartTime = findViewById(R.id.etStartTime);
        etEndTime = findViewById(R.id.etEndTime);

        etEventDate.setOnClickListener(v -> showStartDatePicker());
        etEndDate.setOnClickListener(v -> showEndDatePicker());
        etStartTime.setOnClickListener(v -> showStartTimePicker());
        etEndTime.setOnClickListener(v -> showEndTimePicker());

        // 👥 Users
        rvAssignUsers = findViewById(R.id.rvAssignUsers);
        assignUsersAdapter = new AssignUsersAdapter();
        rvAssignUsers.setLayoutManager(new LinearLayoutManager(this));
        rvAssignUsers.setAdapter(assignUsersAdapter);

        loadUsersFromFirebase();

        // 🎨 Colors
        frameIndigo = findViewById(R.id.frameIndigo);
        frameTeal = findViewById(R.id.frameTeal);
        frameAmber = findViewById(R.id.frameAmber);
        frameRose = findViewById(R.id.frameRose);
        frameSlate = findViewById(R.id.frameSlate);
        frameEmerald = findViewById(R.id.frameEmerald);

        selectColor(frameIndigo, "indigo");

        frameIndigo.setOnClickListener(v -> selectColor(frameIndigo, "indigo"));
        frameTeal.setOnClickListener(v -> selectColor(frameTeal, "teal"));
        frameAmber.setOnClickListener(v -> selectColor(frameAmber, "amber"));
        frameRose.setOnClickListener(v -> selectColor(frameRose, "rose"));
        frameSlate.setOnClickListener(v -> selectColor(frameSlate, "slate"));
        frameEmerald.setOnClickListener(v -> selectColor(frameEmerald, "emerald"));

        // 🔔 Reminder
        spReminder = findViewById(R.id.spReminder);

        String[] reminderOptions = {
                "ללא תזכורת",
                "5 דקות לפני",
                "10 דקות לפני",
                "30 דקות לפני",
                "שעה לפני",
                "יום לפני"
        };

        spReminder.setAdapter(
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, reminderOptions)
        );
        spReminder.setText(reminderOptions[0], false);

        // 🔁 Repeat
        MaterialButtonToggleGroup toggleRepeatType =
                findViewById(R.id.toggleRepeatType);

        toggleRepeatType.check(R.id.btnRepeatOnce);

        toggleRepeatType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;

            if (checkedId == R.id.btnRepeatWeekly) {
                selectedRepeatType = "weekly";
            } else if (checkedId == R.id.btnRepeatMonthly) {
                selectedRepeatType = "monthly";
            } else if (checkedId == R.id.btnRepeatYearly) {
                selectedRepeatType = "yearly";
            } else {
                selectedRepeatType = "once";
            }
        });

        // ⬅️ / 💾
        btnBack.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            selectedUserIds.clear();
            selectedUserIds.addAll(assignUsersAdapter.getSelectedUserIds());

            if (validateForm()) {
                saveEventToFirestore();
            }
        });

        if (editingEventId != null) {
            loadEventForEdit();
        }
    }

    // ================= Firestore =================

    private void loadUsersFromFirebase() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String groupId = prefs.getString(KEY_GROUP_ID, null);
        if (groupId == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .whereEqualTo("groupId", groupId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    assignUsersAdapter.setUsers(snapshot.getDocuments());

                    if (pendingSelectedUserIds != null) {
                        assignUsersAdapter.setSelectedUserIds(pendingSelectedUserIds);
                    }
                });
    }

    private void saveEventToFirestore() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String groupId = prefs.getString(KEY_GROUP_ID, null);

        if (groupId == null) {
            Toast.makeText(this, "לא נמצא groupId", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isAllUsers = selectedUserIds.isEmpty();
        String assignedToLabel = buildAssignedToLabel(isAllUsers);

        HashMap<String, Object> data = new HashMap<>();
        data.put("groupId", groupId);
        data.put("title", etEventTitle.getText().toString().trim());
        data.put("date", etEventDate.getText().toString().trim());
        data.put("endDate", etEndDate.getText().toString().trim());
        data.put("startTime", etStartTime.getText().toString().trim());
        data.put("endTime", etEndTime.getText().toString().trim());
        data.put("color", selectedColor);
        data.put("reminderMinutes", getReminderMinutes());
        data.put("assignedUserIds", selectedUserIds);
        data.put("assignedToLabel", assignedToLabel);
        data.put("isAllUsers", isAllUsers);
        data.put("repeatType", selectedRepeatType);

        FirebaseFirestore.getInstance()
                .collection("calendar_events")
                .add(data)
                .addOnSuccessListener(v -> finish());
    }

    // ================= Helpers =================

    private String buildAssignedToLabel(boolean isAllUsers) {
        if (isAllUsers) return "לכולם";

        List<String> names = new ArrayList<>();
        List<String> selectedIds = assignUsersAdapter.getSelectedUserIds();
        List<DocumentSnapshot> allUsers = assignUsersAdapter.getUsers();

        for (DocumentSnapshot user : allUsers) {
            if (selectedIds.contains(user.getId())) {
                String name = user.getString("name");
                if (name != null) names.add(name);
            }
        }

        if (names.isEmpty()) return "לכולם";
        if (names.size() == 1) return names.get(0);
        if (names.size() == 2) return names.get(0) + " ו" + names.get(1);

        return names.get(0) + ", " + names.get(1) + " ועוד " + (names.size() - 2);
    }

    private boolean validateForm() {
        if (TextUtils.isEmpty(etEventTitle.getText())) {
            etEventTitle.setError("יש להזין כותרת");
            return false;
        }
        if (TextUtils.isEmpty(etEventDate.getText())) {
            etEventDate.setError("יש לבחור תאריך");
            return false;
        }
        if (TextUtils.isEmpty(etStartTime.getText())) {
            etStartTime.setError("יש לבחור שעה");
            return false;
        }
        return true;
    }

    private int getReminderMinutes() {
        switch (spReminder.getText().toString()) {
            case "5 דקות לפני": return 5;
            case "10 דקות לפני": return 10;
            case "30 דקות לפני": return 30;
            case "שעה לפני": return 60;
            case "יום לפני": return 1440;
            default: return 0;
        }
    }

    // ================= Pickers =================

    private void showStartDatePicker() {
        new DatePickerDialog(this, (v, y, m, d) -> {
            startCalendar.set(y, m, d);
            endCalendar.set(y, m, d);
            etEventDate.setText(String.format("%02d/%02d/%04d", d, m + 1, y));
            etEndDate.setText(String.format("%02d/%02d/%04d", d, m + 1, y));
        }, startCalendar.get(Calendar.YEAR),
                startCalendar.get(Calendar.MONTH),
                startCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showEndDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (v, y, m, d) ->
                        etEndDate.setText(String.format("%02d/%02d/%04d", d, m + 1, y)),
                endCalendar.get(Calendar.YEAR),
                endCalendar.get(Calendar.MONTH),
                endCalendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.getDatePicker().setMinDate(startCalendar.getTimeInMillis());
        dialog.show();
    }

    private void showStartTimePicker() {
        new TimePickerDialog(this, (v, h, m) ->
                etStartTime.setText(String.format("%02d:%02d", h, m)),
                startCalendar.get(Calendar.HOUR_OF_DAY),
                startCalendar.get(Calendar.MINUTE),
                true).show();
    }

    private void showEndTimePicker() {
        new TimePickerDialog(this, (v, h, m) ->
                etEndTime.setText(String.format("%02d:%02d", h, m)),
                endCalendar.get(Calendar.HOUR_OF_DAY),
                endCalendar.get(Calendar.MINUTE),
                true).show();
    }

    private void selectColor(FrameLayout selectedFrame, String colorId) {
        selectedColor = colorId;
        frameIndigo.setBackgroundResource(R.drawable.bg_color_unselected);
        frameTeal.setBackgroundResource(R.drawable.bg_color_unselected);
        frameAmber.setBackgroundResource(R.drawable.bg_color_unselected);
        frameRose.setBackgroundResource(R.drawable.bg_color_unselected);
        frameSlate.setBackgroundResource(R.drawable.bg_color_unselected);
        frameEmerald.setBackgroundResource(R.drawable.bg_color_unselected);
        selectedFrame.setBackgroundResource(R.drawable.bg_color_selected);
    }

    // ===== טעינת אירוע לעריכה =====
    private void loadEventForEdit() {
        FirebaseFirestore.getInstance()
                .collection("calendar_events")
                .document(editingEventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    etEventTitle.setText(doc.getString("title"));
                    etEventDate.setText(doc.getString("date"));
                    etEndDate.setText(doc.getString("endDate"));
                    etStartTime.setText(doc.getString("startTime"));
                    etEndTime.setText(doc.getString("endTime"));

                    String color = doc.getString("color");
                    if (color != null) {
                        switch (color) {
                            case "teal": selectColor(frameTeal, "teal"); break;
                            case "amber": selectColor(frameAmber, "amber"); break;
                            case "rose": selectColor(frameRose, "rose"); break;
                            case "slate": selectColor(frameSlate, "slate"); break;
                            case "emerald": selectColor(frameEmerald, "emerald"); break;
                            default: selectColor(frameIndigo, "indigo");
                        }
                    }

                    Long reminder = doc.getLong("reminderMinutes");
                    if (reminder != null) {
                        setReminderFromMinutes(reminder.intValue());
                    }

                    // משתמשים שנבחרו
                    pendingSelectedUserIds =
                            (List<String>) doc.get("assignedUserIds");
                });
    }

    // ===== הגדרת תזכורת לפי דקות =====
    private void setReminderFromMinutes(int minutes) {
        switch (minutes) {
            case 5:
                spReminder.setText("5 דקות לפני", false);
                break;
            case 10:
                spReminder.setText("10 דקות לפני", false);
                break;
            case 30:
                spReminder.setText("30 דקות לפני", false);
                break;
            case 60:
                spReminder.setText("שעה לפני", false);
                break;
            case 1440:
                spReminder.setText("יום לפני", false);
                break;
            default:
                spReminder.setText("ללא תזכורת", false);
        }
    }


}
