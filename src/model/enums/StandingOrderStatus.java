package model.enums;

public enum StandingOrderStatus {
    ACTIVE,     // Εκτελείται κανονικά
    PAUSED,     // Ο χρήστης την έχει παύσει προσωρινά
    EXPIRED,    // Έχει περάσει η ημερομηνία λήξης
    FAILED      // Απέτυχε η τελευταία εκτέλεση (π.χ. ανεπαρκές υπόλοιπο)
}