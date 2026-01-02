package gui.panels;

import commands.TransferCommand;
import model.Account;
import model.Transaction;
import service.BankDataStore;
import service.BankingApiService;
import utils.TimeManager;

// Imports για το Bridge Pattern (Ειδοποιήσεις)
import bridge.EmailSender;
import bridge.MessageSender;
import bridge.Notification;
import bridge.TransactionNotification;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.LocalDate; // Χρειάζεται για την προεπιλεγμένη ημερομηνία
import java.util.List;

public class TransfersPanel extends JPanel implements Refreshable {

    private JTabbedPane tabbedPane;
    
    //Components για Internal
    private JComboBox<String> intSourceBox;
    private JTextField intTargetIbanF, intAmountF, intDescF;
    
    //Components για SEPA
    private JComboBox<String> sepaSourceBox;
    private JTextField sepaIbanF, sepaNameF, sepaBicF, sepaBankF, sepaAmountF, sepaDateF;
    private JComboBox<String> sepaChargesBox; //Επιλογή SHA/OUR

    //Components για SWIFT
    private JComboBox<String> swiftSourceBox;
    private JTextField swiftAccF, swiftNameF, swiftAddrF, swiftBankF, swiftCodeF, swiftCountryF, swiftAmountF;
    private JComboBox<String> swiftChargesBox; //Επιλογή SHA/OUR
    private JComboBox<String> swiftCurrencyBox;
    
    private model.User currentUser;
    private BankingApiService apiService;
    private static final DecimalFormat df = new DecimalFormat("0.00");
    private static final String[] CHARGE_OPTIONS = {"SHA", "OUR"}; //Επιλογές χρέωσης

    public TransfersPanel(model.User user) {
        this.currentUser = user;
        this.apiService = new BankingApiService();
        
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Μεταφορά Χρημάτων");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        add(title, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        tabbedPane.addTab("Ενδοτραπεζική", createInternalPanel());
        tabbedPane.addTab("SEPA (Ευρώπη)", createSepaPanel());
        tabbedPane.addTab("SWIFT (Διεθνές)", createSwiftPanel());

        add(tabbedPane, BorderLayout.CENTER);
        refresh();
    }

