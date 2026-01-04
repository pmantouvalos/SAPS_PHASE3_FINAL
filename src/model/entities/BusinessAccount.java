package model.entities;

import model.enums.AccountType;

public class BusinessAccount extends Account {

    public BusinessAccount(String iban, String ownerName, double balance) {
        super(iban, ownerName, balance);
    }

    @Override
    public String getAccountType() {
        return AccountType.BUSINESS.toString(); // "Επαγγελματικός"
    }
}