package view.panels;

import data.BankDataStore;
import model.entities.Account;
import service.bridge.InternalProtocol;
import service.bridge.SepaProtocol;
import service.bridge.SwiftProtocol;
import service.command.TransferCommand;
import service.notification.Notification;
import service.notification.TransactionNotification;
import service.notification.sender.EmailSender;
import service.notification.sender.MessageSender;
import service.strategy.InternalFeeStrategy;
import service.strategy.SepaFeeStrategy;
import service.strategy.SwiftFeeStrategy;
import utils.TimeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;

public class TransfersPanel extends JPanel implements Refreshable {

    private JTabbedPane tabbedPane;
    
    // Components για Internal
    private JComboBox<String> intSourceBox;
    private JTextField intTargetIbanF, intAmountF, intDescF;
    
    // Components για SEPA
    private JComboBox<String> sepaSourceBox;
    private JTextField sepaIbanF, sepaNameF, sepaBicF, sepaBankF, sepaAmountF, sepaDateF;
    private JComboBox<String> sepaChargesBox;

    // Components για SWIFT
    private JComboBox<String> swiftSourceBox;
    private JTextField swiftAccF, swiftNameF, swiftAddrF, swiftBankF, swiftCodeF, swiftCountryF, swiftAmountF;
    private JComboBox<String> swiftChargesBox;
    private JComboBox<String> swiftCurrencyBox;
    
    private model.entities.User currentUser;
    private static final DecimalFormat df = new DecimalFormat("0.00");
    private static final String[] CHARGE_OPTIONS = {"SHA", "OUR"};

    public TransfersPanel(model.entities.User user) {
        this.currentUser = user;
        
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

    // 1. INTERNAL TRANSFER UI
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

    // 2. SEPA TRANSFER UI
    private JPanel createSepaPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 10, 5, 10); g.fill = GridBagConstraints.HORIZONTAL; g.anchor = GridBagConstraints.WEST;

        g.gridx=0; g.gridy=0; p.add(new JLabel("Από Λογαριασμό:"), g);
        sepaSourceBox = new JComboBox<>(); sepaSourceBox.setPreferredSize(new Dimension(250, 30));
        g.gridx=1; p.add(sepaSourceBox, g);

        addRow(p, g, 1, "Όνομα Παραλήπτη:", sepaNameF = new JTextField());
        addRow(p, g, 2, "* IBAN Παραλήπτη:", sepaIbanF = new JTextField());
        addRow(p, g, 3, "* BIC Τράπεζας:", sepaBicF = new JTextField());
        addRow(p, g, 4, "Όνομα Τράπεζας:", sepaBankF = new JTextField());
        addRow(p, g, 5, "* Ποσό (€):", sepaAmountF = new JTextField());
        addRow(p, g, 6, "* Ημ/νία (YYYY-MM-DD):", sepaDateF = new JTextField(LocalDate.now().toString()));
        
        g.gridx=0; g.gridy=7; p.add(new JLabel("* Έξοδα (Charges):"), g);
        sepaChargesBox = new JComboBox<>(CHARGE_OPTIONS); sepaChargesBox.setPreferredSize(new Dimension(250, 30));
        g.gridx=1; p.add(sepaChargesBox, g);

        JButton btn = new JButton("Αποστολή SEPA");
        btn.setBackground(new Color(0, 51, 102)); btn.setForeground(Color.WHITE);
        btn.addActionListener(e -> doSepaTransfer());
        g.gridx=1; g.gridy=8; p.add(btn, g);

