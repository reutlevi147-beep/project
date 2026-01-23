package com.mycasa.app;

import java.util.ArrayList;
import java.util.List;

public class FinanceCatalog {

    /* =====================================================
     * הכנסות קבועות
     * ===================================================== */
    public static List<FlowCategory> getFixedIncomeCategories() {
        List<FlowCategory> list = new ArrayList<>();

        list.add(new FlowCategory("income_work", "עבודה והכנסות", true));
        list.add(new FlowCategory("income_other", "הכנסות נוספות", true));

        return list;
    }

    /* =====================================================
     * הוצאות קבועות
     * ===================================================== */
    public static List<FlowCategory> getFixedExpenseCategories() {
        List<FlowCategory> list = new ArrayList<>();

        list.add(new FlowCategory("expense_communication", "תקשורת", false));
        list.add(new FlowCategory("expense_housing", "דיור", false));
        list.add(new FlowCategory("expense_kids", "ילדים וחינוך", false));
        list.add(new FlowCategory("expense_insurance", "ביטוחים", false));
        list.add(new FlowCategory("expense_transport", "תחבורה", false));
        list.add(new FlowCategory("expense_finance", "עלויות מימון ובנק", false));
        list.add(new FlowCategory("expense_savings", "חיסכון", false));
        list.add(new FlowCategory("expense_other_fixed", "שונות", false));

        return list;
    }

    /* =====================================================
     * הוצאות משתנות
     * ===================================================== */
    public static List<FlowCategory> getVariableExpenseCategories() {
        List<FlowCategory> list = new ArrayList<>();

        list.add(new FlowCategory("expense_food", "אוכל וקניות", false));
        list.add(new FlowCategory("expense_health", "בריאות", false));
        list.add(new FlowCategory("expense_leisure", "פנאי ובילויים", false));
        list.add(new FlowCategory("expense_personal", "טיפוח ויופי", false));
        list.add(new FlowCategory("expense_pets", "חיות מחמד", false));
        list.add(new FlowCategory("expense_home_misc", "שונות", false));

        return list;
    }

