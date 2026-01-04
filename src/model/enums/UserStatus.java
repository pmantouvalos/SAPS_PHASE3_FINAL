package model.enums;

public enum UserStatus {
    ACTIVE,     // Ενεργός
    LOCKED,     // Κλειδωμένος (π.χ. μετά από αποτυχημένες προσπάθειες)
    DELETED     // Διεγραμμένος (Soft delete - κρατάμε ιστορικό)
}