        return p;
    }

    // 3. SWIFT TRANSFER UI
    private JPanel createSwiftPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 10, 5, 10); g.fill = GridBagConstraints.HORIZONTAL; g.anchor = GridBagConstraints.WEST;

        g.gridx=0; g.gridy=0; p.add(new JLabel("Από Λογαριασμό:"), g);
        swiftSourceBox = new JComboBox<>(); swiftSourceBox.setPreferredSize(new Dimension(250, 30));
        g.gridx=1; p.add(swiftSourceBox, g);

        addRow(p, g, 1, "Όνομα Παραλήπτη:", swiftNameF = new JTextField());
        addRow(p, g, 2, "Διεύθυνση:", swiftAddrF = new JTextField());
        addRow(p, g, 3, "* Αρ. Λογαριασμού:", swiftAccF = new JTextField());
        addRow(p, g, 4, "Όνομα Τράπεζας:", swiftBankF = new JTextField());
        addRow(p, g, 5, "* SWIFT Code:", swiftCodeF = new JTextField());
        addRow(p, g, 6, "Χώρα Τράπεζας:", swiftCountryF = new JTextField());
        addRow(p, g, 7, "* Ποσό:", swiftAmountF = new JTextField());

        g.gridx=0; g.gridy=8; p.add(new JLabel("* Νόμισμα:"), g);
        swiftCurrencyBox = new JComboBox<>(new String[]{"EUR", "USD", "GBP", "CHF"});
        swiftCurrencyBox.setPreferredSize(new Dimension(250, 30));
        g.gridx=1; p.add(swiftCurrencyBox, g);
        
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

    // --- LOGIC & BRIDGE IMPLEMENTATION ---

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
            String src = getSelectedIban(intSourceBox);
            String target = intTargetIbanF.getText();
            String desc = intDescF.getText();

            // BRIDGE: Χρήση του TransferCommand με InternalProtocol
            new TransferCommand(
                src, 
                target, 
                amt, 
                new InternalFeeStrategy(), // Strategy
                new InternalProtocol(),    // Bridge Protocol
                desc, 
                false
            ).execute();

            // Αποθήκευση και ενημέρωση UI
            BankDataStore.getInstance().saveAllData(); 
            sendTransactionNotification(amt, target);
            refresh();
            JOptionPane.showMessageDialog(this, "Επιτυχής Μεταφορά!");
            
        } catch (Exception e) { showError("Σφάλμα: " + e.getMessage()); }
    }

    private void doSepaTransfer() {
        if (currentUser.isLocked()) { showError("Ο λογαριασμός είναι κλειδωμένος."); return; }
        try {
            double amt = Double.parseDouble(sepaAmountF.getText());
            String src = getSelectedIban(sepaSourceBox);
            String targetIban = sepaIbanF.getText();
            String desc = "SEPA Transfer to " + sepaNameF.getText();

            // --- BRIDGE IMPLEMENTATION ---
            // Δημιουργία του κατάλληλου πρωτοκόλλου με τα στοιχεία από το GUI
            SepaProtocol sepaProtocol = new SepaProtocol(
                sepaNameF.getText(),
                sepaBicF.getText(),
                sepaBankF.getText(),
                sepaDateF.getText(),
                (String) sepaChargesBox.getSelectedItem()
            );

            // Εκτέλεση μέσω Command που χρησιμοποιεί το Protocol
            new TransferCommand(
                src, 
                targetIban, 
                amt, 
                new SepaFeeStrategy(), // Strategy για χρέωση
                sepaProtocol,          // Bridge Protocol για επικοινωνία
                desc, 
                false
            ).execute();

            BankDataStore.getInstance().saveAllData();
            sendTransactionNotification(amt, "SEPA: " + targetIban);
            refresh();
            JOptionPane.showMessageDialog(this, "Η εντολή SEPA καταχωρήθηκε επιτυχώς!");

        } catch (Exception e) { showError("Σφάλμα SEPA: " + e.getMessage()); }
    }

    private void doSwiftTransfer() {
        if (currentUser.isLocked()) { showError("Ο λογαριασμός είναι κλειδωμένος."); return; }
        try {
            double amt = Double.parseDouble(swiftAmountF.getText());
            String src = getSelectedIban(swiftSourceBox);
            String targetAcc = swiftAccF.getText();
            String currency = (String) swiftCurrencyBox.getSelectedItem();
            String desc = "SWIFT (" + currency + ") to " + swiftNameF.getText();

            // --- BRIDGE IMPLEMENTATION ---
            SwiftProtocol swiftProtocol = new SwiftProtocol(
                swiftNameF.getText(),
                swiftAddrF.getText(),
                swiftBankF.getText(),
                swiftCodeF.getText(),
                swiftCountryF.getText(),
                currency,
                (String) swiftChargesBox.getSelectedItem()
            );

            new TransferCommand(
                src, 
                targetAcc, 
                amt, 
                new SwiftFeeStrategy(), // Strategy
                swiftProtocol,          // Bridge Protocol
                desc, 
                false
            ).execute();

            BankDataStore.getInstance().saveAllData();
            sendTransactionNotification(amt, "SWIFT: " + targetAcc);
            refresh();
            JOptionPane.showMessageDialog(this, "Η εντολή SWIFT καταχωρήθηκε επιτυχώς!");

        } catch (Exception e) { showError("Σφάλμα SWIFT: " + e.getMessage()); }
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