    /* =====================================================
     * כל תתי־הקטגוריות
     * ===================================================== */
    public static List<FlowItem> getAllItems() {
        List<FlowItem> list = new ArrayList<>();

        /* ===== הכנסות ===== */
        list.add(new FlowItem("salary", "income_work", "משכורת"));
        list.add(new FlowItem("bonus", "income_work", "בונוס"));
        list.add(new FlowItem("freelance", "income_work", "עצמאי / פרילנס"));

        list.add(new FlowItem("allowance", "income_other", "קצבאות"));
        list.add(new FlowItem("rent_income", "income_other", "שכירות"));
        list.add(new FlowItem("other_income", "income_other", "אחר"));

        /* ===== תקשורת ===== */
        list.add(new FlowItem("phone", "expense_communication", "טלפון נייד"));
        list.add(new FlowItem("internet", "expense_communication", "תשתית אינטרנט"));
        list.add(new FlowItem("tv", "expense_communication", "טלוויזיה / סטרימינג"));

        /* ===== דיור ===== */
        list.add(new FlowItem("rent", "expense_housing", "שכירות / משכנתא"));
        list.add(new FlowItem("electricity", "expense_housing", "חשמל"));
        list.add(new FlowItem("water", "expense_housing", "מים"));
        list.add(new FlowItem("gas", "expense_housing", "גז"));
        list.add(new FlowItem("house_committee", "expense_housing", "ועד בית"));
        list.add(new FlowItem("maintenance", "expense_housing", "תחזוקת בית"));

        /* ===== ילדים וחינוך ===== */
        list.add(new FlowItem("kindergarten", "expense_kids", "גן / מסגרת"));
        list.add(new FlowItem("school", "expense_kids", "בית ספר"));
        list.add(new FlowItem("lessons", "expense_kids", "חוגים"));
        list.add(new FlowItem("babysitter", "expense_kids", "בייביסיטר"));

        /* ===== ביטוחים ===== */
        list.add(new FlowItem("car_insurance", "expense_insurance", "ביטוח רכב"));
        list.add(new FlowItem("home_insurance", "expense_insurance", "ביטוח דירה"));
        list.add(new FlowItem("health_insurance", "expense_insurance", "ביטוח בריאות"));
        list.add(new FlowItem("life_insurance", "expense_insurance", "ביטוח חיים"));

        /* ===== תחבורה ===== */
        list.add(new FlowItem("fuel", "expense_transport", "דלק"));
        list.add(new FlowItem("public_transport", "expense_transport", "תחבורה ציבורית"));
        list.add(new FlowItem("car_maintenance", "expense_transport", "אחזקת רכב"));

        /* ===== עלויות מימון ===== */
        list.add(new FlowItem("loan", "expense_finance", "הלוואות"));
        list.add(new FlowItem("bank_fees", "expense_finance", "עמלות בנק"));
        list.add(new FlowItem("credit_interest", "expense_finance", "ריבית"));

        /* ===== חיסכון ===== */
        list.add(new FlowItem("pension", "expense_savings", "פנסיה"));
        list.add(new FlowItem("savings", "expense_savings", "חיסכון חודשי"));

        /* ===== אוכל וקניות ===== */
        list.add(new FlowItem("supermarket", "expense_food", "סופר"));
        list.add(new FlowItem("restaurants", "expense_food", "מסעדות"));
        list.add(new FlowItem("takeaway", "expense_food", "אוכל מוכן"));

        /* ===== בריאות ===== */
        list.add(new FlowItem("doctor", "expense_health", "רופאים"));
        list.add(new FlowItem("medicine", "expense_health", "תרופות"));
        list.add(new FlowItem("treatments", "expense_health", "טיפולים"));

        /* ===== פנאי ובילויים ===== */
        list.add(new FlowItem("cinema", "expense_leisure", "קולנוע"));
        list.add(new FlowItem("vacation", "expense_leisure", "חופשות"));
        list.add(new FlowItem("shows", "expense_leisure", "הופעות"));

        /* ===== טיפוח ויופי ===== */
        list.add(new FlowItem("haircut", "expense_personal", "ספר"));
        list.add(new FlowItem("cosmetics", "expense_personal", "קוסמטיקה"));

        /* ===== חיות מחמד ===== */
        list.add(new FlowItem("pet_food", "expense_pets", "אוכל לחיות"));
        list.add(new FlowItem("vet", "expense_pets", "וטרינר"));

        /* ===== שונות ===== */
        list.add(new FlowItem("misc_fixed", "expense_other_fixed", "אחר (קבוע)"));
        list.add(new FlowItem("misc_variable", "expense_home_misc", "אחר (משתנה)"));

        return list;
    }

    public static List<FlowItem> getPendingDemoItems() {

        List<FlowItem> list = new ArrayList<>();

        list.add(new FlowItem("electric", "expense_fixed", "חשמל"));
        list.get(0).setAmount(350);
        list.get(0).setFrequency("חודשי");

        list.add(new FlowItem("water", "expense_fixed", "מים"));
        list.get(1).setAmount(180);
        list.get(1).setFrequency("דו-חודשי");

        list.add(new FlowItem("mortgage", "expense_fixed", "משכנתא"));
        list.get(2).setAmount(3750);
        list.get(2).setFrequency("חודשי");

        list.add(new FlowItem("salary", "income_fixed", "משכורת"));
        list.get(3).setAmount(9000);
        list.get(3).setFrequency("חודשי");

        return list;
    }
    public static List<FlowItem> getPendingItemsForPeriod(String period) {

        List<FlowItem> result = new ArrayList<>();

        for (FlowItem item : getAllItems()) {

            // רק פריטים שמוגדרים (יש סכום)
            if (!item.isConfigured()) continue;

            String freq = item.getFrequency();

            if (period.equals("month") && freq.equals("חודשי")) {
                result.add(item);
            }

            if (period.equals("year") && freq.equals("שנתי")) {
                result.add(item);
            }

            if (period.equals("week")) {
                // כרגע אין שבועי – אפשר להרחיב בעתיד
            }
        }

        return result;
    }

    public static List<FlowItem> getAllPendingItems() {

        List<FlowItem> result = new ArrayList<>();

        for (FlowItem item : getAllItems()) {
            if (item.isConfigured()) {
                result.add(item);
            }
        }

        return result;
    }


}
