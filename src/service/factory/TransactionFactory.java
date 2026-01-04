package service.factory;

import model.entities.Transaction;
import model.enums.TransactionType;
import service.builder.TransactionBuilder;
import utils.TimeManager;
import java.time.LocalDate;

public class TransactionFactory {

    // Για ΝΕΕΣ συναλλαγές (Ημερομηνία = Τώρα)
    public static Transaction createTransaction(double amount, TransactionType type, String description, 
                                                String sender, String receiver, double fee, double balanceAfter) {
        return new TransactionBuilder(amount)
                .setType(type)
                .setDescription(description)
                .setDate(TimeManager.getInstance().getDate()) 
                .setSender(sender)
                .setReceiver(receiver)
                .setFee(fee)
                .setBalanceAfter(balanceAfter)
                .build();
    }

    // --- ΝΕΑ ΜΕΘΟΔΟΣ: Για ΦΟΡΤΩΣΗ από CSV (Ημερομηνία = Custom) ---
    public static Transaction createTransactionFromCsv(double amount, TransactionType type, String description, 
                                                       double fee, LocalDate date, String sender, 
                                                       String receiver, double balanceAfter) {
        return new TransactionBuilder(amount)
                .setType(type)
                .setDescription(description)
                .setFee(fee)
                .setDate(date) // Εδώ βάζουμε την ημερομηνία του αρχείου
                .setSender(sender)
                .setReceiver(receiver)
                .setBalanceAfter(balanceAfter)
                .build();
    }

    // Helper για απλή δημιουργία
    public static Transaction createSimpleTransaction(double amount, String typeStr, String description, double balanceAfter) {
        // ... (Ο κώδικας που ήδη έχεις) ...
        return null; // (Placeholder για το παράδειγμα)
    }
}