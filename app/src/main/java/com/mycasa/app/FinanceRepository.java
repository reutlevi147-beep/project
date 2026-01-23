package com.mycasa.app;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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

    public static void saveOrUpdateFlowItem(String groupId, FlowItem item) {
        FirebaseFirestore.getInstance()
                .collection("groups")
                .document(groupId)
                .collection("finance_flow_items")
                .document(item.getId()) // ⭐️ ID קבוע
                .set(item);             // overwrite זה בסדר
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


