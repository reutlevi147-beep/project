package com.mycasa.app;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FinanceRepository {

    // ======================================
    // טעינת פריטים לאישור (בלוק צהוב)
    // ======================================
    public static void getPendingItems(
            String groupId,
            String period,
            OnSuccessListener<List<FlowItem>> onSuccess
    ) {
        if (groupId == null) {
            onSuccess.onSuccess(new ArrayList<>());
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("groups")
                .document(groupId)
                .collection("finance_flow_items")
                .whereEqualTo("enabled", true)
                .get()
                .addOnSuccessListener(snapshot -> {

                    List<FlowItem> result = new ArrayList<>();
                    Date now = new Date();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        FlowItem item = doc.toObject(FlowItem.class);
                        if (item == null) continue;

                        if (shouldShowPending(item, period, now)) {
                            result.add(item);
                        }
                    }

                    onSuccess.onSuccess(result);
                });
    }

    // ======================================
    // בדיקה אם צריך להציג לאישור
    // ======================================
    private static boolean shouldShowPending(
            FlowItem item,
            String period,
            Date now
    ) {
        // אף פעם לא אושר → תמיד להציג
        if (item.getLastApprovedAt() == null) {
            return true;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(item.getLastApprovedAt());

        switch (item.getFrequency()) {
            case "חודשי":
                cal.add(Calendar.MONTH, 1);
                break;
            case "דו-חודשי":
                cal.add(Calendar.MONTH, 2);
                break;
            case "שנתי":
                cal.add(Calendar.YEAR, 1);
                break;
        }

        return cal.getTime().before(now);
    }

    // ======================================
    // עדכון אישור (לחיצה על "אישור")
    // ======================================
    public static void updateApproval(String groupId, FlowItem item) {
        if (groupId == null || item.getId() == null) return;

        FirebaseFirestore.getInstance()
                .collection("groups")
                .document(groupId)
                .collection("finance_flow_items")
                .document(item.getId())
                .update("lastApprovedAt", item.getLastApprovedAt());
    }

    public static void saveOrUpdateFlowItem(
            String groupId,
            FlowItem item
    ) {
        if (groupId == null || item == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference ref = db.collection("groups")
                .document(groupId)
                .collection("finance_flow_items")
                .document(item.getId());

        Map<String, Object> data = new HashMap<>();
        data.put("title", item.getTitle());
        data.put("categoryId", item.getCategoryId());
        data.put("amount", item.getAmount());
        data.put("frequency", item.getFrequency());
        data.put("enabled", item.getAmount() > 0);

        // ⭐️ זה החלק החשוב
        data.put("updatedAt", FieldValue.serverTimestamp());

        ref.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                // פריט חדש → תאריך יצירה
                data.put("createdAt", FieldValue.serverTimestamp());
            }

            ref.set(data, SetOptions.merge());
        });
    }



    // ===========================
    // ===========
    // שמירת פריט (מהגדרות כלכלה)
    // ======================================
    public static void saveFlowItem(String groupId, FlowItem item) {
        if (groupId == null || item.getId() == null) return;

        FirebaseFirestore.getInstance()
                .collection("groups")
                .document(groupId)
                .collection("finance_flow_items")
                .document(item.getId())
                .set(item);
    }



}


