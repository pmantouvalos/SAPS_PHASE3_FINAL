package service.command;

import data.BankDataStore;
import model.entities.Account;
import model.entities.Transaction;
import model.entities.User;
import model.enums.TransactionType;
import service.bridge.TransferProtocol;
import service.factory.TransactionFactory;
import service.strategy.FeeStrategy;
import service.validation.TransactionValidator;

import javax.swing.*;

public class TransferCommand implements Command {
    private String sourceIban;
    private String targetIban;
    private double amount;
    private FeeStrategy feeStrategy;
    private TransferProtocol protocol; // Bridge Pattern
    private String description;
    private boolean isSimulated;

    public TransferCommand(String sourceIban, String targetIban, double amount, 
                           FeeStrategy feeStrategy, TransferProtocol protocol, 
                           String description, boolean isSimulated) {
        this.sourceIban = sourceIban;
        this.targetIban = targetIban;
        this.amount = amount;
        this.feeStrategy = feeStrategy;
        this.protocol = protocol;
        this.description = description;
        this.isSimulated = isSimulated;
    }

    @Override
    public void execute() {
        BankDataStore data = BankDataStore.getInstance();
        Account sAcc = data.getAccountByIban(sourceIban);

        if (sAcc == null) {
            if (!isSimulated) JOptionPane.showMessageDialog(null, "Λάθος λογαριασμός προέλευσης.");
            return;
        }

        // 1. Εύρεση Χρήστη (για ελέγχους ορίων/κλειδώματος)
        User user = data.getUserByFullName(sAcc.getOwnerName());
        if (user == null) user = data.getLoggedUser();

        // 2. Υπολογισμός Προμήθειας
        double fee = feeStrategy.calculateFee(amount);

        // 3. ΕΠΙΚΥΡΩΣΗ ΜΕΣΩ CHAIN OF RESPONSIBILITY
        try {
            if (user != null) {
                TransactionValidator.buildChain().validate(user, sAcc, amount, fee);
            } else {
                // Fallback έλεγχος μόνο για υπόλοιπο αν δεν βρεθεί χρήστης
                double total = amount + fee;
                if (sAcc.getBalance() < total) throw new Exception("Ανεπαρκές Υπόλοιπο.");
            }
        } catch (Exception e) {
            if (!isSimulated) JOptionPane.showMessageDialog(null, "Αδυναμία Συναλλαγής:\n" + e.getMessage(), "Σφάλμα", JOptionPane.ERROR_MESSAGE);
            return; 
        }

        // 4. ΕΚΤΕΛΕΣΗ ΜΕΣΩ BRIDGE PROTOCOL
        try {
            // ΑΛΛΑΓΗ: Αναμένουμε String (Transaction ID) αντί για boolean
            String transactionId = protocol.executeTransfer(sAcc, targetIban, amount);

            if (transactionId != null) {
                double totalCharge = amount + fee;
                
                // Χρέωση του αποστολέα
                sAcc.withdraw(totalCharge);

                // Δημιουργία περιγραφής που περιλαμβάνει το ID της συναλλαγής
                String finalDesc = String.format("%s Transfer [%s]: %s", 
                                     protocol.getProtocolName(), transactionId, description);

                // Καταγραφή Συναλλαγής (Factory)
                Transaction tOut = TransactionFactory.createTransaction(
                        amount,
                        TransactionType.TRANSFER,
                        finalDesc, // <-- Νέα περιγραφή
                        sourceIban,
                        targetIban,
                        fee,
                        sAcc.getBalance()
                );
                sAcc.addTransaction(tOut);

                // Πίστωση Προμήθειας στην Τράπεζα
                if (fee > 0) {
                    Account bankAcc = data.getCentralBankAccount();
                    if (bankAcc != null) {
                        bankAcc.deposit(fee);
                        Transaction tFee = TransactionFactory.createTransaction(
                            fee, TransactionType.DEPOSIT, 
                            "Commission from " + sourceIban, 
                            sourceIban, "Bank of TUC", 0.0, bankAcc.getBalance()
                        );
                        bankAcc.addTransaction(tFee);
                    }
                }
                
                // Αποθήκευση αλλαγών
                data.saveAllData();

                // Εμφάνιση επιτυχίας με τον κωδικό (αν δεν είναι simulation)
                if (!isSimulated) {
                    JOptionPane.showMessageDialog(null, "Επιτυχία! Κωδικός Συναλλαγής: " + transactionId);
                }
            }

        } catch (Exception e) {
            if (!isSimulated) JOptionPane.showMessageDialog(null, "Σφάλμα Μεταφοράς: " + e.getMessage());
        }
    }
}