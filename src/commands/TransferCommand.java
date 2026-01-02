package commands;

import bridge.*;
import model.Account;
import model.Transaction;
import service.BankDataStore;
import utils.TimeManager;
import javax.swing.JOptionPane;

public class TransferCommand {
    private String sourceIban, targetIban, description;
    private double amount;
    private double commission;
    private boolean isSilent;

    public TransferCommand(String src, String trg, double amt, double comm, String desc, boolean silent) {
        this.sourceIban = src;
        this.targetIban = trg;
        this.amount = amt;
        this.commission = comm;
        this.description = desc;
        this.isSilent = silent;
    }
    
    //Constructor Πάγιας
    public TransferCommand(String src, String trg, double amt, double comm, String desc) {
        this(src, trg, amt, comm, desc, true);
    }

    public void execute() {
        BankDataStore store = BankDataStore.getInstance();
        Account sAcc = store.getAccountByIban(sourceIban);
        Account tAcc = store.getAccountByIban(targetIban);

        double totalCharge = amount + commission;

        if (sAcc != null && sAcc.getBalance() >= totalCharge) {
            //1.ΧΡΕΩΣΗ ΑΠΟΣΤΟΛΕΑ
            //Μειώνουμε το υπόλοιπο κατά το συνολικό κόστος
            sAcc.setBalance(sAcc.getBalance() - totalCharge);
            
            //Καταγραφή Κίνησης Αποστολέα
            Transaction tOut = new Transaction.Builder(totalCharge) //Χρέωση με προμήθεια
                    .setType("Μεταφορά")
                    .setDescription(description)
                    .setDate(TimeManager.getInstance().getDate())
                    .setSender(sourceIban)
                    .setReceiver(targetIban)
                    .setFee(commission)
                    .setBalanceAfter(sAcc.getBalance()) //Το νέο μειωμένο υπόλοιπο
                    .build();
            sAcc.addTransaction(tOut);

            //2. ΠΙΣΤΩΣΗ ΠΑΡΑΛΗΠΤΗ (Αν υπάρχει στο σύστημα)
            if (tAcc != null) {
                //Αυξάνουμε το υπόλοιπο μόνο κατά το καθαρό ποσό
                tAcc.setBalance(tAcc.getBalance() + amount);
                
                //Καταγραφή Κίνησης Παραλήπτη
                Transaction tIn = new Transaction.Builder(amount) //Πίστωση καθαρού ποσού
                    .setType("Κατάθεση") //Ή "Μεταφορά (Εισερχόμενη)"
                    .setDescription(description)
                    .setDate(TimeManager.getInstance().getDate())
                    .setSender(sourceIban)
                    .setReceiver(targetIban)
                    .setFee(0.0)
                    .setBalanceAfter(tAcc.getBalance()) //Το νέο αυξημένο υπόλοιπο
                    .build();
                tAcc.addTransaction(tIn);
                
                if(!isSilent) System.out.println("LOG: Πιστώθηκε ο λογαριασμός " + targetIban);
            }

            //3. ΟΛΟΚΛΗΡΩΣΗ
            new TransactionNotification(new EmailSender()).send("Μεταφορά επιτυχής: -" + totalCharge + "€");

            if (!isSilent) {
                // JOptionPane.showMessageDialog(null, "Η μεταφορά ολοκληρώθηκε!");
            }
        } else {
            if (!isSilent) JOptionPane.showMessageDialog(null, "Ανεπαρκές υπόλοιπο ή λάθος λογαριασμός.");
            else System.err.println("Transfer Failed for " + sourceIban);
        }
    }
}