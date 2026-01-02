package gui.panels;

import model.Account;
import service.BankDataStore;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class SettingsPanel extends JPanel implements Refreshable {
    
    private CardLayout cardLayout;
    private JPanel mainContainer;
    private model.User user;
    
    //Sub-Panels
    private JPanel menuPanel;
    private NotificationsPanel notifPanel;
    private SecurityPanel securityPanel;

    public SettingsPanel(model.User user) {
        this.user = user;
        setLayout(new BorderLayout());
        
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        
        //Αρχικοποίηση
        initMenuPanel();
        notifPanel = new NotificationsPanel();
        securityPanel = new SecurityPanel();
        
        mainContainer.add(menuPanel, "MENU");
        mainContainer.add(notifPanel, "NOTIFICATIONS");
        mainContainer.add(securityPanel, "SECURITY");
        
        add(mainContainer, BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        securityPanel.refreshData(); //Ανανέωση labels
        notifPanel.loadToggles();    //Ανανέωση checkboxes
    }

    //1. MAIN MENU
    private void initMenuPanel() {
        menuPanel = new JPanel(new GridBagLayout());
        menuPanel.setBackground(Color.WHITE);
        
        JLabel title = new JLabel("Ρυθμίσεις");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        
        JButton btnNotif = createBigButton("Ειδοποιήσεις");
        btnNotif.addActionListener(e -> {
            notifPanel.loadToggles(); //Φόρτωση τρεχουσών ρυθμίσεων
            cardLayout.show(mainContainer, "NOTIFICATIONS");
        });
        
        JButton btnSec = createBigButton("Ασφάλεια και Όρια");
        btnSec.addActionListener(e -> {
            securityPanel.refreshData();
            cardLayout.show(mainContainer, "SECURITY");
        });
        
        JButton btnDel = new JButton("Διαγραφή Λογαριασμού Χρήστη");
        btnDel.setBackground(Color.RED);
        btnDel.setForeground(Color.WHITE);
        btnDel.addActionListener(e -> deleteUserLogic());

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(20, 20, 20, 20);
        g.gridx=0; g.gridy=0; g.gridwidth=2; menuPanel.add(title, g);
        
        g.gridwidth=1; g.gridy=1;
        menuPanel.add(btnNotif, g);
        g.gridx=1;
        menuPanel.add(btnSec, g);
        
        g.gridx=1; g.gridy=2; g.anchor = GridBagConstraints.SOUTHEAST;
        menuPanel.add(btnDel, g);
    }
    
    //2. NOTIFICATIONS PANEL
    private class NotificationsPanel extends JPanel {
        
        private JCheckBox cbLogin, cbTrans, cbSO, cbBill;

        public NotificationsPanel() {
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(30, 50, 30, 50));
            
            //Header
            JPanel head = new JPanel(new BorderLayout()); head.setBackground(Color.WHITE);
            JLabel t = new JLabel("Ειδοποιήσεις"); t.setFont(new Font("Segoe UI", Font.BOLD, 22));
            JButton back = new JButton("← Πίσω"); back.addActionListener(e -> cardLayout.show(mainContainer, "MENU"));
            head.add(t, BorderLayout.CENTER); head.add(back, BorderLayout.WEST);
            add(head, BorderLayout.NORTH);
            
            //Λίστα Επιλογών
            JPanel list = new JPanel(new GridLayout(0, 1, 10, 10)); 
            list.setBackground(Color.WHITE);
            list.setBorder(new EmptyBorder(10, 0, 10, 0));
            
            //Δημιουργία των Checkboxes (Συνδεδεμένα με τα πεδία του User)
            cbLogin = createToggle("Ειδοποίηση κατά τη Σύνδεση (Login)");
            cbTrans = createToggle("Ειδοποίηση για Συναλλαγές (Μεταφορές/Αναλήψεις)");
            cbSO = createToggle("Ειδοποίηση Αποτυχίας Πάγιας Εντολής");
            cbBill = createToggle("Ειδοποίηση Λήξης Λογαριασμού");
            
            list.add(cbLogin);
            list.add(cbTrans);
            list.add(cbSO);
            list.add(cbBill);
            
            JScrollPane scroll = new JScrollPane(list);
            scroll.setBorder(null);
            add(scroll, BorderLayout.CENTER);
            
            JButton save = new JButton("Αποθήκευση Προτιμήσεων");
            save.setBackground(new Color(40, 45, 55)); save.setForeground(Color.WHITE);
            save.addActionListener(e -> savePreferences());
            
            JPanel bot = new JPanel(); bot.setBackground(Color.WHITE); bot.add(save);
            add(bot, BorderLayout.SOUTH);
        }
        
        public void loadToggles() {
            cbLogin.setSelected(user.isNotifyLogin());
            cbTrans.setSelected(user.isNotifyTransaction());
            cbSO.setSelected(user.isNotifyStandingOrderFailed());
            cbBill.setSelected(user.isNotifyBillExpiring());
        }

        private void savePreferences() {
            user.setNotifyLogin(cbLogin.isSelected());
            user.setNotifyTransaction(cbTrans.isSelected());
            user.setNotifyStandingOrderFailed(cbSO.isSelected());
            user.setNotifyBillExpiring(cbBill.isSelected());
            
            //Αποθήκευση στο CSV
            BankDataStore.getInstance().saveAllData();
            
            JOptionPane.showMessageDialog(this, "Οι ρυθμίσεις αποθηκεύτηκαν επιτυχώς.");
            cardLayout.show(mainContainer, "MENU");
        }
        
        private JCheckBox createToggle(String label) {
            JCheckBox box = new JCheckBox(label);
            box.setBackground(Color.WHITE);
            box.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            return box;
        }
    }
    
    //3. SECURITY kai LIMITS PANEL
    private class SecurityPanel extends JPanel {
        private JLabel lblWith, lblTrans, lblPay;
        private JLabel lblUsername, lblPasswordMask;
        
        public SecurityPanel() {
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(30, 50, 30, 50));
            
            //Header
            JPanel head = new JPanel(new BorderLayout()); head.setBackground(Color.WHITE);
            JLabel t = new JLabel("Ασφάλεια και Όρια"); t.setFont(new Font("Segoe UI", Font.BOLD, 22));
            JButton back = new JButton("← Πίσω"); back.addActionListener(e -> cardLayout.show(mainContainer, "MENU"));
            head.add(t, BorderLayout.CENTER); head.add(back, BorderLayout.WEST);
            add(head, BorderLayout.NORTH);
            
            //Content
            JPanel content = new JPanel(new GridBagLayout());
            content.setBackground(Color.WHITE);
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(15, 10, 15, 10); g.fill = GridBagConstraints.HORIZONTAL; g.anchor = GridBagConstraints.WEST;
            
            //ΟΡΙΑ
            g.gridx=0; g.gridy=0; content.add(new JLabel("Όριο Ανάληψης :"), g);
            g.gridx=1; lblWith = new JLabel(); content.add(lblWith, g);
            g.gridx=2; content.add(createActionButton("Τροποποίηση", e -> modifyLimitLogic("WITHDRAWAL")), g);
            
            g.gridx=0; g.gridy=1; content.add(new JLabel("Όριο Μεταφοράς :"), g);
            g.gridx=1; lblTrans = new JLabel(); content.add(lblTrans, g);
            g.gridx=2; content.add(createActionButton("Τροποποίηση", e -> modifyLimitLogic("TRANSFER")), g);
            
            g.gridx=0; g.gridy=2; content.add(new JLabel("Όριο Πληρωμών :"), g);
            g.gridx=1; lblPay = new JLabel(); content.add(lblPay, g);
            g.gridx=2; content.add(createActionButton("Τροποποίηση", e -> modifyLimitLogic("PAYMENT")), g);
            
            //Separator
            g.gridx=0; g.gridy=3; g.gridwidth=3; content.add(new JSeparator(), g);
            g.gridwidth=1;
            
            //CREDENTIALS
            g.gridx=0; g.gridy=4; content.add(new JLabel("Όνομα Χρήστη :"), g);
            g.gridx=1; lblUsername = new JLabel(); content.add(lblUsername, g);
            g.gridx=2; content.add(createActionButton("Αλλαγή", e -> changeUsernameLogic()), g);
            
            g.gridx=0; g.gridy=5; content.add(new JLabel("Κωδικός Πρόσβασης :"), g);
            g.gridx=1; lblPasswordMask = new JLabel("*********"); content.add(lblPasswordMask, g);
            g.gridx=2; content.add(createActionButton("Αλλαγή", e -> changePasswordLogic()), g);
            
            add(content, BorderLayout.CENTER);
        }
        
        public void refreshData() {
            lblWith.setText(String.format("%.2f €", user.getLimitWithdrawal()));
            lblTrans.setText(String.format("%.2f €", user.getLimitTransfer()));
            lblPay.setText(String.format("%.2f €", user.getLimitPayment()));
            lblUsername.setText(user.getUsername());
        }
        
        private JButton createActionButton(String text, java.awt.event.ActionListener al) {
            JButton b = new JButton(text);
            b.setBackground(new Color(40, 45, 55)); b.setForeground(Color.WHITE);
            b.addActionListener(al);
            return b;
        }
        
        //LOGIC: Modify Limits
        private void modifyLimitLogic(String type) {
            String input = JOptionPane.showInputDialog(this, "Εισάγετε νέο όριο (€):");
            if(input != null && !input.isEmpty()) {
                String otp = JOptionPane.showInputDialog(this, "Επιβεβαίωση Ασφαλείας\nΕισάγετε OTP (1234):");
                if("1234".equals(otp)) {
                    try {
                        double val = Double.parseDouble(input);
                        if(type.equals("WITHDRAWAL")) user.setLimitWithdrawal(val);
                        else if(type.equals("TRANSFER")) user.setLimitTransfer(val);
                        else user.setLimitPayment(val);
                        
                        BankDataStore.getInstance().saveAllData(); // Save!
                        refreshData();
                        JOptionPane.showMessageDialog(this, "Το όριο ενημερώθηκε.");
                    } catch(Exception ex) { JOptionPane.showMessageDialog(this, "Λάθος αριθμός."); }
                } else {
                    JOptionPane.showMessageDialog(this, "Λάθος OTP.");
                }
            }
        }

        //LOGIC: Change Username
        private void changeUsernameLogic() {
            String newName = JOptionPane.showInputDialog(this, "Εισάγετε νέο Όνομα Χρήστη:");
            if (newName != null && !newName.trim().isEmpty()) {
                String otp = JOptionPane.showInputDialog(this, "Απαιτείται επιβεβαίωση.\nΕισάγετε OTP (1234):");
                if ("1234".equals(otp)) {
                    user.setUsername(newName);
                    BankDataStore.getInstance().saveAllData(); //Save!
                    refreshData();
                    JOptionPane.showMessageDialog(this, "Το Όνομα Χρήστη άλλαξε επιτυχώς!");
                } else {
                    JOptionPane.showMessageDialog(this, "Λάθος OTP. Η αλλαγή ακυρώθηκε.");
                }
            }
        }

        //LOGIC: Change Password
        private void changePasswordLogic() {
            JPasswordField pf = new JPasswordField();
            int res = JOptionPane.showConfirmDialog(this, pf, "Εισάγετε νέο Κωδικό:", JOptionPane.OK_CANCEL_OPTION);
            
            if (res == JOptionPane.OK_OPTION) {
                String newPass = new String(pf.getPassword());
                if (!newPass.isEmpty()) {
                    String otp = JOptionPane.showInputDialog(this, "Απαιτείται επιβεβαίωση.\nΕισάγετε OTP (1234):");
                    if ("1234".equals(otp)) {
                        user.setPassword(newPass);
                        BankDataStore.getInstance().saveAllData(); // Save!
                        JOptionPane.showMessageDialog(this, "Ο Κωδικός Πρόσβασης άλλαξε επιτυχώς!");
                    } else {
                        JOptionPane.showMessageDialog(this, "Λάθος OTP. Η αλλαγή ακυρώθηκε.");
                    }
                }
            }
        }
    }
    
    //GENERAL HELPERS
    private JButton createBigButton(String text) {
        JButton b = new JButton(text);
        b.setPreferredSize(new Dimension(200, 100));
        b.setBackground(new Color(40, 45, 55));
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 16));
        return b;
    }
    
    private void deleteUserLogic() {
        //1.Έλεγχος Υπολοίπων
        java.util.List<Account> myAccs = service.BankDataStore.getInstance().getAccountsForUser(user);
        
        for(Account a : myAccs) {
            //Ελέγχουμε αν είναι ο κύριος δικαιούχος για να απαγορεύσουμε τη διαγραφή αν έχει λεφτά
            if(a.getOwnerName().equals(user.getFullName()) && a.getBalance() > 0) {
                JOptionPane.showMessageDialog(this, "Αδυναμία διαγραφής: Υπάρχουν λογαριασμοί ιδιοκτησίας σας με θετικό υπόλοιπο.", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        //2.Ζητάμε OTP
        String otp = JOptionPane.showInputDialog(this, "Επιβεβαίωση Διαγραφής Χρήστη\nΕισάγετε OTP (1234):");
        
        if("1234".equals(otp)) {
            service.BankDataStore ds = service.BankDataStore.getInstance();

            //Α. ΔΙΑΧΕΙΡΙΣΗ ΛΟΓΑΡΙΑΣΜΩΝ
            java.util.List<Account> accountsToDelete = new java.util.ArrayList<>();
            
            for (Account acc : ds.getAccounts()) {
                if (acc.getOwnerName().equals(user.getFullName())) {
                    //Περίπτωση 1: Είναι ΚΥΡΙΟΣ Ιδιοκτήτης -> Ο λογαριασμός διαγράφεται
                    accountsToDelete.add(acc);
                } else {
                    //Περίπτωση 2: Είναι ΣΥΝΔΙΚΑΙΟΥΧΟΣ -> Αφαιρείται μόνο το όνομά του
                    if (acc.getJointOwners() != null) {
                        acc.getJointOwners().removeIf(jo -> jo.getAfm().equals(user.getAfm()));
                    }
                }
            }
            
            //Διαγραφή των λογαριασμών από τη λίστα
            ds.getAccounts().removeAll(accountsToDelete);

            //Β. ΔΙΑΓΡΑΦΗ ΠΑΓΙΩΝ ΕΝΤΟΛΩΝ
            //Πρέπει να σβήσουμε τις πάγιες εντολές που ξεκινούν από τους λογαριασμούς που μόλις σβήσαμε
            java.util.List<String> deletedIbans = new java.util.ArrayList<>();
            for(Account a : accountsToDelete) deletedIbans.add(a.getIban());
            
            ds.getStandingOrders().removeIf(so -> deletedIbans.contains(so.getSourceIban()));

            //Γ. ΔΙΑΓΡΑΦΗ ΟΦΕΙΛΩΝ (BILLS)
            // Σβήνουμε τυχόν απλήρωτους λογαριασμούς που χρεώνονται στο ΑΦΜ του
            ds.getPendingBills().removeIf(b -> b.getOwnerAfm().equals(user.getAfm()));

            //Δ. ΔΙΑΓΡΑΦΗ ΤΟΥ ΙΔΙΟΥ ΤΟΥ ΧΡΗΣΤΗ
            ds.getUsers().remove(user);

            //Ε. ΑΠΟΘΗΚΕΥΣΗ ΣΤΑ CSV
            //Καλουμε saveAllData για να ενημερωθούν users.csv, accounts.csv, bills.csv κλπ.
            ds.saveAllData();

            JOptionPane.showMessageDialog(this, "Ο χρήστης και τα δεδομένα του διαγράφηκαν οριστικά.");
            
            //Τερματισμός ή επιστροφή στο Login
            System.exit(0); 
        } else {
            JOptionPane.showMessageDialog(this, "Λάθος OTP.");
        }
    }
}