package utils;

import java.util.regex.Pattern;

public class ValidationUtils {

    // Regex Patterns
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final String AFM_REGEX = "^[0-9]{9}$"; // ΑΦΜ: Ακριβώς 9 ψηφία
    private static final String PHONE_REGEX = "^(69|2)[0-9]{9}$"; // Ελληνικά κινητά/σταθερά (10 ψηφία)
    
    // ΑΛΛΑΓΗ: Γενικό Regex για IBAN (Διεθνές πρότυπο)
    // 2 Γράμματα (Χώρα) + 2 Ψηφία (Check) + 11 έως 30 αλφαριθμητικά
    private static final String IBAN_REGEX = "^[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}$";

    // Private Constructor (Utility class)
    private ValidationUtils() {}

    /**
     * Ελέγχει αν το email είναι έγκυρο.
     */
    public static boolean isValidEmail(String email) {
        return email != null && Pattern.matches(EMAIL_REGEX, email);
    }

    /**
     * Ελέγχει αν το ΑΦΜ είναι έγκυρο (9 ψηφία).
     */
    public static boolean isValidAfm(String afm) {
        return afm != null && Pattern.matches(AFM_REGEX, afm);
    }

    /**
     * Ελέγχει αν το τηλέφωνο είναι έγκυρο (Ελληνικό format).
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && Pattern.matches(PHONE_REGEX, phone);
    }

    /**
     * Ελέγχει αν το IBAN έχει έγκυρη μορφή (Ελληνικό ή Διεθνές).
     * Υποστηρίζει π.χ. GR (27 chars), GB (22 chars), DE (22 chars) κλπ.
     */
    public static boolean isValidIban(String iban) {
        if (iban == null) return false;
        // Αφαιρούμε κενά και κάνουμε κεφαλαία
        String cleanIban = iban.replace(" ", "").toUpperCase();
        return Pattern.matches(IBAN_REGEX, cleanIban);
    }

    /**
     * Ελέγχει αν ένα String είναι θετικός αριθμός.
     */
    public static boolean isPositiveNumber(String str) {
        try {
            double d = Double.parseDouble(str);
            return d > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}