    //1.INTERNAL TRANSFER UI
    private JPanel createInternalPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 10, 10, 10); g.fill = GridBagConstraints.HORIZONTAL; g.anchor = GridBagConstraints.WEST;

        g.gridx=0; g.gridy=0; p.add(new JLabel("Από Λογαριασμό:"), g);
        intSourceBox = new JComboBox<>(); intSourceBox.setPreferredSize(new Dimension(300, 35));
        g.gridx=1; p.add(intSourceBox, g);

        g.gridx=0; g.gridy=1; p.add(new JLabel("IBAN Παραλήπτη:"), g);
        intTargetIbanF = new JTextField(); intTargetIbanF.setPreferredSize(new Dimension(300, 35));
        g.gridx=1; p.add(intTargetIbanF, g);

        g.gridx=0; g.gridy=2; p.add(new JLabel("Ποσό (€):"), g);
        intAmountF = new JTextField(); intAmountF.setPreferredSize(new Dimension(300, 35));
        g.gridx=1; p.add(intAmountF, g);

        g.gridx=0; g.gridy=3; p.add(new JLabel("Αιτιολογία:"), g);
        intDescF = new JTextField(); intDescF.setPreferredSize(new Dimension(300, 35));
        g.gridx=1; p.add(intDescF, g);

        JButton btn = new JButton("Εκτέλεση");
        btn.setBackground(new Color(40, 45, 55)); btn.setForeground(Color.WHITE);
        btn.addActionListener(e -> doInternalTransfer());
        g.gridx=1; g.gridy=4; p.add(btn, g);
        
        return p;
    }

    //2.SEPA TRANSFER UI
    private JPanel createSepaPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 10, 5, 10); g.fill = GridBagConstraints.HORIZONTAL; g.anchor = GridBagConstraints.WEST;

        //Source
        g.gridx=0; g.gridy=0; p.add(new JLabel("Από Λογαριασμό:"), g);
        sepaSourceBox = new JComboBox<>(); sepaSourceBox.setPreferredSize(new Dimension(250, 30));
        g.gridx=1; p.add(sepaSourceBox, g);

        //Fields
        addRow(p, g, 1, "Όνομα Παραλήπτη:", sepaNameF = new JTextField());
        addRow(p, g, 2, "* IBAN Παραλήπτη:", sepaIbanF = new JTextField());
        addRow(p, g, 3, "* BIC Τράπεζας:", sepaBicF = new JTextField());
        addRow(p, g, 4, "Όνομα Τράπεζας:", sepaBankF = new JTextField());
        addRow(p, g, 5, "* Ποσό (€):", sepaAmountF = new JTextField());
        
        //Ημερομηνία Εκτέλεσης (Default: Σήμερα)
        addRow(p, g, 6, "* Ημ/νία (YYYY-MM-DD):", sepaDateF = new JTextField(LocalDate.now().toString()));
        
        //Έξοδα (Charges)
        g.gridx=0; g.gridy=7; p.add(new JLabel("* Έξοδα (Charges):"), g);
        sepaChargesBox = new JComboBox<>(CHARGE_OPTIONS); sepaChargesBox.setPreferredSize(new Dimension(250, 30));
        g.gridx=1; p.add(sepaChargesBox, g);

        JButton btn = new JButton("Αποστολή SEPA");
        btn.setBackground(new Color(0, 51, 102)); btn.setForeground(Color.WHITE);
        btn.addActionListener(e -> doSepaTransfer());
        g.gridx=1; g.gridy=8; p.add(btn, g); // Adjusted Y

        return p;
    }

    //3.SWIFT TRANSFER UI
    private JPanel createSwiftPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 10, 5, 10); g.fill = GridBagConstraints.HORIZONTAL; g.anchor = GridBagConstraints.WEST;

        //Source
        g.gridx=0; g.gridy=0; p.add(new JLabel("Από Λογαριασμό:"), g);
        swiftSourceBox = new JComboBox<>(); swiftSourceBox.setPreferredSize(new Dimension(250, 30));
        g.gridx=1; p.add(swiftSourceBox, g);

        //Fields
        addRow(p, g, 1, "Όνομα Παραλήπτη:", swiftNameF = new JTextField());
        addRow(p, g, 2, "Διεύθυνση:", swiftAddrF = new JTextField());
        addRow(p, g, 3, "* Αρ. Λογαριασμού:", swiftAccF = new JTextField());
        addRow(p, g, 4, "Όνομα Τράπεζας:", swiftBankF = new JTextField());
        addRow(p, g, 5, "* SWIFT Code:", swiftCodeF = new JTextField());
        addRow(p, g, 6, "Χώρα Τράπεζας:", swiftCountryF = new JTextField());
        addRow(p, g, 7, "* Ποσό:", swiftAmountF = new JTextField());

        //Currency
        g.gridx=0; g.gridy=8; p.add(new JLabel("* Νόμισμα:"), g);
        swiftCurrencyBox = new JComboBox<>(new String[]{"EUR", "USD", "GBP", "CHF"});
        swiftCurrencyBox.setPreferredSize(new Dimension(250, 30));
        g.gridx=1; p.add(swiftCurrencyBox, g);
        
        //Charges
        g.gridx=0; g.gridy=9; p.add(new JLabel("* Μοντέλο Χρέωσης:"), g);
        swiftChargesBox = new JComboBox<>(new String[]{"SHA", "OUR"}); 
        swiftChargesBox.setPreferredSize(new Dimension(250, 30));
        g.gridx=1; p.add(swiftChargesBox, g);

        JButton btn = new JButton("Αποστολή SWIFT");
        btn.setBackground(new Color(102, 0, 0)); btn.setForeground(Color.WHITE);
        btn.addActionListener(e -> doSwiftTransfer());
        g.gridx=1; g.gridy=10; p.add(btn, g);

        return p;
    }
    
  

    private void addRow(JPanel p, GridBagConstraints g, int y, String label, JTextField field) {
        g.gridx=0; g.gridy=y; p.add(new JLabel(label), g);
        field.setPreferredSize(new Dimension(250, 30));
        g.gridx=1; p.add(field, g);
    }

    //LOGIC

    private void sendTransactionNotification(double amount, String target) {
        if (currentUser.isNotifyTransaction()) {
            MessageSender sender = new EmailSender();
            Notification notif = new TransactionNotification(sender);
            String msg = "Εκτελέστηκε μεταφορά ποσού <b>" + df.format(amount) + "€</b><br>" +
                         "Προς: " + target + "<br>Ημερομηνία: " + TimeManager.getInstance().getFormattedDate();
            notif.send(msg);
        }
    }
    
    private void doInternalTransfer() {
        if (currentUser.isLocked()) { showError("Ο λογαριασμός είναι κλειδωμένος."); return; }
        try {
        	double amt = Double.parseDouble(intAmountF.getText());
            //Υπολογισμός συνολικού κόστους
            double totalCost = amt + BankDataStore.FEE_INTERNAL; 
            
            String src = getSelectedIban(intSourceBox);
            if(checkLimitsAndBalance(src, totalCost)) {
                new TransferCommand(src, intTargetIbanF.getText(), amt, 0.0, intDescF.getText(), false).execute();
                BankDataStore.getInstance().saveAllData(); 
                sendTransactionNotification(amt, intTargetIbanF.getText());
                refresh();
                JOptionPane.showMessageDialog(this, "Επιτυχής Μεταφορά!");
            }
        } catch (Exception e) { showError("Σφάλμα: " + e.getMessage()); }
    }

    private void doSepaTransfer() {
        if (currentUser.isLocked()) { showError("Ο λογαριασμός είναι κλειδωμένος."); return; }
        try {
            double amt = Double.parseDouble(sepaAmountF.getText());
            String src = getSelectedIban(sepaSourceBox);
            
            if(checkLimitsAndBalance(src, amt+ BankDataStore.FEE_SEPA)) {
                // Ανάκτηση των νέων πεδίων
                String date = sepaDateF.getText();
                String charges = (String) sepaChargesBox.getSelectedItem();
                
                // Κλήση της ενημερωμένης μεθόδου SEPA
                String response = apiService.sendSepaTransfer(
                        amt, sepaNameF.getText(), sepaIbanF.getText(), 
                        sepaBicF.getText(), sepaBankF.getText(), 
                        date, charges); // Πέρασμα ημερομηνίας και χρέωσης
                        
                handleApiResponse(src, amt, "SEPA", response);
            }
        } catch (Exception e) { showError("Σφάλμα SEPA: " + e.getMessage()); }
    }

    private void doSwiftTransfer() {
        if (currentUser.isLocked()) { showError("Ο λογαριασμός είναι κλειδωμένος."); return; }
        try {
            double amt = Double.parseDouble(swiftAmountF.getText());
            String src = getSelectedIban(swiftSourceBox);
            
            if(checkLimitsAndBalance(src, amt+ BankDataStore.FEE_SWIFT)) {
                String charges = (String) swiftChargesBox.getSelectedItem();
                String currency = (String) swiftCurrencyBox.getSelectedItem();

                //Κλήση ΧΩΡΙΣ τα extra πεδία της ανταποκρίτριας
                String response = apiService.sendSwiftTransfer(
                        amt, currency, swiftNameF.getText(), swiftAddrF.getText(), 
                        swiftAccF.getText(), swiftBankF.getText(), 
                        swiftCodeF.getText(), swiftCountryF.getText(),
                        charges);
                        
                handleApiResponse(src, amt, "SWIFT (" + currency + ")", response);
            }
        } catch (Exception e) { showError("Σφάλμα SWIFT: " + e.getMessage()); }
    }

    private boolean checkLimitsAndBalance(String iban, double totalAmountNeeded) { // totalAmountNeeded = amount + fee
        Account acc = BankDataStore.getInstance().getAccountByIban(iban);
        if(acc == null) return false;
        
        if(acc.getBalance() < totalAmountNeeded) { 
            showError("Ανεπαρκές Υπόλοιπο (συμπεριλαμβανομένης προμήθειας)."); 
            return false; 
        }
        if(totalAmountNeeded > currentUser.getLimitTransfer()) { 
            showError("Υπέρβαση Ορίου Μεταφορών."); 
            return false; 
        }
        return true;
    }

    private void handleApiResponse(String srcIban, double amount, String type, String response) {
        if (response.startsWith("SUCCESS")) {
            
            //1.Υπολογισμός Προμήθειας
            double fee = 0.0;
            if (type.contains("SEPA")) fee = BankDataStore.FEE_SEPA;
            else if (type.contains("SWIFT")) fee = BankDataStore.FEE_SWIFT;
            
            double totalDeduction = amount + fee;

            //2.Αφαίρεση από τον Πελάτη
            Account userAcc = BankDataStore.getInstance().getAccountByIban(srcIban);
            if (userAcc.getBalance() < totalDeduction) {
                showError("Ανεπαρκές υπόλοιπο για την κάλυψη της προμήθειας.");
                return;
            }
            userAcc.withdraw(totalDeduction); 
            
            //3. ΚΑΤΑΘΕΣΗ ΠΡΟΜΗΘΕΙΑΣ ΣΤΗΝ BANK OF TUC
            if (fee > 0) {
                Account centralBankAcc = BankDataStore.getInstance().getCentralBankAccount();
                centralBankAcc.deposit(fee); //Τα λεφτά πάνε στην τράπεζα
                
                //Καταγραφή εσόδου για την τράπεζα (Προαιρετικό αλλά σωστό)
                Transaction bankIncome = new Transaction.Builder(fee)
                    .setType("Commission Revenue")
                    .setDescription("Προμήθεια από " + type + " (" + srcIban + ")")
                    .setDate(TimeManager.getInstance().getDate())
                    .setSender(srcIban)
                    .setReceiver("Bank of TUC")
                    .setBalanceAfter(centralBankAcc.getBalance())
                    .build();
                centralBankAcc.addTransaction(bankIncome);
            }

            //4.Καταγραφή Συναλλαγής Πελάτη
            Transaction t = new Transaction.Builder(amount)
                    .setType(type + " Transfer")
                    .setFee(fee)
                    .setDescription("Εξωτερική Μεταφορά: " + type + " (Fee: " + df.format(fee) + "€)")
                    .setDate(TimeManager.getInstance().getDate())
                    .setSender(srcIban)
                    .setReceiver("External")
                    .setBalanceAfter(userAcc.getBalance())
                    .build();
            userAcc.addTransaction(t);
            
            BankDataStore.getInstance().saveAllData(); 
            sendTransactionNotification(amount, "External (" + type + ")");
            
            refresh();
            JOptionPane.showMessageDialog(this, "Η συναλλαγή ολοκληρώθηκε.\nΠρομήθεια Τράπεζας: " + df.format(fee) + "€");
            
        } else {
            showError("Η συναλλαγή απέτυχε.\n" + response);
        }
    }

    private String getSelectedIban(JComboBox<String> box) {
        if(box.getSelectedItem() == null) return "";
        return ((String)box.getSelectedItem()).split(" ")[0];
    }
    
    private void showError(String msg) { JOptionPane.showMessageDialog(this, msg, "Σφάλμα", JOptionPane.ERROR_MESSAGE); }

    @Override
    public void refresh() {
        List<Account> myAccs = BankDataStore.getInstance().getAccountsForUser(currentUser);
        updateCombo(intSourceBox, myAccs);
        updateCombo(sepaSourceBox, myAccs);
        updateCombo(swiftSourceBox, myAccs);
    }
    
    private void updateCombo(JComboBox<String> box, List<Account> accounts) {
        box.removeAllItems();
        for(Account a : accounts) box.addItem(a.getIban() + " (" + df.format(a.getBalance()) + "€)");
    }
}