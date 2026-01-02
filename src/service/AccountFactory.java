package service;

import model.*;

public class AccountFactory {
    public static Account createAccount(String type, String iban, String owner, double balance) {
        if (type == null) return null;
        
        type = type.trim(); //Καθαρισμός κενών

        switch (type) {
            case "Τρεχούμενος":
                return new CurrentAccount(iban, owner, balance);
            case "Ταμιευτηρίου":
                return new SavingsAccount(iban, owner, balance);
            case "Επαγγελματικός":
            case "Επιχειρηματικός":
                return new CurrentAccount(iban, owner, balance);
            default:
                // Fallback για να μην σκάει με NullPointerException
                System.out.println("Unknown type: " + type + ". Defaulting to CurrentAccount.");
                return new CurrentAccount(iban, owner, balance);
        }
    }
}