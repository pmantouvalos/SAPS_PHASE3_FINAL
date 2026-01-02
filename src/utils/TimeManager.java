package utils;

import commands.PaymentCommand;
import commands.TransferCommand;
import model.Account;
import model.Role;
import model.StandingOrder;
import model.Transaction;
import model.User;
import service.BankDataStore;

import javax.swing.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import bridge.*;
import model.Bill;

public class TimeManager {
    private static TimeManager instance;
    private LocalDate currentDate;
    
    //Σταθερές Χρεώσεων , Τόκων
    private static final double BUSINESS_MONTHLY_FEE = 5.00;
  
    
    //Επιτόκιο Ταμιευτηρίου (1.6%)
    private static final double SAVINGS_INTEREST_RATE = 0.016; 

    private TimeManager() { 
        this.currentDate = LocalDate.now(); 
    }

    public static synchronized TimeManager getInstance() {
        if (instance == null) instance = new TimeManager();
        return instance;
    }

    public void setDate(LocalDate date) {
        this.currentDate = date;
        System.out.println("System Date set to: " + getFormattedDate());
    }
    
    public LocalDate getDate() { return currentDate; }
    public String getFormattedDate() { return currentDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")); }

    //ΚΕΝΤΡΙΚΗ ΜΕΘΟΔΟΣ ΑΛΛΑΓΗΣ ΧΡΟΝΟΥ
    public void advanceTime() {
        int oldMonth = currentDate.getMonthValue();
        
        //1.Επόμενη Μέρα
        currentDate = currentDate.plusDays(1);
        System.out.println(">>> ΝΕΑ ΗΜΕΡΟΜΗΝΙΑ: " + getFormattedDate());

        //2.Εκτέλεση Πάγιων Εντολών
        executeGlobalStandingOrders();
        checkBills();
        checkUpcomingStandingOrders();
        //3.ΕΛΕΓΧΟΣ ΑΛΛΑΓΗΣ ΜΗΝΑ
        if (currentDate.getMonthValue() != oldMonth) {
            System.out.println("--- ΝΕΟΣ ΜΗΝΑΣ ---");
            
            //Α.Απόδοση Τόκων σε Ταμιευτηρίου (Η Τράπεζα πληρώνει τους Πελάτες)
            applySavingsInterest();
            
            //Β.Χρέωση Εξόδων σε Επιχειρήσεις (Οι Επιχειρήσεις πληρώνουν την Τράπεζα)
            applyBusinessMonthlyFees();
        }
    }

    

    //2.ΑΠΟΔΟΣΗ ΤΟΚΩΝ
    private void applySavingsInterest() {
        BankDataStore store = BankDataStore.getInstance();
        Account bankAcc = store.getCentralBankAccount();
        
        if (bankAcc == null) {
            System.err.println("Δεν βρέθηκε ο κεντρικός λογαριασμός της Τράπεζας!");
            return;
        }

        double totalInterestPaid = 0;
        int count = 0;

        for (Account acc : store.getAccounts()) {
            //Έλεγχος αν είναι Ταμιευτηρίου και έχει θετικό υπόλοιπο
            if (acc.getAccountType().equals("Ταμιευτηρίου") && acc.getBalance() > 0) {
                
                double interest = acc.getBalance() * SAVINGS_INTEREST_RATE;
                
                //1.Πίστωση στον Πελάτη
                acc.setBalance(acc.getBalance() + interest);
                acc.addTransaction(new Transaction.Builder(interest)
                        .setType("Τόκοι")
                        .setDescription("Απόδοση Τόκων (1.6%)")
                        .setDate(currentDate)
                        .setSender("Bank OfTuc")
                        .setReceiver(acc.getIban())
                        .setBalanceAfter(acc.getBalance())
                        .build());

                //2.Χρέωση στην Τράπεζα
                bankAcc.setBalance(bankAcc.getBalance() - interest);
                bankAcc.addTransaction(new Transaction.Builder(interest)
                        .setType("Πληρωμή Τόκων")
                        .setDescription("Τόκοι προς " + acc.getIban())
                        .setDate(currentDate)
                        .setBalanceAfter(bankAcc.getBalance())
                        .build());

                totalInterestPaid += interest;
                count++;
            }
        }

        if (count > 0) {
            JOptionPane.showMessageDialog(null, 
                "Απόδοση Τόκων Μήνα (" + currentDate.getMonth() + ")\n" +
                "Πιστώθηκαν τόκοι σε " + count + " λογαριασμούς Ταμιευτηρίου.\n" +
                "Συνολικό κόστος για την τράπεζα: " + String.format("%.2f", totalInterestPaid) + "€");
        }
    }

    //3.ΧΡΕΩΣΗ ΕΠΙΧΕΙΡΗΣΕΩΝ
    private void applyBusinessMonthlyFees() {
        BankDataStore store = BankDataStore.getInstance();
        Account bankAcc = store.getCentralBankAccount();
        double totalProfit = 0;
        int count = 0;

        for (User u : store.getUsers()) {
            if ((u.getRole() == Role.BUSINESS || u.getRole() == Role.ADMIN) && !u.getFullName().equals("Bank OfTuc")) {
                for (Account acc : store.getAccounts()) {
                    if (acc.getOwnerName().equals(u.getFullName())) {
                        double oldBal = acc.getBalance();
                        acc.setBalance(oldBal - BUSINESS_MONTHLY_FEE);
                        
                        acc.addTransaction(new Transaction.Builder(BUSINESS_MONTHLY_FEE)
                                .setType("Χρέωση")
                                .setDescription("Μηνιαίο Τέλος Διατήρησης")
                                .setDate(currentDate)
                                .setBalanceAfter(acc.getBalance())
                                .build());
                        
                        totalProfit += BUSINESS_MONTHLY_FEE;
                        count++;
                    }
                }
            }
        }

        if (bankAcc != null && totalProfit > 0) {
            bankAcc.setBalance(bankAcc.getBalance() + totalProfit);
            bankAcc.addTransaction(new Transaction.Builder(totalProfit)
                    .setType("Είσπραξη")
                    .setDescription("Συνολικά Μηνιαία Τέλη")
                    .setDate(currentDate)
                    .setBalanceAfter(bankAcc.getBalance())
                    .build());
        }
        
        if (count > 0) {
            JOptionPane.showMessageDialog(null, "Χρεώθηκαν τέλη σε " + count + " εταιρικούς λογαριασμούς.\nΚέρδος: " + totalProfit + "€");
        }
    }
    
    private void executeGlobalStandingOrders() {
        List<StandingOrder> orders = BankDataStore.getInstance().getStandingOrders();
        Account bankOfTuc = BankDataStore.getInstance().getCentralBankAccount(); // Ο Λογαριασμός της Τράπεζας

        for (StandingOrder so : orders) {
            //1.Έλεγχος αν η εντολή είναι ενεργή
            if (!so.isActive()) continue;

            //2.Έλεγχος Ημερομηνίας: Πρέπει να έχει περάσει η ημερομηνία έναρξης 
            // ΚΑΙ να έχει φτάσει η ημερομηνία επόμενης εκτέλεσης.
            if (!so.getNextExecutionDate().isAfter(currentDate) && !so.getStartDate().isAfter(currentDate)) {
                 
                 //Βρίσκουμε τον λογαριασμό του πελάτη
                 Account sourceAcc = BankDataStore.getInstance().getAccountByIban(so.getSourceIban());
                 
                 if (sourceAcc != null) {
                     // Υπολογισμός Συνόλου (Ποσό + Προμήθεια)
                     double fee = BankDataStore.FEE_STANDING_ORDER;
                     double totalDeduction = so.getAmount() + fee;
                     
                     //3.Έλεγχος Υπολοίπου
                     if (sourceAcc.getBalance() >= totalDeduction) {
                         //ΕΠΙΤΥΧΙΑ
                         
                         //Α.Χρέωση Πελάτη (Ποσό + Προμήθεια)
                         sourceAcc.withdraw(totalDeduction);
                         
                         //Β.Πίστωση Παραλήπτη (Αν είναι εσωτερικός λογαριασμός IBAN)
                         //Αν το targetIban είναι κενό ή εξωτερικό, υποθέτουμε ότι τα λεφτά φεύγουν εκτός τράπεζας.
                         Account targetAcc = BankDataStore.getInstance().getAccountByIban(so.getTarget());
                         if (targetAcc != null) {
                             targetAcc.deposit(so.getAmount()); // Ο παραλήπτης παίρνει το καθαρό ποσό
                             
                             //Καταγραφή εισερχόμενης στον παραλήπτη
                             targetAcc.addTransaction(new Transaction.Builder(so.getAmount())
                                     .setType("\"Εισερχόμενη Πάγια εντολή")
                                     .setSender(sourceAcc.getIban())
                                     .setReceiver(so.getTarget())
                                     .setDescription(so.getDescription() +" από "+ sourceAcc.getOwnerName())
                                     .setDate(currentDate)
                                     .setBalanceAfter(targetAcc.getBalance())
                                     .build());
                         }
                         
                         
                         	model.Bill paidBill = BankDataStore.getInstance().getBillByRf(so.getTarget());
                         
                         if (paidBill != null) {
                             //Διαγραφή από τη λίστα εκκρεμών λογαριασμών
                             BankDataStore.getInstance().getPendingBills().remove(paidBill);
                             
                             //Αποθήκευση του bills.csv για να μη ξαναεμφανιστεί
                             service.CsvService service = new service.CsvService();
                             service.saveBills(BankDataStore.getInstance().getPendingBills());
                             
                             System.out.println("Ο λογαριασμός " + paidBill.getDescription() + " εξοφλήθηκε μέσω πάγιας!");
                         }
                         
                         //Γ.Κατάθεση Προμήθειας στην Τράπεζα
                         if (fee > 0) {
                             bankOfTuc.deposit(fee);
                             
                             //Καταγραφή εσόδου τράπεζας (Προαιρετικό)
                             bankOfTuc.addTransaction(new Transaction.Builder(fee)
                                     .setType("Commission Revenue")
                                     .setDescription("Προμήθεια Πάγιας: " + so.getSourceIban())
                                     .setDate(currentDate)
                                     .setBalanceAfter(bankOfTuc.getBalance())
                                     .build());
                         }
                         
                         //Δ.Καταγραφή Συναλλαγής στο Ιστορικό του Πελάτη
                         Transaction t = new Transaction.Builder(totalDeduction) // Ο πελάτης βλέπει τη συνολική χρέωση
                             .setType("Πάγια εντολή")
                             .setFee(fee)
                             .setDescription(so.getDescription() +" πάγια εντολή προς " + so.getTarget())
                             .setDate(currentDate)
                             .setSender(sourceAcc.getIban())
                             .setReceiver(so.getTarget())
                             .setBalanceAfter(sourceAcc.getBalance())
                             .build();
                         sourceAcc.addTransaction(t);

                     } else {
                         //ΑΠΟΤΥΧΙΑ (Ανεπαρκές Υπόλοιπο)
                         
                         //Ειδοποίηση Χρήστη (Bridge Pattern)
                         //Πρέπει να βρούμε τον User object από το όνομα στον λογαριασμό ή το IBAN
                         //Χρησιμοποιούμε τη μέθοδο που φτιάξαμε νωρίτερα στο BankDataStore
                         User owner = BankDataStore.getInstance().getUserByFullName(sourceAcc.getOwnerName());
                         
                         if (owner != null && owner.isNotifyStandingOrderFailed()) {
                             MessageSender sender = new EmailSender();
                             Notification notif = new StandingOrderNotification(sender);
                             
                             String msg = "Η πάγια εντολή προς <b>" + so.getTarget() + "</b><br>" +
                                          "ποσού <b>" + so.getAmount() + "€</b> απέτυχε λόγω ανεπαρκούς υπολοίπου.";
                             
                             notif.send(msg);
                         }
                     }
                 }
                 
                 //4.Ενημέρωση της επόμενης ημερομηνίας εκτέλεσης (είτε πέτυχε είτε απέτυχε)
                 //Προσθέτουμε τις ημέρες συχνότητας (π.χ. 30 μέρες)
                 so.setNextExecutionDate(currentDate.plusDays(so.getFrequencyDays()));
            }
        }
        
        //5.Αποθήκευση όλων των αλλαγών (υπόλοιπα, transactions, ημερομηνίες so)
        BankDataStore.getInstance().saveAllData();
    }
    
    
    private void checkBills() {
        List<Bill> bills = BankDataStore.getInstance().getPendingBills();
        
        User loggedUser = BankDataStore.getInstance().getLoggedUser(); //Ποιος βλέπει την οθόνη;

        //Αν δεν υπάρχει συνδεδεμένος χρήστης, δεν δείχνουμε popups
        if (loggedUser == null) return;
        
        for (Bill b : bills) {
            //Έλεγχος αν λήγει ΣΗΜΕΡΑ ή ΑΥΡΙΟ
            if (b.getDueDate().equals(currentDate) || b.getDueDate().equals(currentDate.plusDays(1))) {
                
                //1.Βρίσκουμε τον χρήστη βάσει του ΑΦΜ που έχει ο λογαριασμός
                User debtor = BankDataStore.getInstance().getUserByAfm(b.getOwnerAfm());
                
                if (debtor != null && debtor.getUsername().equals(loggedUser.getUsername())) {
                
                //2.Αν ο χρήστης υπάρχει ΚΑΙ έχει ενεργοποιήσει τις ειδοποιήσεις
                if (debtor != null && debtor.isNotifyBillExpiring()) {
                    
                    MessageSender sender = new SmsSender();
                    Notification notif = new BillNotification(sender);
                    
                    String msg = "Αγαπητέ πελάτη (ΑΦΜ: " + debtor.getAfm() + "),<br>" +
                                 "Ο λογαριασμός <b>" + b.getProvider() + "</b> ποσού <b>" + b.getAmount() + "€</b><br>" +
                                 "λήγει στις " + b.getDueDate() + ".";
                    
                    notif.send(msg);
                }
            }
           }
        }
    }
    
    private void checkUpcomingStandingOrders() {
        List<StandingOrder> orders = BankDataStore.getInstance().getStandingOrders();
        User loggedUser = BankDataStore.getInstance().getLoggedUser(); //Ποιος βλέπει την οθόνη;

        //Αν δεν υπάρχει συνδεδεμένος χρήστης, δεν δείχνουμε popups
        if (loggedUser == null) return;

        for (StandingOrder so : orders) {
            if (!so.isActive()) continue;

            //Έλεγχος: Είναι η ημερομηνία εκτέλεσης ΑΥΡΙΟ;
            if (so.getNextExecutionDate().equals(currentDate.plusDays(1))) {
                
                //Βρίσκουμε τον ιδιοκτήτη της πάγιας
                Account sourceAcc = BankDataStore.getInstance().getAccountByIban(so.getSourceIban());
                if (sourceAcc != null) {
                    User owner = BankDataStore.getInstance().getUserByFullName(sourceAcc.getOwnerName());

                    //Στέλνουμε ειδοποίηση ΜΟΝΟ αν ο ιδιοκτήτης είναι ο συνδεδεμένος χρήστης
                    if (owner != null && owner.getUsername().equals(loggedUser.getUsername())) {
                        
                        //Χρησιμοποιούμε την προτίμηση StandingOrderFailed ή θεωρούμε ότι θέλει ενημέρωση
                        if (owner.isNotifyStandingOrderFailed()) {
                            MessageSender sender = new EmailSender(); // ή SmsSender ανάλογα τις ρυθμίσεις
                            Notification notif = new StandingOrderNotification(sender);
                            
                            String msg = "Υπενθύμιση: Η πάγια εντολή προς <b>" + so.getTarget() + "</b><br>" +
                                         "ποσού <b>" + so.getAmount() + "€</b> θα εκτελεστεί αύριο (" + so.getNextExecutionDate() + ").";
                            
                            notif.send(msg);
                        }
                    }
                }
            }
        }
    }
    
    
}