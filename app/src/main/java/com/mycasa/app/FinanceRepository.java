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
import java.util.Random;

public class FinanceRepository {

    // שליפת פריטים שדורשים אישור מהשרת לפי קבוצה וסינון לפי תדירות
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

    // בדיקה האם פריט צריך להופיע לאישור לפי תאריך אישור אחרון ותדירות
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

    // עדכון תאריך אישור של פריט לאחר שהמשתמש אישר אותו
    public static void updateApproval(String groupId, FlowItem item) {
        if (groupId == null || item.getId() == null) return;

        FirebaseFirestore.getInstance()
                .collection("groups")
                .document(groupId)
                .collection("finance_flow_items")
                .document(item.getId())
                .update("lastApprovedAt", item.getLastApprovedAt());
    }

    // יצירה או עדכון של פריט פיננסי בשרת כולל נתוני תזמון ואישור
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

// ⭐️ חשובים
        data.put("lastApprovedAt", item.getLastApprovedAt());
        data.put("updatedAt", FieldValue.serverTimestamp());

        ref.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                // פריט חדש → תאריך יצירה
                data.put("createdAt", FieldValue.serverTimestamp());
            }

            ref.set(data, SetOptions.merge());
        });
    }


    public static void addApprovalHistory(
            String groupId,
            FlowItem item,
            int approvedAmount,
            Date approvedDate
    ) {
        if (groupId == null || item == null || item.getId() == null) return;

        Calendar cal = Calendar.getInstance();
        cal.setTime(approvedDate);

        Map<String, Object> approval = new HashMap<>();
        approval.put("itemId", item.getId());
        approval.put("title", item.getTitle());
        approval.put("categoryId", item.getCategoryId());
        approval.put("frequency", item.getFrequency());
        approval.put("approvedAmount", approvedAmount);
        approval.put("approvedAt", approvedDate);
        approval.put("year", cal.get(Calendar.YEAR));
        approval.put("month", cal.get(Calendar.MONTH)); // 0=ינואר

        FirebaseFirestore.getInstance()
                .collection("groups")
                .document(groupId)
                .collection("finance_flow_items")
                .document(item.getId())
                .collection("approvals")
                .add(approval);
    }

    public static void seedApprovalHistoryLast3Months(
            String groupId,
            FlowItem item
    ) {
        if (groupId == null || item == null || item.getId() == null) return;

        Calendar cal = Calendar.getInstance();
        Random random = new Random();

        double baseAmount = item.getAmount();

        for (int i = 1; i <= 3; i++) {

            Calendar temp = (Calendar) cal.clone();
            temp.add(Calendar.MONTH, -i);

            Date approvedDate = temp.getTime();

            // 🎯 שינוי רנדומלי ±20%
            double variation = 0.8 + (1.2 - 0.8) * random.nextDouble();
            int newAmount = (int) Math.round(baseAmount * variation);

            Map<String, Object> approval = new HashMap<>();
            approval.put("itemId", item.getId());
            approval.put("title", item.getTitle());
            approval.put("categoryId", item.getCategoryId());
            approval.put("frequency", item.getFrequency());
            approval.put("approvedAmount", newAmount);
            approval.put("approvedAt", approvedDate);
            approval.put("year", temp.get(Calendar.YEAR));
            approval.put("month", temp.get(Calendar.MONTH));

            FirebaseFirestore.getInstance()
                    .collection("groups")
                    .document(groupId)
                    .collection("finance_flow_items")
                    .document(item.getId())
                    .collection("approvals")
                    .add(approval);
        }
    }

    // שמירה פשוטה של פריט פיננסי בשרת (ללא לוגיקת עדכון מתקדמת)
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


