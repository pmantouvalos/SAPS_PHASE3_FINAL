package model.entities;

import model.enums.AccessLevel;
import java.io.Serializable;
import java.time.LocalDate;

public class AccountAccess implements Serializable {
    
    // Σύνδεση με Χρήστη και Λογαριασμό (Foreign Keys)
    private String userAfm;      // Αναγνωριστικό χρήστη (ΑΦΜ)
    private String accountIban;  // Αναγνωριστικό λογαριασμού (IBAN)
    
    // Πεδία από το Domain Model (σελ. 32)
    private AccessLevel accessLevel; // Το Enum που έχεις (VIEW_ONLY, FULL_ACCESS, OWNER)
    private LocalDate validFrom;     // Ημερομηνία έναρξης δικαιώματος
    private boolean isActive;        // Αν είναι ενεργό το δικαίωμα

    // --- Constructors ---

    // Πλήρης Constructor
    public AccountAccess(String userAfm, String accountIban, AccessLevel accessLevel, LocalDate validFrom, boolean isActive) {
        this.userAfm = userAfm;
        this.accountIban = accountIban;
        this.accessLevel = accessLevel;
        this.validFrom = validFrom;
        this.isActive = isActive;
    }

    // Βοηθητικός Constructor (βάζει default ημερομηνία 'σήμερα' και active=true)
    public AccountAccess(String userAfm, String accountIban, AccessLevel accessLevel) {
        this(userAfm, accountIban, accessLevel, LocalDate.now(), true);
    }

    // --- Getters & Setters ---

    public String getUserAfm() {
        return userAfm;
    }

    public void setUserAfm(String userAfm) {
        this.userAfm = userAfm;
    }

    public String getAccountIban() {
        return accountIban;
    }

    public void setAccountIban(String accountIban) {
        this.accountIban = accountIban;
    }

    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    // --- Helper Methods ---

    /**
     * Ελέγχει αν ο χρήστης έχει δικαίωμα για οικονομικές συναλλαγές.
     */
    public boolean canExecuteTransactions() {
        return isActive && (accessLevel == AccessLevel.FULL_ACCESS || accessLevel == AccessLevel.OWNER);
    }

    /**
     * Ελέγχει αν ο χρήστης είναι ιδιοκτήτης (μπορεί να κλείσει λογαριασμό κλπ).
     */
    public boolean isOwner() {
        return isActive && accessLevel == AccessLevel.OWNER;
    }

    @Override
    public String toString() {
        return "Access{" +
                "User='" + userAfm + '\'' +
                ", Level=" + accessLevel +
                ", Active=" + isActive +
                '}';
    }
}