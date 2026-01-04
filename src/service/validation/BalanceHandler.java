package service.validation;

import model.entities.Account;
import model.entities.User;

public class BalanceHandler extends TransactionValidator {

    @Override
    public void validate(User user, Account account, double amount, double fee) throws Exception {
        double totalRequired = amount + fee;
        
        if (account.getBalance() < totalRequired) {
            throw new Exception("Ανεπαρκές Υπόλοιπο. (Απαιτούνται: " + String.format("%.2f", totalRequired) + "€)");
        }
        
        checkNext(user, account, amount, fee);
    }
}