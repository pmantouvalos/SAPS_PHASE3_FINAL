package service.validation;

import model.entities.Account;
import model.entities.User;

public abstract class TransactionValidator {
    
    private TransactionValidator next;

    /**
     * Συνδέει τον τρέχοντα handler με τον επόμενο στην αλυσίδα.
     * Επιστρέφει τον επόμενο για να μπορούμε να κάνουμε chaining (fluent interface).
     */
    public TransactionValidator linkWith(TransactionValidator next) {
        this.next = next;
        return next;
    }

    /**
     * Η μέθοδος που καλείται για επικύρωση.
     */
    public abstract void validate(User user, Account account, double amount, double fee) throws Exception;

    /**
     * Προχωράει στον επόμενο έλεγχο αν υπάρχει.
     */
    protected void checkNext(User user, Account account, double amount, double fee) throws Exception {
        if (next != null) {
            next.validate(user, account, amount, fee);
        }
    }

    // Helper για εύκολο στήσιμο της αλυσίδας
    public static TransactionValidator buildChain() {
        TransactionValidator head = new UserStatusHandler();
        head.linkWith(new DailyLimitHandler())
            .linkWith(new BalanceHandler());
        return head;
    }
}