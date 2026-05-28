package com.mycasa.app;

import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public abstract class BaseActivity extends AppCompatActivity {

    protected FirebaseFirestore db;

    // =====================================================
    // 🔐 סוגי הרשאות
    // =====================================================
    public enum PagePermission {
        LOCKED,
        VIEW_ONLY,
        ADD_ONLY,
        ADD_EDIT,
        FULL_ACCESS
    }

    public enum AppPage {
        CALENDAR,
        TASKS,
        FINANCE,
        SHOPPING
    }

    public interface PermissionCallback {
        void onResult(PagePermission permission);
    }

    // שליפת הרשאות המשתמש מהשרת לפי עמוד האפליקציה
    protected void resolvePermissionFromServer(
            AppPage page,
            String groupId,
            String userId,
            PermissionCallback callback
    ) {

        db = FirebaseFirestore.getInstance();

        if (groupId == null || userId == null) {
            callback.onResult(PagePermission.VIEW_ONLY);
            return;
        }

        db.collection("groups")
                .document(groupId)
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        callback.onResult(PagePermission.VIEW_ONLY);
                        return;
                    }

                    String role = doc.getString("role");

                    if ("parent".equals(role)) {
                        callback.onResult(PagePermission.FULL_ACCESS);
                        return;
                    }

                    Map<String, Object> permissions =
                            (Map<String, Object>) doc.get("permissions");

                    if (permissions == null) {
                        callback.onResult(PagePermission.VIEW_ONLY);
                        return;
                    }

                    Map<String,Object> pagePermissions =
                            (Map<String,Object>) permissions.get(
                                    page.name().toLowerCase()
                            );

                    if(pagePermissions == null){
                        callback.onResult(PagePermission.VIEW_ONLY);
                        return;
                    }

                    boolean canView = getBoolean(pagePermissions,"view");
                    boolean canAdd = getBoolean(pagePermissions,"add");
                    boolean canEdit = getBoolean(pagePermissions,"edit");
                    boolean canDelete = getBoolean(pagePermissions,"delete");

                    if (!canView) {
                        callback.onResult(PagePermission.LOCKED);
                        return;
                    }

                    if (canAdd && canEdit && canDelete)
                        callback.onResult(PagePermission.FULL_ACCESS);
                    else if (canAdd && canEdit)
                        callback.onResult(PagePermission.ADD_EDIT);
                    else if (canAdd)
                        callback.onResult(PagePermission.ADD_ONLY);
                    else
                        callback.onResult(PagePermission.VIEW_ONLY);

                    if (canAdd && canEdit && canDelete)
                        callback.onResult(PagePermission.FULL_ACCESS);
                    else if (canAdd && canEdit)
                        callback.onResult(PagePermission.ADD_EDIT);
                    else if (canAdd)
                        callback.onResult(PagePermission.ADD_ONLY);
                    else
                        callback.onResult(PagePermission.VIEW_ONLY);
                });
    }

    // בדיקה האם ערך ההרשאה הוא true
    private boolean getBoolean(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value instanceof Boolean && (Boolean) value;
    }

    // נעילת המסך עבור משתמש מסוג ילד לפי ההרשאות
    protected void applyFullLockIfChild(
            String groupId,
            String userId,
            View mainContent,
            LinearLayout lockOverlay,
            FloatingActionButton fab
    ) {

        db = FirebaseFirestore.getInstance();

        if (groupId == null || userId == null) return;

        db.collection("groups")
                .document(groupId)
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) return;

                    String role = doc.getString("role");
                    boolean isChild = "child".equals(role);

                    if (isChild) {

                        if (lockOverlay != null)
                            lockOverlay.setVisibility(View.VISIBLE);

                        if (fab != null)
                            fab.setVisibility(View.GONE);

                        if (mainContent != null &&
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                            mainContent.setRenderEffect(
                                    RenderEffect.createBlurEffect(
                                            25f,
                                            25f,
                                            Shader.TileMode.CLAMP
                                    )
                            );
                        }
                    }
                });
    }
}