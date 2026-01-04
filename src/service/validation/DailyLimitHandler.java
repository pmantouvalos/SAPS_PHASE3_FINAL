package service.validation;

import model.entities.Account;
import model.entities.User;

public class DailyLimitHandler extends TransactionValidator {

    @Override
    public void validate(User user, Account account, double amount, double fee) throws Exception {
        // Εδώ ελέγχουμε το όριο μεταφοράς/πληρωμής
        // Σημείωση: Μπορείς να διαχωρίσεις τα όρια ανάλογα με το είδος, 
        // αλλά για απλότητα ελέγχουμε το γενικό LimitTransfer εδώ.
        
        if (amount > user.getLimitTransfer()) {
            throw new Exception("Το ποσό (" + amount + "€) υπερβαίνει το ημερήσιο όριο συναλλαγών σας (" + user.getLimitTransfer() + "€).");
        }
        
        checkNext(user, account, amount, fee);
    }
}