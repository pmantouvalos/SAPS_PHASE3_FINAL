package model.entities;

import model.enums.AccountType;

public class SavingsAccount extends Account {

    public SavingsAccount(String iban, String ownerName, double balance) {
        super(iban, ownerName, balance);
    }

    @Override
    public String getAccountType() {
        return AccountType.SAVINGS.toString(); // "Ταμιευτηρίου"
    }
}