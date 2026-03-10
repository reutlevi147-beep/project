package com.mycasa.app;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class ChildPermissionsActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private String groupId;
    private String childUserId;

    private PermissionSection financeSection;
    private PermissionSection shoppingSection;
    private PermissionSection calendarSection;
    private PermissionSection tasksSection;

    private MaterialButton btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_permissions);

        db = FirebaseFirestore.getInstance();


        groupId = getIntent().getStringExtra("GROUP_ID");
        childUserId = getIntent().getStringExtra("USER_ID");

        if (groupId == null || childUserId == null) {
            Toast.makeText(this, "חסר נתונים למסך הרשאות", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // init sections from includes
        financeSection = new PermissionSection(findViewById(R.id.sectionFinance));
        shoppingSection = new PermissionSection(findViewById(R.id.sectionShopping));
        calendarSection = new PermissionSection(findViewById(R.id.sectionCalendar));
        tasksSection = new PermissionSection(findViewById(R.id.sectionTasks));

        // apply UI style like your design
        financeSection.applyMeta(
                "כלכלה",
                "גישה למידע כלכלי ומטרות חיסכון",
                R.drawable.ic_trending_down_24,     // תחליפי לאייקון שיש לך
                "#F3F4F6", "#D1D5DB",           // OFF: bg, stroke
                "#F1F5F9", "#CBD5E1",           // ON? (לא חובה, אנחנו משנים לפי main)
                "#FFFFFF", "#E5E7EB"            // (לא בשימוש ישיר כאן)
        );
        // נעדכן צבעים יפים כמו בתמונה:
        financeSection.setTheme("#F3F4F6", "#D1D5DB", "#FFFFFF"); // default (אפור)
        // כשה-main ידלק אנחנו נצבע דרך setOnState

        shoppingSection.applyMeta(
                "קניות",
                "צפייה והוספת פריטים לרשימת קניות",
                R.drawable.ic_cart,
                "#ECFDF5", "#10B981",
                "#ECFDF5", "#10B981",
                "#FFFFFF", "#E5E7EB"
        );

        calendarSection.applyMeta(
                "יומן",
                "צפייה באירועים ותכנון משפחתי",
                R.drawable.ic_calendar,
                "#FFFBEB", "#F59E0B",
                "#FFFBEB", "#F59E0B",
                "#FFFFFF", "#E5E7EB"
        );

        tasksSection.applyMeta(
                "משימות",
                "ניהול משימות אישיות ומשפחתיות",
                R.drawable.ic_list,
                "#EEF2FF", "#6366F1",
                "#EEF2FF", "#6366F1",
                "#FFFFFF", "#E5E7EB"
        );

        // Hook main toggle behavior (disable/enable + color)
        financeSection.attachMainLogic();
        shoppingSection.attachMainLogic();
        calendarSection.attachMainLogic();
        tasksSection.attachMainLogic();

        btnSave = findViewById(R.id.btnSavePermissions);
        btnSave.setOnClickListener(v -> savePermissions());

        loadPermissions();
    }

    // =========================
    // Load from Firestore
    // =========================
    private void loadPermissions() {
        db.collection("groups")
                .document(groupId)
                .collection("users")
                .document(childUserId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) return;

                    Object permsObj = doc.get("permissions");
                    if (!(permsObj instanceof Map)) {
                        // אין עדיין הרשאות -> נשאיר ברירת מחדל (OFF)
                        financeSection.setMainChecked(false, false);
                        shoppingSection.setMainChecked(false, false);
                        calendarSection.setMainChecked(false, false);
                        tasksSection.setMainChecked(false, false);
                        return;
                    }

                    @SuppressWarnings("unchecked")
                    Map<String, Object> permissions = (Map<String, Object>) permsObj;

                    financeSection.applyFromFirestore(permissions, "finance");
                    shoppingSection.applyFromFirestore(permissions, "shopping");
                    calendarSection.applyFromFirestore(permissions, "calendar");
                    tasksSection.applyFromFirestore(permissions, "tasks");
                });
    }

    // =========================
    // Save to Firestore
    // =========================
    private void savePermissions() {

        Map<String, Object> permissions = new HashMap<>();
        permissions.put("finance", financeSection.toMap());
        permissions.put("shopping", shoppingSection.toMap());
        permissions.put("calendar", calendarSection.toMap());
        permissions.put("tasks", tasksSection.toMap());

        Map<String, Object> update = new HashMap<>();
        update.put("permissions", permissions);

        db.collection("groups")
                .document(groupId)
                .collection("users")
                .document(childUserId)
                .set(update, SetOptions.merge())
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "ההרשאות נשמרו", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "שגיאה בשמירה: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // ============================================================
    // PermissionSection helper: works with your item_permission_section.xml
    // ============================================================
    private static class PermissionSection {

        private final View root;
        private final MaterialCardView card;

        private final TextView tvTitle;
        private final TextView tvDesc;
        private final ImageView ivIcon;

        private final SwitchMaterial swMain;
        private final SwitchMaterial swView;
        private final SwitchMaterial swAdd;
        private final SwitchMaterial swEdit;
        private final SwitchMaterial swDelete;

        private String onBg = "#F3F4F6";
        private String onStroke = "#D1D5DB";
        private String offBg = "#F3F4F6";
        private String offStroke = "#D1D5DB";

        PermissionSection(@NonNull View includeRoot) {
            this.root = includeRoot;

            // includeRoot in LinearLayout points to the root of included layout: MaterialCardView
            // so we can cast it:
            this.card = (MaterialCardView) includeRoot;

            tvTitle = includeRoot.findViewById(R.id.tvSectionTitle);
            tvDesc = includeRoot.findViewById(R.id.tvSectionDesc);
            ivIcon = includeRoot.findViewById(R.id.ivSectionIcon);

            swMain = includeRoot.findViewById(R.id.switchMain);
            swView = includeRoot.findViewById(R.id.switchView);
            swAdd = includeRoot.findViewById(R.id.switchAdd);
            swEdit = includeRoot.findViewById(R.id.switchEdit);
            swDelete = includeRoot.findViewById(R.id.switchDelete);

            // default OFF
            setMainChecked(false, false);
        }

        void applyMeta(String title,
                       String desc,
                       int iconRes,
                       String enabledBg,
                       String enabledStroke,
                       String disabledBg,
                       String disabledStroke,
                       String unused1,
                       String unused2) {
            tvTitle.setText(title);
            tvDesc.setText(desc);
            ivIcon.setImageResource(iconRes);

            // In your design: when main ON -> section is colored
            // when main OFF -> section is grey
            this.onBg = enabledBg;
            this.onStroke = enabledStroke;
            this.offBg = "#FFFFFF";     // card background looks white/grey. We'll set proper in attach logic
            this.offStroke = "#E5E7EB";
        }

        void setTheme(String bg, String stroke, String iconBgOptional) {
            // optional helper if you want override
            this.onBg = bg;
            this.onStroke = stroke;
        }

        void attachMainLogic() {
            swMain.setOnCheckedChangeListener((buttonView, isChecked) -> {
                setEnabledInner(isChecked);

                if (!isChecked) {
                    // reset children toggles
                    swView.setChecked(false);
                    swAdd.setChecked(false);
                    swEdit.setChecked(false);
                    swDelete.setChecked(false);
                }

                applyCardState(isChecked);
            });

            // apply state now (initial)
            applyCardState(swMain.isChecked());
            setEnabledInner(swMain.isChecked());
        }

        void setMainChecked(boolean checked, boolean triggerListener) {
            if (triggerListener) {
                swMain.setChecked(checked);
            } else {
                // temporarily remove listener effect by setting first, then manual apply
                swMain.setOnCheckedChangeListener(null);
                swMain.setChecked(checked);
                attachMainLogic(); // re-attach logic (safe here because we call during init/load)
            }
        }

        private void setEnabledInner(boolean enabled) {
            swView.setEnabled(enabled);
            swAdd.setEnabled(enabled);
            swEdit.setEnabled(enabled);
            swDelete.setEnabled(enabled);
        }

        private void applyCardState(boolean enabled) {
            // background tint
            card.setCardBackgroundColor(Color.parseColor(enabled ? onBg : "#F3F4F6"));
            card.setStrokeColor(Color.parseColor(enabled ? onStroke : "#E5E7EB"));
        }

        Map<String, Boolean> toMap() {
            Map<String, Boolean> m = new HashMap<>();
            boolean mainOn = swMain.isChecked();

            // אם main כבוי -> נשמור הכל false
            m.put("view", mainOn && swView.isChecked());
            m.put("add", mainOn && swAdd.isChecked());
            m.put("edit", mainOn && swEdit.isChecked());
            m.put("delete", mainOn && swDelete.isChecked());

            return m;
        }

        void applyFromFirestore(Map<String, Object> permissions, String key) {
            Object sectionObj = permissions.get(key);
            if (!(sectionObj instanceof Map)) {
                setMainChecked(false, false);
                return;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> section = (Map<String, Object>) sectionObj;

            boolean v = Boolean.TRUE.equals(section.get("view"));
            boolean a = Boolean.TRUE.equals(section.get("add"));
            boolean e = Boolean.TRUE.equals(section.get("edit"));
            boolean d = Boolean.TRUE.equals(section.get("delete"));

            boolean mainOn = v || a || e || d;

            // set main first (without firing too much)
            swMain.setOnCheckedChangeListener(null);
            swMain.setChecked(mainOn);

            setEnabledInner(mainOn);

            if (!mainOn) {
                swView.setChecked(false);
                swAdd.setChecked(false);
                swEdit.setChecked(false);
                swDelete.setChecked(false);
            } else {
                swView.setChecked(v);
                swAdd.setChecked(a);
                swEdit.setChecked(e);
                swDelete.setChecked(d);
            }

            attachMainLogic(); // re-attach + recolor
        }
    }
}