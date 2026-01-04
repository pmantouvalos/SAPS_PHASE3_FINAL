package service.validation;

import model.entities.Account;
import model.entities.User;

public class UserStatusHandler extends TransactionValidator {

    @Override
    public void validate(User user, Account account, double amount, double fee) throws Exception {
        if (user.isLocked()) {
            throw new Exception("Ο λογαριασμός χρήστη είναι κλειδωμένος. Επικοινωνήστε με τον διαχειριστή.");
        }
        // Αν είναι ΟΚ, προχωράμε στον επόμενο
        checkNext(user, account, amount, fee);
    }
}