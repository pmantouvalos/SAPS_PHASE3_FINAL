package service.command;

import data.BankDataStore;
import model.entities.Account;
import model.entities.Bill;
import model.entities.Transaction;
import model.entities.User;
import model.enums.TransactionType;
import service.factory.TransactionFactory;
import service.validation.TransactionValidator;

import javax.swing.*;

public class PaymentCommand implements Command {
    private String sourceIban;
    private String providerName;
    private double amount;
    private String description;
    private Bill billReference; 

    public PaymentCommand(String sourceIban, String providerName, double amount, String description, Bill bill) {
        this.sourceIban = sourceIban;
        this.providerName = providerName;
        this.amount = amount;
        this.description = description;
        this.billReference = bill;
    }

    @Override
    public void execute() {
        BankDataStore data = BankDataStore.getInstance();
        Account account = data.getAccountByIban(sourceIban);
        
        if (account == null) {
            JOptionPane.showMessageDialog(null, "Λάθος λογαριασμός.");
            return;
        }

        // 1. Εύρεση Χρήστη
        User user = data.getUserByFullName(account.getOwnerName());
        if (user == null) user = data.getLoggedUser();

        // 2. ΕΠΙΚΥΡΩΣΗ ΜΕΣΩ CHAIN OF RESPONSIBILITY
        try {
            // Fee είναι 0.0 για τις πληρωμές (σύμφωνα με τις παραδοχές μας)
            if (user != null) {
                TransactionValidator.buildChain().validate(user, account, amount, 0.0);
            } else {
                if (account.getBalance() < amount) throw new Exception("Ανεπαρκές Υπόλοιπο.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Αδυναμία Πληρωμής:\n" + e.getMessage(), "Σφάλμα", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 3. Εκτέλεση (Χρέωση)
        account.withdraw(amount);

        // 4. Καταγραφή Συναλλαγής (Factory)
        Transaction t = TransactionFactory.createTransaction(
                amount,
                TransactionType.PAYMENT,
                description,
                sourceIban,
                providerName,
                0.0,
                account.getBalance()
        );
        account.addTransaction(t);
        
        // 5. Αποθήκευση
        data.saveAllData();
    }
}