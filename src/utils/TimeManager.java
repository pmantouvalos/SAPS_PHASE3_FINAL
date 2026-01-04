package utils;

import data.BankDataStore;
import model.entities.*;
import model.enums.TransactionType; // Import Enum
import service.factory.TransactionFactory; // Import Factory
import service.notification.*;
import service.notification.sender.*;

import javax.swing.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TimeManager {
    
    // --- SINGLETON PATTERN ---
    private static TimeManager instance;
    private LocalDate currentDate;
    
    // Σταθερές Χρεώσεων & Επιτοκίων
    private static final double BUSINESS_MONTHLY_FEE = 5.00;
    private static final double SAVINGS_INTEREST_RATE = 0.016; 
    private static final double FEE_STANDING_ORDER = 0.50; 

    private TimeManager() { 
        this.currentDate = LocalDate.now(); 
    }

    public static synchronized TimeManager getInstance() {
        if (instance == null) instance = new TimeManager();
        return instance;
    }

    // --- TIME MANAGEMENT ---
    
    public void setDate(LocalDate date) {
        this.currentDate = date;
        System.out.println("System Date set to: " + getFormattedDate());
    }
    
    public LocalDate getDate() { return currentDate; }
    
    public String getFormattedDate() { 
        return currentDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")); 
    }

    // --- ΚΕΝΤΡΙΚΗ ΜΕΘΟΔΟΣ ΑΛΛΑΓΗΣ ΧΡΟΝΟΥ ---
    public void advanceTime() {
        int oldMonth = currentDate.getMonthValue();
        
        // 1. Επόμενη Μέρα
        currentDate = currentDate.plusDays(1);
        System.out.println(">>> ΝΕΑ ΗΜΕΡΟΜΗΝΙΑ: " + getFormattedDate());

        // 2. Εκτέλεση Καθημερινών Εργασιών
        executeGlobalStandingOrders();
        checkBills();
        checkUpcomingStandingOrders();

        // 3. Έλεγχος Αλλαγής Μήνα
        if (currentDate.getMonthValue() != oldMonth) {
            System.out.println("--- ΝΕΟΣ ΜΗΝΑΣ ---");
            applySavingsInterest();
            applyBusinessMonthlyFees();
        }
    }

    // 2. ΑΠΟΔΟΣΗ ΤΟΚΩΝ (Χρήση Factory)
    private void applySavingsInterest() {
        BankDataStore store = BankDataStore.getInstance();
        Account bankAcc = store.getCentralBankAccount();
        
        if (bankAcc == null) {
            System.err.println("Warning: Δεν βρέθηκε ο κεντρικός λογαριασμός της Τράπεζας!");
            return;
        }

        double totalInterestPaid = 0;
        int count = 0;

        for (Account acc : store.getAccounts()) {
            // Έλεγχος: Είναι τύπου Savings (Ταμιευτηρίου) και έχει θετικό υπόλοιπο;
            // Χρησιμοποιούμε τη μέθοδο του Factory/Enum ή instanceof
            if (acc instanceof SavingsAccount && acc.getBalance() > 0) {
                
                double interest = acc.getBalance() * SAVINGS_INTEREST_RATE;
                
                // A. Πίστωση στον Πελάτη
                acc.deposit(interest);
                
                Transaction tClient = TransactionFactory.createTransaction(
                    interest,
                    TransactionType.DEPOSIT, // Τύπος Enum
                    "Απόδοση Τόκων (1.6%)",
                    "Bank OfTuc",
                    acc.getIban(),
                    0.0,
                    acc.getBalance()
                );
                acc.addTransaction(tClient);

                // B. Χρέωση στην Τράπεζα
                bankAcc.withdraw(interest);
                
                Transaction tBank = TransactionFactory.createTransaction(
                    interest,
                    TransactionType.PAYMENT, // Η τράπεζα πληρώνει
                    "Πληρωμή Τόκων προς " + acc.getIban(),
                    "Bank OfTuc",
                    acc.getIban(),
                    0.0,
                    bankAcc.getBalance()
                );
                bankAcc.addTransaction(tBank);

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

    // 3. ΧΡΕΩΣΗ ΕΠΙΧΕΙΡΗΣΕΩΝ (Χρήση Factory)
    private void applyBusinessMonthlyFees() {
        BankDataStore store = BankDataStore.getInstance();
        Account bankAcc = store.getCentralBankAccount();
        double totalProfit = 0;
        int count = 0;

        for (Account acc : store.getAccounts()) {
            // Έλεγχος αν είναι BusinessAccount
            if (acc instanceof BusinessAccount) {
                // Δεν χρεώνουμε τον ίδιο τον λογαριασμό της τράπεζας
                if (acc.getIban().equals(bankAcc.getIban())) continue;

                acc.withdraw(BUSINESS_MONTHLY_FEE);
                
                Transaction t = TransactionFactory.createTransaction(
                    BUSINESS_MONTHLY_FEE,
                    TransactionType.PAYMENT,
                    "Μηνιαίο Τέλος Διατήρησης",
                    acc.getIban(),
                    "Bank OfTuc",
                    0.0,
                    acc.getBalance()
                );
                acc.addTransaction(t);
                        
                totalProfit += BUSINESS_MONTHLY_FEE;
                count++;
            }
        }

        if (bankAcc != null && totalProfit > 0) {
            bankAcc.deposit(totalProfit);
            Transaction tBank = TransactionFactory.createTransaction(
                totalProfit,
                TransactionType.DEPOSIT,
                "Είσπραξη Μηνιαίων Τελών",
                "System",
                "Bank OfTuc",
                0.0,
                bankAcc.getBalance()
            );
            bankAcc.addTransaction(tBank);
        }
        
        if (count > 0) {
            JOptionPane.showMessageDialog(null, "Χρεώθηκαν τέλη σε " + count + " εταιρικούς λογαριασμούς.\nΚέρδος: " + totalProfit + "€");
        }
    }
    
    // 4. ΕΚΤΕΛΕΣΗ ΠΑΓΙΩΝ ΕΝΤΟΛΩΝ (Διορθωμένο)
    private void executeGlobalStandingOrders() {
        List<StandingOrder> orders = BankDataStore.getInstance().getStandingOrders();
        Account bankOfTuc = BankDataStore.getInstance().getCentralBankAccount();

        for (StandingOrder so : orders) {
            if (!so.isActive()) continue;

            // Έλεγχος Ημερομηνίας
            if (!so.getNextExecutionDate().isAfter(currentDate) && !so.getStartDate().isAfter(currentDate)) {
                 
                 Account sourceAcc = BankDataStore.getInstance().getAccountByIban(so.getSourceIban());
                 
                 if (sourceAcc != null) {
                     double fee = FEE_STANDING_ORDER;
                     double totalDeduction = so.getAmount() + fee;
                     
                     if (sourceAcc.getBalance() >= totalDeduction) {
                         // A. Χρέωση
                         sourceAcc.withdraw(totalDeduction);
                         
                         // B. Πίστωση (αν είναι εσωτερικός)
                         Account targetAcc = BankDataStore.getInstance().getAccountByIban(so.getTarget());
                         if (targetAcc != null) {
                             targetAcc.deposit(so.getAmount());
                             
                             Transaction tIn = TransactionFactory.createTransaction(
                                 so.getAmount(),
                                 TransactionType.DEPOSIT,
                                 "Εισερχόμενη Πάγια: " + so.getDescription(),
                                 sourceAcc.getIban(),
                                 so.getTarget(),
                                 0.0,
                                 targetAcc.getBalance()
                             );
                             targetAcc.addTransaction(tIn);
                         }
                         
                         // Έλεγχος αν ήταν πληρωμή Bill
                         Bill paidBill = BankDataStore.getInstance().getBillByRf(so.getTarget());
                         if (paidBill != null) {
                             BankDataStore.getInstance().getPendingBills().remove(paidBill);
                             System.out.println("Ο λογαριασμός " + paidBill.getDescription() + " εξοφλήθηκε μέσω πάγιας!");
                         }
                         
                         // Γ. Προμήθεια
                         if (fee > 0 && bankOfTuc != null) {
                             bankOfTuc.deposit(fee);
                             // TransactionFactory...
                         }
                         
                         // Δ. Καταγραφή Χρέωσης
                         Transaction tOut = TransactionFactory.createTransaction(
                             so.getAmount(),
                             TransactionType.TRANSFER,
                             "Πάγια εντολή: " + so.getDescription(),
                             sourceAcc.getIban(),
                             so.getTarget(),
                             fee,
                             sourceAcc.getBalance()
                         );
                         sourceAcc.addTransaction(tOut);

                     } else {
                         // Αποτυχία λόγω υπολοίπου - Ειδοποίηση
                         User owner = BankDataStore.getInstance().getUserByFullName(sourceAcc.getOwnerName());
                         if (owner != null && owner.isNotifyStandingOrderFailed()) {
                             MessageSender sender = new EmailSender();
                             Notification notif = new StandingOrderNotification(sender);
                             notif.send("Η πάγια εντολή προς " + so.getTarget() + " απέτυχε.");
                         }
                     }
                 }
                 // Ενημέρωση επόμενης ημερομηνίας
                 so.setNextExecutionDate(currentDate.plusDays(so.getFrequencyDays()));
            }
        }
        BankDataStore.getInstance().saveAllData();
    }
    
    private void checkBills() {
        List<Bill> bills = BankDataStore.getInstance().getPendingBills();
        User loggedUser = BankDataStore.getInstance().getLoggedUser(); 

        if (loggedUser == null) return;
        
        for (Bill b : bills) {
            if (b.getDueDate().equals(currentDate) || b.getDueDate().equals(currentDate.plusDays(1))) {
                User debtor = BankDataStore.getInstance().getUserByAfm(b.getOwnerAfm());
                if (debtor != null && debtor.getUsername().equals(loggedUser.getUsername())) {
                    if (debtor.isNotifyBillExpiring()) {
                        MessageSender sender = new SmsSender();
                        Notification notif = new BillNotification(sender);
                        notif.send("Ο λογαριασμός " + b.getProvider() + " λήγει σύντομα.");
                    }
                }
            }
        }
    }
    
    private void checkUpcomingStandingOrders() {
        List<StandingOrder> orders = BankDataStore.getInstance().getStandingOrders();
        User loggedUser = BankDataStore.getInstance().getLoggedUser();

        if (loggedUser == null) return;

        for (StandingOrder so : orders) {
            if (!so.isActive()) continue;
            if (so.getNextExecutionDate().equals(currentDate.plusDays(1))) {
                Account sourceAcc = BankDataStore.getInstance().getAccountByIban(so.getSourceIban());
                if (sourceAcc != null) {
                    User owner = BankDataStore.getInstance().getUserByFullName(sourceAcc.getOwnerName());
                    if (owner != null && owner.getUsername().equals(loggedUser.getUsername())) {
                        if (owner.isNotifyStandingOrderFailed()) { // Χρήση ως γενική ειδοποίηση πάγιας
                            MessageSender sender = new EmailSender(); 
                            Notification notif = new StandingOrderNotification(sender);
                            notif.send("Η πάγια εντολή προς " + so.getTarget() + " θα εκτελεστεί αύριο.");
                        }
                    }
                }
            }
        }
    }
}