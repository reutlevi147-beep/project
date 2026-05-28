package com.mycasa.app;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FinanceFixedExpensesActivity extends AppCompatActivity {

    private RecyclerView rvCategories;

    private FirebaseFirestore db;
    private String groupId;

    // אתחול מסך הצגת הוצאות קבועות והגדרת רשימת הקטגוריות
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finance_fixed_expenses);

        rvCategories = findViewById(R.id.rvFixedExpenses);
        rvCategories.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        groupId = AppSession.getGroupId();

        loadFixedExpensesWithDrillDown();
    }

    // טעינת הוצאות קבועות וחלוקתן לפי קטגוריות ותתי־קטגוריות
    private void loadFixedExpensesWithDrillDown() {

        if (groupId == null) return;

        // 1️⃣ שלד מלא מהקטלוג
        Map<String, CategoryWithItems> categoryMap = new LinkedHashMap<>();

        for (FlowCategory cat : FinanceCatalog.getFixedExpenseCategories()) {

            CategoryWithItems parent = new CategoryWithItems(
                    cat.getId(),
                    cat.getTitle(),
                    resolveColorForFixedCategory(cat.getId()),
                    0,
                    new ArrayList<>()
            );

            // כל תתי הקטגוריות של הקטגוריה
            for (FlowItem item : FinanceCatalog.getAllItems()) {
                if (cat.getId().equals(item.getCategoryId())) {
                    parent.items.add(
                            new SubCategoryItem(
                                    item.getId(),
                                    item.getTitle(),
                                    0
                            )
                    );
                }
            }

            categoryMap.put(cat.getId(), parent);
        }

        // 2️⃣ מילוי סכומים מה־Firebase
        db.collection("groups")
                .document(groupId)
                .collection("finance_flow_items")
                .whereEqualTo("enabled", true)
                .get()
                .addOnSuccessListener(snapshot -> {

                    for (QueryDocumentSnapshot doc : snapshot) {

                        String categoryId = doc.getString("categoryId");
                        if (!categoryMap.containsKey(categoryId)) continue;

                        Number num = (Number) doc.get("amount");
                        if (num == null) continue;

                        double amount = adjustAmountByFrequency(
                                num.doubleValue(),
                                doc.getString("frequency")
                        );

                        String title = doc.getString("title");

                        CategoryWithItems parent = categoryMap.get(categoryId);

                        for (SubCategoryItem sub : parent.items) {
                            if (sub.title.equals(title)) {
                                sub.amount += amount;
                                parent.totalAmount += amount;
                                break;
                            }
                        }
                    }

                    rvCategories.setAdapter(
                            new FixedExpenseDrillAdapter(
                                    new ArrayList<>(categoryMap.values())
                            )
                    );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    // החזרת צבע מתאים לקטגוריית הוצאה קבועה
    private int resolveColorForFixedCategory(String categoryId) {

        switch (categoryId) {
            case "expense_communication":
                return 0xFF6366F1;
            case "expense_housing":
                return 0xFF4B5563;
            case "expense_kids":
                return 0xFF8B5CF6;
            case "expense_insurance":
                return 0xFFEC4899;
            case "expense_transport":
                return 0xFF10B981;
            case "expense_finance":
                return 0xFFF59E0B;
            case "expense_savings":
                return 0xFF14B8A6;
            case "expense_other_fixed":
                return 0xFF9CA3AF;
            default:
                return 0xFF9CA3AF;
        }
    }

    // התאמת סכום ההוצאה לפי תדירות התשלום
    private double adjustAmountByFrequency(double amount, String frequency) {

        if (frequency == null) return amount;

        String f = frequency.trim();
        if (f.contains("שנת")) return amount / 12.0;
        if (f.contains("דו")) return amount / 2.0;

        return amount;
    }
}
