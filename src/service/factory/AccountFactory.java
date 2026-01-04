package service.factory;

import model.entities.Account;
import model.entities.BusinessAccount;
import model.entities.CurrentAccount;
import model.entities.SavingsAccount;
import model.enums.AccountType;

public class AccountFactory {

    /**
     * Δημιουργία λογαριασμού με βάση το String type (π.χ. από CSV).
     */
    public static Account createAccount(String typeStr, String iban, String ownerName, double balance) {
        // Χρησιμοποιούμε τη μέθοδο fromString του Enum που έφτιαξες
        AccountType type = AccountType.fromString(typeStr);
        
        // Καλούμε την overloaded μέθοδο
        return createAccount(type, iban, ownerName, balance);
    }

    /**
     * Δημιουργία λογαριασμού με βάση το Enum AccountType.
     */
    public static Account createAccount(AccountType type, String iban, String ownerName, double balance) {
        if (type == null) type = AccountType.CURRENT; // Default

        switch (type) {
            case SAVINGS:
                return new SavingsAccount(iban, ownerName, balance);
            
            case BUSINESS:
                return new BusinessAccount(iban, ownerName, balance);
            
            case CURRENT:
            default:
                return new CurrentAccount(iban, ownerName, balance);
        }
    }
}