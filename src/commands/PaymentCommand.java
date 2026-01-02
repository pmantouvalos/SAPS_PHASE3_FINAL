package commands;

import bridge.*;
import model.Account;
import model.Bill;
import model.Transaction;
import service.BankDataStore;
import utils.TimeManager;
import javax.swing.JOptionPane;

public class PaymentCommand {
    private String sourceIban, receiverName, description;
    private double amount;
    private Bill linkedBill;

    public PaymentCommand(String src, String rec, double amt, String desc, Bill bill) {
        this.sourceIban = src;
        this.receiverName = rec;
        this.amount = amt;
        this.description = desc;
        this.linkedBill = bill;
    }

    public void execute() {
        BankDataStore store = BankDataStore.getInstance();
        Account account = store.getAccountByIban(sourceIban);

        if (account != null && account.getBalance() >= amount) {
            //1.ΧΡΕΩΣΗ ΠΕΛΑΤΗ
            account.setBalance(account.getBalance() - amount);

            Transaction t = new Transaction.Builder(amount)
                    .setType("Πληρωμή")
                    .setDescription(description)
                    .setDate(TimeManager.getInstance().getDate())
                    .setSender(sourceIban)
                    .setReceiver(receiverName)
                    .setFee(0.0)
                    .setBalanceAfter(account.getBalance())
                    .build();
            account.addTransaction(t);

            //2.ΠΙΣΤΩΣΗ ΕΠΙΧΕΙΡΗΣΗΣ (Αν βρεθεί λογαριασμός με αυτό το όνομα)
            //Ψάχνουμε αν υπάρχει λογαριασμός που ανήκει στον receiverName (π.χ. ΔΕΗ)
            Account bizAccount = null;
            for(Account acc : store.getAccounts()) {
                //Ελέγχουμε αν το όνομα του δικαιούχου περιέχει το όνομα της πληρωμής
                if(acc.getOwnerName().equalsIgnoreCase(receiverName)) {
                    bizAccount = acc;
                    break;
                }
            }

            if (bizAccount != null) {
                bizAccount.setBalance(bizAccount.getBalance() + amount);
                
                //Καταγραφή Κίνησης στην Επιχείρηση
                Transaction tBiz = new Transaction.Builder(amount)
                    .setType("Είσπραξη")
                    .setDescription("Πληρωμή από " + sourceIban)
                    .setDate(TimeManager.getInstance().getDate())
                    .setSender(sourceIban)
                    .setReceiver(receiverName)
                    .setBalanceAfter(bizAccount.getBalance())
                    .build();
                bizAccount.addTransaction(tBiz);
            }

            //3.ΔΙΑΓΡΑΦΗ ΟΦΕΙΛΗΣ
            if (linkedBill != null) {
                store.getPendingBills().remove(linkedBill);
            }

            //4. ΕΙΔΟΠΟΙΗΣΗ
            new TransactionNotification(new EmailSender()).send("Πληρωμή " + amount + "€ ολοκληρώθηκε.");
            
        } else {
            System.err.println("Payment Failed (Balance) for " + sourceIban);
            JOptionPane.showMessageDialog(null, "Ανεπαρκές υπόλοιπο για την πληρωμή.");
        }
    }
}