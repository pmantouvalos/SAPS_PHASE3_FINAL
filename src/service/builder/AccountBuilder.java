package service.builder;

import model.entities.Account;
import model.enums.AccountType;
import service.factory.AccountFactory; // Χρήση του Factory

public class AccountBuilder {
    private String iban;
    private String ownerName;
    private double balance;
    private AccountType type;

    public AccountBuilder() {
        // Defaults
        this.balance = 0.0;
        this.type = AccountType.CURRENT; // Default τύπος
    }

    public AccountBuilder setIban(String iban) { this.iban = iban; return this; }
    public AccountBuilder setOwnerName(String ownerName) { this.ownerName = ownerName; return this; }
    public AccountBuilder setBalance(double balance) { this.balance = balance; return this; }
    public AccountBuilder setType(AccountType type) { this.type = type; return this; }

    public Account build() {
        if (iban == null || ownerName == null) {
            throw new IllegalStateException("IBAN and Owner Name are required to build an Account");
        }
        // Καλεί το Factory που φτιάχνει το συγκεκριμένο subclass (Current/Savings/Business)
        // Προσοχή: Το Factory περιμένει String ή Enum ανάλογα την υλοποίησή σου.
        // Εδώ υποθέτουμε ότι το Factory δέχεται String type.
        return AccountFactory.createAccount(type.toString(), iban, ownerName, balance);
    }
}