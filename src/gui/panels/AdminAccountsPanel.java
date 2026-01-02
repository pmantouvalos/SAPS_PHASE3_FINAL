package gui.panels;

import commands.PaymentCommand;
import commands.TransferCommand;
import model.*;
import model.Account.JointOwner;
import service.AccountFactory;
import service.BankDataStore;
import utils.TimeManager;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminAccountsPanel extends JPanel implements Refreshable {
    private JTextField searchField;
    private JTable accountsTable;
    private DefaultTableModel model;

    public AdminAccountsPanel() {
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        //SEARCH
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Αναζήτηση (IBAN/Όνομα):"));
        searchField = new JTextField(20);
        JButton searchBtn = new JButton("Αναζήτηση");
        searchBtn.addActionListener(e -> refreshData(searchField.getText()));
        top.add(searchField); top.add(searchBtn);
        
        JButton createBtn = new JButton("Δημιουργία Νέου Λογαριασμού");
        createBtn.setBackground(new Color(0, 100, 0));
        createBtn.setForeground(Color.WHITE);
        createBtn.addActionListener(e -> createNewAccount());
        top.add(createBtn);
        
        add(top, BorderLayout.NORTH);

        //TABLE
        String[] cols = {"IBAN", "Τύπος", "Ιδιοκτήτης", "Υπόλοιπο"};
        model = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        accountsTable = new JTable(model);
        accountsTable.setRowHeight(25);
        add(new JScrollPane(accountsTable), BorderLayout.CENTER);

        //ACTIONS
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton manageBtn = new JButton("Διαχείριση & Συναλλαγές");
        manageBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        manageBtn.addActionListener(e -> openManagementDialog());
        
        JButton deleteBtn = new JButton("Διαγραφή Λογαριασμού");
        deleteBtn.setBackground(Color.RED);
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.addActionListener(e -> deleteAccount());
        
        actions.add(manageBtn); actions.add(deleteBtn);
        add(actions, BorderLayout.SOUTH);

        refreshData("");
    }

    private void refreshData(String query) {
        model.setRowCount(0);
        for(Account a : BankDataStore.getInstance().getAccounts()) {
            if(!query.isEmpty()) {
                if(!a.getIban().contains(query) && !a.getOwnerName().contains(query)) continue;
            }
            model.addRow(new Object[]{a.getIban(), a.getAccountType(), a.getOwnerName(), a.getBalance()});
        }
    }

    private Account getSelectedAccount() {
        int row = accountsTable.getSelectedRow();
        if(row == -1) return null;
        String iban = (String) model.getValueAt(row, 0);
        return BankDataStore.getInstance().getAccountByIban(iban);
    }

    //CREATE and DELETE
    private void createNewAccount() {
        JTextField ownerF = new JTextField();
        String[] types = {"Τρεχούμενος", "Ταμιευτηρίου", "Όψεως"};
        JComboBox<String> typeBox = new JComboBox<>(types);
        
        Object[] msg = {"Όνομα Ιδιοκτήτη :", ownerF, "Τύπος:", typeBox};
        if(JOptionPane.showConfirmDialog(this, msg, "Δημιουργία", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String owner = ownerF.getText();
            //Verify user exists
            User u = BankDataStore.getInstance().getUsers().stream().filter(usr -> usr.getFullName().equalsIgnoreCase(owner)).findFirst().orElse(null);
            if(u == null) { JOptionPane.showMessageDialog(this, "Δεν βρέθηκε χρήστης με αυτό το όνομα."); return; }
            
            Account acc = AccountFactory.createAccount((String)typeBox.getSelectedItem(), "GR"+System.currentTimeMillis(), u.getFullName(), 0.0);
            BankDataStore.getInstance().getAccounts().add(acc);
            refreshData("");
            JOptionPane.showMessageDialog(this, "Δημιουργήθηκε: " + acc.getIban());
        }
    }

    private void deleteAccount() {
        Account a = getSelectedAccount();
        if(a == null) { JOptionPane.showMessageDialog(this, "Επιλέξτε λογαριασμό."); return; }
        if(JOptionPane.showConfirmDialog(this, "Διαγραφή;", "Confirm", JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) {
            BankDataStore.getInstance().getAccounts().remove(a);
            refreshData("");
        }
    }

    //MANAGEMENT DIALOG (ΤΟ ΚΕΝΤΡΟ ΕΛΕΓΧΟΥ ΤΟΥ ADMIN)

    private void openManagementDialog() {
        Account acc = getSelectedAccount();
        if(acc == null) { JOptionPane.showMessageDialog(this, "Επιλέξτε λογαριασμό."); return; }

        JDialog d = new JDialog((Frame)null, "Διαχείριση: " + acc.getIban(), true);
        d.setSize(800, 600); d.setLocationRelativeTo(this);
        
        JTabbedPane tabs = new JTabbedPane();
        
        tabs.addTab("Συνδικαιούχοι", createJointPanel(acc));
        tabs.addTab("Ταμείο", createCashierPanel(acc));
        tabs.addTab("Μεταφορές & Πληρωμές", createTransfersPanel(acc));
        tabs.addTab("Πάγιες Εντολές", createStandingOrdersPanel(acc));
        
        //ΙΣΤΟΡΙΚΟ
        //Κρατάμε αναφορά στο μοντέλο του πίνακα ιστορικού για να το ανανεώνουμε
        DefaultTableModel historyModel = new DefaultTableModel(new String[]{"Ημ/νία", "Είδος", "Ποσό", "Υπόλοιπο"}, 0);
        JTable historyTable = new JTable(historyModel);
        
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.add(new JScrollPane(historyTable), BorderLayout.CENTER);
        
        tabs.addTab("Ιστορικό", historyPanel);

        //LISTENER ΓΙΑ ΑΥΤΟΜΑΤΗ ΑΝΑΝΕΩΣΗ
        tabs.addChangeListener(e -> {
            //Αν επιλέχθηκε η καρτέλα "Ιστορικό"
            if (tabs.getSelectedIndex() == 4) {
                historyModel.setRowCount(0);
                //Ταξινόμηση για να φαίνονται οι πρόσφατες πάνω
                acc.getTransactions().sort((t1, t2) -> t2.getDate().compareTo(t1.getDate()));
                
                for(Transaction tr : acc.getTransactions()) {
                    historyModel.addRow(new Object[]{
                        tr.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yy")),
                        tr.getType(),
                        String.format("%.2f €", tr.getAmount()),
                        String.format("%.2f €", tr.getBalanceAfter())
                    });
                }
            }
        });

        d.add(tabs);
        d.setVisible(true);
        refreshData(searchField.getText());
    }

    //TAB 1: Joint Owners
    private JPanel createJointPanel(Account acc) {
        JPanel p = new JPanel(new BorderLayout());
        DefaultTableModel tm = new DefaultTableModel(new String[]{"ΑΦΜ", "Όνομα", "Πρόσβαση"}, 0);
        JTable t = new JTable(tm);
        
        Runnable load = () -> {
            tm.setRowCount(0);
            for(JointOwner jo : acc.getJointOwners()) tm.addRow(new Object[]{jo.getAfm(), jo.getName()+" "+jo.getSurname(), jo.getAccessLevel()});
        };
        load.run();
        
        p.add(new JScrollPane(t), BorderLayout.CENTER);
        
        JPanel btnP = new JPanel();
        JButton addBtn = new JButton("Προσθήκη Συνδικαιούχου");
        addBtn.addActionListener(e -> {
            String afm = JOptionPane.showInputDialog("Εισάγετε ΑΦΜ Χρήστη:");
            User u = BankDataStore.getInstance().getUsers().stream().filter(usr -> usr.getAfm().equals(afm)).findFirst().orElse(null);
            if(u!=null) {
                String[] rights = {"Πλήρης", "Μερική", "Προβολή"};
                int r = JOptionPane.showOptionDialog(null, "Δικαίωμα:", "Access", 0, JOptionPane.QUESTION_MESSAGE, null, rights, rights[0]);
                String[] nameParts = u.getFullName().split(" ");
                acc.addJointOwner(new JointOwner(nameParts[0], nameParts.length>1?nameParts[1]:"", u.getAfm(), rights[r]));
                load.run();
            } else JOptionPane.showMessageDialog(null, "Δεν βρέθηκε χρήστης.");
        });
        
        JButton remBtn = new JButton("Αφαίρεση");
        remBtn.addActionListener(e -> {
            int row = t.getSelectedRow();
            if(row!=-1) { acc.removeJointOwner((String)tm.getValueAt(row, 0)); load.run(); }
        });
        
        btnP.add(addBtn); btnP.add(remBtn);
        p.add(btnP, BorderLayout.SOUTH);
        return p;
    }

    //TAB 2: Cashier
    private JPanel createCashierPanel(Account acc) {
        JPanel p = new JPanel(new GridBagLayout());
        JTextField amtF = new JTextField(10);
        JButton depBtn = new JButton("Κατάθεση");
        JButton withBtn = new JButton("Ανάληψη");
        
        depBtn.addActionListener(e -> doCashierTx(acc, amtF, true));
        withBtn.addActionListener(e -> doCashierTx(acc, amtF, false));
        
        p.add(new JLabel("Ποσό: ")); p.add(amtF);
        p.add(depBtn); p.add(withBtn);
        return p;
    }
    
    private void doCashierTx(Account acc, JTextField f, boolean isDep) {
        try {
            double amt = Double.parseDouble(f.getText());
            if(!isDep && acc.getBalance() < amt) { JOptionPane.showMessageDialog(null, "Ανεπαρκές υπόλοιπο"); return; }
            acc.setBalance(isDep ? acc.getBalance()+amt : acc.getBalance()-amt);
            
            Transaction t = new Transaction.Builder(amt)
                .setType(isDep ? "Κατάθεση (Ταμείο)" : "Ανάληψη (Ταμείο)")
                .setDescription("Admin Operation")
                .setDate(TimeManager.getInstance().getDate())
                .setBalanceAfter(acc.getBalance()).build();
            acc.addTransaction(t);
            JOptionPane.showMessageDialog(null, "Επιτυχία! Νέο Υπόλοιπο: " + acc.getBalance());
        } catch(Exception ex) { JOptionPane.showMessageDialog(null, "Λάθος ποσό"); }
    }

    //TAB 3: Transfers & Payments
    private JPanel createTransfersPanel(Account acc) {
        JPanel p = new JPanel(new GridLayout(2, 1));
        p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        
        // Transfer Area
        JPanel trP = new JPanel(new FlowLayout());
        trP.setBorder(new TitledBorder("Μεταφορά"));
        JTextField trgIban = new JTextField(15); JTextField trAmt = new JTextField(8);
        JButton sendBtn = new JButton("Αποστολή");
        sendBtn.addActionListener(e -> {
            try {
                double amt = Double.parseDouble(trAmt.getText());
                new TransferCommand(acc.getIban(), trgIban.getText(), amt, 0.0, "Admin Transfer").execute();
                JOptionPane.showMessageDialog(null, "Εντολή εστάλη.");
            } catch(Exception ex) { JOptionPane.showMessageDialog(null, "Error"); }
        });
        trP.add(new JLabel("Προς IBAN:")); trP.add(trgIban); trP.add(new JLabel("Ποσό:")); trP.add(trAmt); trP.add(sendBtn);
        
        //Payment Area
        JPanel payP = new JPanel(new FlowLayout());
        payP.setBorder(new TitledBorder("Πληρωμή (RF)"));
        JTextField rfF = new JTextField(15); JTextField payAmt = new JTextField(8);
        JButton payBtn = new JButton("Πληρωμή");
        payBtn.addActionListener(e -> {
            try {
                double amt = Double.parseDouble(payAmt.getText());
                //Simple logic for admin
                new PaymentCommand(acc.getIban(), "Οργανισμός (RF)", amt, "Admin RF: "+rfF.getText(), null).execute();
                JOptionPane.showMessageDialog(null, "Πληρωμή εκτελέστηκε.");
            } catch(Exception ex) { JOptionPane.showMessageDialog(null, "Error"); }
        });
        payP.add(new JLabel("RF:")); payP.add(rfF); payP.add(new JLabel("Ποσό:")); payP.add(payAmt); payP.add(payBtn);
        
        p.add(trP); p.add(payP);
        return p;
    }

    //TAB 4: Standing Orders
    private JPanel createStandingOrdersPanel(Account acc) {
        JPanel p = new JPanel(new BorderLayout());
        DefaultTableModel tm = new DefaultTableModel(new String[]{"Τύπος", "Προς", "Ποσό", "Συχνότητα"}, 0);
        JTable t = new JTable(tm);
        
        Runnable load = () -> {
            tm.setRowCount(0);
            for(StandingOrder so : BankDataStore.getInstance().getStandingOrders()) {
                if(so.getSourceIban().equals(acc.getIban())) {
                    tm.addRow(new Object[]{so.getType(), so.getTarget(), so.getAmount(), so.getFrequencyDays()+" μέρες"});
                }
            }
        };
        load.run();
        
        p.add(new JScrollPane(t), BorderLayout.CENTER);
        
        JPanel btnP = new JPanel();
        JButton delBtn = new JButton("Διαγραφή Επιλεγμένης");
        delBtn.addActionListener(e -> {
            int r = t.getSelectedRow();
            if(r != -1) {
                // Απλή λογική διαγραφής: Βρίσκουμε την εντολή στη λίστα και τη σβήνουμε
                String target = (String)tm.getValueAt(r, 1);
                BankDataStore.getInstance().getStandingOrders().removeIf(so -> so.getSourceIban().equals(acc.getIban()) && so.getTarget().equals(target));
                load.run();
            }
        });
        
        btnP.add(delBtn);
        p.add(btnP, BorderLayout.SOUTH);
        return p;
    }

  

    @Override
    public void refresh() { refreshData(searchField.getText()); }
}