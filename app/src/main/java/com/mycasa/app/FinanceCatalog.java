package com.mycasa.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FinanceCatalog {

    // החזרת רשימת קטגוריות הכנסות קבועות
    public static List<FlowCategory> getFixedIncomeCategories() {
        List<FlowCategory> list = new ArrayList<>();



        list.add(new FlowCategory("income_work", "עבודה והכנסות", true));
        list.add(new FlowCategory("income_other", "הכנסות נוספות", true));

        return list;
    }

    // החזרת רשימת קטגוריות הוצאות קבועות
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

    // החזרת רשימת קטגוריות הוצאות משתנות
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

    // החזרת כל פריטי הפיננסים (תתי־קטגוריות) לפי קטגוריות
    public static List<FlowItem> getAllItems() {
        List<FlowItem> list = new ArrayList<>();

        /* ===== הכנסות ===== */
        list.add(new FlowItem("salary_main", "income_work", "משכורת"));
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

    // החזרת מזהי קטגוריות הכנסות לשימוש במסכים שונים
    public static List<String> getIncomeCategoryIds() {
        return Arrays.asList(
                "income_work",
                "income_other"
        );
    }

    // החזרת מזהי קטגוריות הוצאות לשימוש במסכים שונים
    public static List<String> getExpenseCategoryIds() {
        return Arrays.asList(
                "expense_food",
                "expense_health",
                "expense_leisure",
                "expense_personal",
                "expense_pets",
                "expense_home_misc"
        );
    }

    // החזרת שם תצוגה לקטגוריה לפי מזהה
    public static String getCategoryTitle(String id) {

        switch (id) {

            case "expense_food": return "אוכל וקניות";
            case "expense_health": return "בריאות";
            case "expense_leisure": return "פנאי ובילויים";
            case "expense_personal": return "טיפוח ויופי";
            case "expense_pets": return "חיות מחמד";
            case "expense_home_misc": return "שונות";

            case "income_work": return "עבודה";
            case "income_other": return "הכנסות נוספות";

            default: return "קטגוריה";
        }
    }


    // החזרת אייקון מתאים לקטגוריה לפי מזהה
    public static String getCategoryIcon(String id) {

        switch (id) {

            case "expense_food": return "🍔";
            case "expense_health": return "💊";
            case "expense_leisure": return "🎬";
            case "expense_personal": return "💄";
            case "expense_pets": return "🐶";
            case "expense_home_misc": return "📦";

            case "income_work": return "💰";
            case "income_other": return "📈";

            default: return "❓";
        }
    }





}
