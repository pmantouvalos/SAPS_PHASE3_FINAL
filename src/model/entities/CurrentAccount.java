package model.entities;

import model.enums.AccountType;

public class CurrentAccount extends Account {

    public CurrentAccount(String iban, String ownerName, double balance) {
        super(iban, ownerName, balance);
    }

    @Override
    public String getAccountType() {
        return AccountType.CURRENT.toString(); // "Τρεχούμενος"
    }
}