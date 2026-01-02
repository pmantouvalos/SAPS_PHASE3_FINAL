package gui.panels;

import model.Account;
import model.Account.JointOwner;
import model.Role;
import model.Transaction;
import model.User;
import service.AccountFactory;
import service.BankDataStore;
import utils.TimeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;


public class AccountsPanel extends JPanel implements Refreshable {
    private CardLayout cardLayout;
    private JPanel mainContainer;
    private model.User currentUser;

    //Panels
    private AccountsListPanel listPanel;
    private AccountDetailsPanel detailsPanel;

    public AccountsPanel(model.User user) {
        this.currentUser = user;
        setLayout(new BorderLayout());
        
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        listPanel = new AccountsListPanel();
        detailsPanel = new AccountDetailsPanel();

        mainContainer.add(listPanel, "LIST");
        mainContainer.add(detailsPanel, "DETAILS");

        add(mainContainer, BorderLayout.CENTER);
        cardLayout.show(mainContainer, "LIST");
    }

    @Override
    public void refresh() {
        listPanel.refreshData();
        //Αν είμαστε στις λεπτομέρειες, κάνουμε refresh και εκεί
        if (detailsPanel.isVisible() && detailsPanel.getCurrentAccount() != null) {
            detailsPanel.setAccount(detailsPanel.getCurrentAccount()); // Trigger update & permissions
        }
    }


    //1.ΛΙΣΤΑ ΛΟΓΑΡΙΑΣΜΩΝ (ΑΡΧΙΚΗ ΟΘΟΝΗ TAB)
    private class AccountsListPanel extends JPanel {
        private DefaultTableModel tableModel;
        private JTable table;

        public AccountsListPanel() {
            setLayout(new BorderLayout(15, 15));
            setBorder(new EmptyBorder(20, 20, 20, 20));

            //Header
            JPanel header = new JPanel(new BorderLayout());
            JLabel title = new JLabel("Οι λογαριασμοί μου");
            title.setFont(new Font("Segoe UI", Font.BOLD, 24));
            
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            
            //Κουμπιά (Μόνο για Ιδιώτες)
            if (currentUser.getRole() != Role.BUSINESS) {
                JButton newAccBtn = createStyledButton("Άνοιγμα Νέου Λογαριασμού", new Color(40, 40, 40));
                newAccBtn.addActionListener(e -> showOpenAccountDialog());
                
                JButton consolBtn = createStyledButton("Ενοποίηση Λογαριασμών", new Color(40, 40, 40));
                
                
                consolBtn.addActionListener(e -> { showConsolidationDialog();
                });
                
                
                
                btnPanel.add(newAccBtn);
                btnPanel.add(consolBtn);
            }

            header.add(title, BorderLayout.WEST);
            header.add(btnPanel, BorderLayout.EAST);
            add(header, BorderLayout.NORTH);

            //Table
            String[] cols = {"IBAN", "Τύπος", "Ρόλος", "Δικαιούχος", "Υπόλοιπο"};
            tableModel = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            table = new JTable(tableModel);
            table.setRowHeight(35);
            table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
            add(new JScrollPane(table), BorderLayout.CENTER);

            //Footer
            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton detailsBtn = createStyledButton("Προβολή Λεπτομερειών", new Color(0, 102, 204));
            detailsBtn.addActionListener(e -> openDetailsSelection());
            bottom.add(detailsBtn);
            add(bottom, BorderLayout.SOUTH);
            
            refreshData();
        }

        public void refreshData() {
            tableModel.setRowCount(0);
            //Χρήση της μεθόδου που φέρνει Ιδιοκτησία + Συνδικαιούχους
            List<Account> myAccounts = BankDataStore.getInstance().getAccountsForUser(currentUser);
            
            for (Account a : myAccounts) {
                boolean isOwner = a.getOwnerName().equals(currentUser.getFullName());
                tableModel.addRow(new Object[]{
                    a.getIban(), 
                    a.getAccountType(), 
                    isOwner ? "Ιδιοκτήτης" : "Συνδικαιούχος", 
                    a.getOwnerName(), 
                    String.format("%.2f €", a.getBalance())
                });
            }
        }

        private void openDetailsSelection() {
            int row = table.getSelectedRow();
            if (row != -1) {
                String iban = (String) tableModel.getValueAt(row, 0);
                Account selected = BankDataStore.getInstance().getAccountByIban(iban);
                detailsPanel.setAccount(selected);
                cardLayout.show(mainContainer, "DETAILS");
            } else {
                JOptionPane.showMessageDialog(this, "Παρακαλώ επιλέξτε έναν λογαριασμό.");
            }
        }
    }

    private void showConsolidationDialog() {
        //Δημιουργία παραθύρου (Dialog)
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Ενοποίηση Λογαριασμών", true);
        dialog.setSize(500, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        //Header
        JLabel title = new JLabel("Ενοποίηση Λογαριασμών", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        title.setBorder(new EmptyBorder(20, 0, 20, 0));
        dialog.add(title, BorderLayout.NORTH);

        //Form Panel
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(10, 40, 10, 40));

        //Λίστα λογαριασμών του χρήστη
        List<Account> myAccounts = BankDataStore.getInstance().getAccountsForUser(currentUser);
        String[] accountItems = new String[myAccounts.size()];
        for (int i = 0; i < myAccounts.size(); i++) {
            Account a = myAccounts.get(i);
            //Εμφανίζουμε IBAN - Τύπος - Υπόλοιπο για να ξέρει τι επιλέγει
            accountItems[i] = a.getIban() + " (" + a.getAccountType() + ") - " + String.format("%.2f€", a.getBalance());
        }

        //Dropdowns
        JComboBox<String> primaryBox = new JComboBox<>(accountItems);
        JComboBox<String> secondaryBox = new JComboBox<>(accountItems);

        //Styling Dropdowns
        primaryBox.setBackground(Color.WHITE);
        secondaryBox.setBackground(Color.WHITE);

        formPanel.add(new JLabel("Πρωτεύων Λογαριασμός:"));
        formPanel.add(new JLabel("Δευτερεύων Λογαριασμός:"));
        formPanel.add(primaryBox);
        formPanel.add(secondaryBox);

        dialog.add(formPanel, BorderLayout.CENTER);

        //Button Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new EmptyBorder(10, 40, 20, 40));

        JButton confirmBtn = new JButton("Ενοποίηση Λογαριασμών");
        confirmBtn.setBackground(new Color(40, 45, 55));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setFocusPainted(false);
        
        btnPanel.add(confirmBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        //LOGIC: Τι γίνεται όταν πατάει το κουμπί ---
        confirmBtn.addActionListener(ev -> {
            int pIndex = primaryBox.getSelectedIndex();
            int sIndex = secondaryBox.getSelectedIndex();

            if (pIndex == -1 || sIndex == -1) return;

            //Βρίσκουμε τα πραγματικά αντικείμενα Account
            Account primary = myAccounts.get(pIndex);
            Account secondary = myAccounts.get(sIndex);

            //ΕΛΕΓΧΟΣ 1:Δεν μπορείς να ενώσεις τον λογαριασμό με τον εαυτό του
            if (primary.equals(secondary)) {
                JOptionPane.showMessageDialog(dialog, "Ο Πρωτεύων και ο Δευτερεύων λογαριασμός πρέπει να είναι διαφορετικοί.", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
                return;
            }

            //ΕΛΕΓΧΟΣ 2:Πρέπει να είναι ίδιου τύπου (π.χ. Τρεχούμενος με Τρεχούμενο)
            if (!primary.getAccountType().equals(secondary.getAccountType())) {
                JOptionPane.showMessageDialog(dialog, "Μπορείτε να ενοποιήσετε μόνο λογαριασμούς ίδιου τύπου.", "Σφάλμα Τύπου", JOptionPane.WARNING_MESSAGE);
                return;
            }

            //ΕΠΙΒΕΒΑΙΩΣΗ
            String msg = "Το υπόλοιπο " + String.format("%.2f€", secondary.getBalance()) + " του Δευτερεύοντος\n" +
                         "θα μεταφερθεί στον Πρωτεύοντα (" + primary.getIban() + ").\n" +
                         "Ο Δευτερεύων (" + secondary.getIban() + ") θα κλείσει οριστικά.\n\n" +
                         "Είστε σίγουροι;";

            int choice = JOptionPane.showConfirmDialog(dialog, msg, "Επιβεβαίωση Ενοποίησης", JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION) {
                //1.Μεταφορά χρημάτων
                double transferAmount = secondary.getBalance();
                primary.setBalance(primary.getBalance() + transferAmount);

                //2.Διαγραφή του παλιού
                //(Χρησιμοποιούμε τη μέθοδο remove που αφαιρεί από τη λίστα του DataStore)
                BankDataStore.getInstance().getAccounts().remove(secondary);

                //3.Αποθήκευση στο CSV (μέσω του AccountDAO που καλεί το saveAllData)
                BankDataStore.getInstance().saveAllData();

                //4.Ενημέρωση UI
                dialog.dispose(); //Κλείσιμο παραθύρου
                refresh(); //Ανανέωση της κεντρικής λίστας (AccountsPanel)
                
                JOptionPane.showMessageDialog(this, "Η ενοποίηση των λογαριασμών ολοκληρώθηκε με επιτυχία.", "Μήνυμα επικύρωσης", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }
    
    
    
    
    //2.ΛΕΠΤΟΜΕΡΕΙΕΣ ΛΟΓΑΡΙΑΣΜΟΥ
    private class AccountDetailsPanel extends JPanel {
        private Account account;
        private JLabel ibanLbl, typeLbl, balanceLbl, ownerNameLbl;
        private DefaultTableModel jointOwnersModel;
        private JTable jointOwnersTable;
        
        //Κουμπιά Ενεργειών
        private JButton depositBtn, withdrawBtn, addOwnerBtn, editOwnerBtn, removeOwnerBtn, deleteAccBtn;

        public AccountDetailsPanel() {
            setLayout(new BorderLayout(20, 20));
            setBorder(new EmptyBorder(20, 20, 20, 20));

            // Header
            JPanel header = new JPanel(new BorderLayout());
            JButton backBtn = new JButton("← Πίσω");
            backBtn.addActionListener(e -> {
                listPanel.refreshData();
                cardLayout.show(mainContainer, "LIST");
            });
            JLabel title = new JLabel(" Λεπτομέρειες Λογαριασμού", SwingConstants.CENTER);
            title.setFont(new Font("Segoe UI", Font.BOLD, 22));
            header.add(backBtn, BorderLayout.WEST); header.add(title, BorderLayout.CENTER);
            add(header, BorderLayout.NORTH);

            //Content Split
            JPanel content = new JPanel(new GridLayout(1, 2, 20, 0));

            //LEFT COLUMN (Info)
            JPanel leftCol = new JPanel(); leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
            
            JPanel infoBox = new JPanel(new GridLayout(3, 2, 10, 10));
            infoBox.setBorder(new TitledBorder("Στοιχεία"));
            infoBox.add(new JLabel("IBAN:")); ibanLbl = new JLabel(); infoBox.add(ibanLbl);
            infoBox.add(new JLabel("Τύπος:")); typeLbl = new JLabel(); infoBox.add(typeLbl);
            infoBox.add(new JLabel("Υπόλοιπο:")); balanceLbl = new JLabel(); 
            balanceLbl.setForeground(new Color(0, 100, 0)); balanceLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
            infoBox.add(balanceLbl);
            leftCol.add(infoBox); leftCol.add(Box.createVerticalStrut(10));

            JPanel ownerBox = new JPanel(new GridLayout(1, 2));
            ownerBox.setBorder(new TitledBorder("Ιδιοκτήτης"));
            ownerNameLbl = new JLabel(); ownerBox.add(ownerNameLbl);
            leftCol.add(ownerBox); leftCol.add(Box.createVerticalStrut(10));

            JPanel jointBox = new JPanel(new BorderLayout());
            jointBox.setBorder(new TitledBorder("Συνδικαιούχοι"));
            String[] cols = {"Επώνυμο", "Όνομα", "ΑΦΜ", "Πρόσβαση"};
            jointOwnersModel = new DefaultTableModel(cols, 0);
            jointOwnersTable = new JTable(jointOwnersModel);
            jointOwnersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            jointBox.add(new JScrollPane(jointOwnersTable), BorderLayout.CENTER);
            leftCol.add(jointBox);
            
            content.add(leftCol);

            //RIGHT COLUMN (Actions)
            JPanel rightCol = new JPanel();
            rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));
            rightCol.setBorder(new TitledBorder("Ενέργειες"));

            depositBtn = createActionButton("Κατάθεση (+)", e -> doTransaction(true));
            rightCol.add(depositBtn); rightCol.add(Box.createVerticalStrut(10));
            
            withdrawBtn = createActionButton("Ανάληψη (-)", e -> doTransaction(false));
            rightCol.add(withdrawBtn); rightCol.add(Box.createVerticalStrut(30));
            
            addOwnerBtn = createActionButton("Προσθήκη Συνδικαιούχου", e -> showAddOwnerDialog());
            rightCol.add(addOwnerBtn); rightCol.add(Box.createVerticalStrut(10));
            
            editOwnerBtn = createActionButton("Επεξεργασία Δικαιωμάτων", e -> showEditRightsDialog());
            rightCol.add(editOwnerBtn); rightCol.add(Box.createVerticalStrut(10));
            
            removeOwnerBtn = createActionButton("Αφαίρεση Συνδικαιούχου", e -> removeSelectedOwner());
            rightCol.add(removeOwnerBtn);
            
            rightCol.add(Box.createVerticalGlue());
            deleteAccBtn = createStyledButton("Διαγραφή Λογαριασμού", new Color(180, 0, 0));
            deleteAccBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            deleteAccBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            deleteAccBtn.addActionListener(e -> deleteAccountLogic());
            rightCol.add(deleteAccBtn);

            content.add(rightCol);
            add(content, BorderLayout.CENTER);
        }

        public void setAccount(Account acc) {
            this.account = acc;
            updateUIFromAccount();
            applyPermissions(); //Εφαρμογή δικαιωμάτων
        }
        
        public Account getCurrentAccount() { return account; }

        public void updateUIFromAccount() {
            if (account == null) return;
            ibanLbl.setText(account.getIban());
            typeLbl.setText(account.getAccountType());
            balanceLbl.setText(String.format("%.2f €", account.getBalance()));
            ownerNameLbl.setText(account.getOwnerName());

            jointOwnersModel.setRowCount(0);
            for(JointOwner jo : account.getJointOwners()) {
                jointOwnersModel.addRow(new Object[]{jo.getSurname(), jo.getName(), jo.getAfm(), jo.getAccessLevel()});
            }
        }

        //ΕΛΕΓΧΟΣ ΔΙΚΑΙΩΜΑΤΩΝ
        public void applyPermissions() {
            if (account == null) return;
            boolean isOwner = account.getOwnerName().equals(currentUser.getFullName());
            
            if (isOwner) {
                enableAllButtons(true);
                return;
            }

            JointOwner me = account.getJointOwners().stream()
                    .filter(jo -> jo.getAfm().equals(currentUser.getAfm()))
                    .findFirst().orElse(null);

            if (me != null) {
                String rights = me.getAccessLevel();
                if (rights.contains("Προβολή")) {
                    enableAllButtons(false);
                } else if (rights.contains("Μερική")) {
                    depositBtn.setEnabled(true);
                    withdrawBtn.setEnabled(false); //Όχι ανάληψη
                    addOwnerBtn.setEnabled(false);
                    editOwnerBtn.setEnabled(false);
                    removeOwnerBtn.setEnabled(false);
                    deleteAccBtn.setEnabled(false);
                } else {
                    enableAllButtons(true);
                    deleteAccBtn.setEnabled(false);
                }
            }
        }
        
        private void enableAllButtons(boolean val) {
            depositBtn.setEnabled(val); withdrawBtn.setEnabled(val);
            addOwnerBtn.setEnabled(val); editOwnerBtn.setEnabled(val); 
            removeOwnerBtn.setEnabled(val); deleteAccBtn.setEnabled(val);
        }


        //1.Κατάθεση / Ανάληψη
        private void doTransaction(boolean isDeposit) {
        	
        	
        	//1.ΕΛΕΓΧΟΣ ΚΛΕΙΔΩΜΕΝΟΥ ΧΡΗΣΤΗ
            if (currentUser.isLocked()) {
                JOptionPane.showMessageDialog(this, 
                    "Ο λογαριασμός σας είναι κλειδωμένος.\nΔεν μπορείτε να πραγματοποιήσετε συναλλαγές.", 
                    "Πρόσβαση Αρνητική", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String label = isDeposit ? "Κατάθεση" : "Ανάληψη";
            String input = JOptionPane.showInputDialog(this, "Ποσό για " + label + ":");
            
            if (input != null && !input.isEmpty()) {
                try {
                    double amount = Double.parseDouble(input);
                    if (amount <= 0) throw new NumberFormatException();

                    //1.Έλεγχος Υπολοίπου (για Ανάληψη)
                    if (!isDeposit && account.getBalance() < amount) {
                        JOptionPane.showMessageDialog(this, "Ανεπαρκές υπόλοιπο.");
                        return;
                    }
                    
                    //2.ΕΛΕΓΧΟΣ ΟΡΙΟΥ ΑΝΑΛΗΨΗΣ
                    if (!isDeposit) {
                        double limit = currentUser.getLimitWithdrawal();
                        if (amount > limit) {
                            JOptionPane.showMessageDialog(this, 
                                "Η συναλλαγή ακυρώθηκε.\n" +
                                "Το ποσό υπερβαίνει το ημερήσιο όριο ανάληψης (" + limit + "€).", 
                                "Υπέρβαση Ορίου", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }

                    //3.Εκτέλεση
                    account.setBalance(isDeposit ? account.getBalance() + amount : account.getBalance() - amount);

                    Transaction t = new Transaction.Builder(amount)
                            .setType(label)
                            .setDescription(label + " από " + currentUser.getFullName())
                            .setDate(TimeManager.getInstance().getDate())
                            .setSender(isDeposit ? currentUser.getFullName() : account.getIban())
                            .setReceiver(isDeposit ? account.getIban() : "Μετρητά")
                            .setBalanceAfter(account.getBalance())
                            .build();
                    account.addTransaction(t);

                    updateUIFromAccount();
                    JOptionPane.showMessageDialog(this, "Η " + label + " ολοκληρώθηκε επιτυχώς!");
                    
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Μη έγκυρο ποσό.");
                }
            }
        }

        //2.ΠΡΟΣΘΗΚΗ ΣΥΝΔΙΚΑΙΟΥΧΟΥ
        private void showAddOwnerDialog() {
            JDialog d = new JDialog((Frame)null, "Προσθήκη Συνδικαιούχου", true);
            d.setSize(400, 320); d.setLocationRelativeTo(this);
            d.setLayout(new GridBagLayout());
            GridBagConstraints g = new GridBagConstraints(); g.insets = new Insets(5,5,5,5); g.fill = GridBagConstraints.HORIZONTAL; g.gridx=0; g.gridy=0;

            JTextField afmF = new JTextField(15);
            d.add(new JLabel("ΑΦΜ Χρήστη:"), g); g.gridx=1; d.add(afmF, g);

            g.gridx=0; g.gridy=1; g.gridwidth=2; d.add(new JLabel("Επίπεδο Πρόσβασης:"), g);
            JRadioButton r1 = new JRadioButton("Πλήρης"); r1.setSelected(true);
            JRadioButton r2 = new JRadioButton("Μερική");
            JRadioButton r3 = new JRadioButton("Μόνο Προβολή");
            ButtonGroup bg = new ButtonGroup(); bg.add(r1); bg.add(r2); bg.add(r3);
            g.gridy=2; d.add(r1, g); g.gridy=3; d.add(r2, g); g.gridy=4; d.add(r3, g);

            JButton addBtn = createStyledButton("Αναζήτηση & Προσθήκη", new Color(0, 100, 0));
            addBtn.addActionListener(e -> {
                String afmIn = afmF.getText().trim();
                //Αναζήτηση στο Store
                User found = BankDataStore.getInstance().getUsers().stream().filter(u->u.getAfm().equals(afmIn)).findFirst().orElse(null);
                
                if (found != null) {
                    if (account.getOwnerName().equals(found.getFullName()) || account.getJointOwners().stream().anyMatch(j->j.getAfm().equals(found.getAfm()))) {
                        JOptionPane.showMessageDialog(d, "Ο χρήστης είναι ήδη στον λογαριασμό."); return;
                    }
                    //Parse Name
                    String[] parts = found.getFullName().split(" ");
                    String name = parts.length > 0 ? parts[0] : "";
                    String sur = parts.length > 1 ? parts[1] : "";
                    String rights = r1.isSelected() ? "Πλήρης" : (r2.isSelected() ? "Μερική" : "Προβολή");
                    
                    account.addJointOwner(new JointOwner(name, sur, found.getAfm(), rights));
                    updateUIFromAccount();
                    JOptionPane.showMessageDialog(d, "Προστέθηκε: " + found.getFullName());
                    d.dispose();
                } else {
                    JOptionPane.showMessageDialog(d, "Δεν βρέθηκε χρήστης με αυτό το ΑΦΜ.");
                }
            });
            g.gridy=5; d.add(addBtn, g);
            d.setVisible(true);
        }

        //3.ΕΠΕΞΕΡΓΑΣΙΑ ΔΙΚΑΙΩΜΑΤΩΝ
        private void showEditRightsDialog() {
            int row = jointOwnersTable.getSelectedRow();
            if(row == -1) { JOptionPane.showMessageDialog(this, "Επιλέξτε συνδικαιούχο."); return; }
            
            String afm = (String) jointOwnersModel.getValueAt(row, 2);
            JointOwner jo = account.getJointOwners().stream().filter(j->j.getAfm().equals(afm)).findFirst().orElse(null);
            if(jo==null) return;

            JDialog d = new JDialog((Frame)null, "Δικαιώματα", true); d.setSize(300, 200); d.setLocationRelativeTo(this);
            d.setLayout(new FlowLayout());
            d.add(new JLabel("Αλλαγή δικαιωμάτων για: " + jo.getName()));
            
            JRadioButton r1 = new JRadioButton("Πλήρης");
            JRadioButton r2 = new JRadioButton("Μερική");
            JRadioButton r3 = new JRadioButton("Προβολή");
            ButtonGroup bg = new ButtonGroup(); bg.add(r1); bg.add(r2); bg.add(r3);
            if(jo.getAccessLevel().contains("Πλήρης")) r1.setSelected(true); else if(jo.getAccessLevel().contains("Μερική")) r2.setSelected(true); else r3.setSelected(true);
            
            JPanel p = new JPanel(new GridLayout(3,1)); p.add(r1); p.add(r2); p.add(r3); d.add(p);
            
            JButton save = new JButton("Αποθήκευση");
            save.addActionListener(e -> {
                jo.setAccessLevel(r1.isSelected()?"Πλήρης":(r2.isSelected()?"Μερική":"Προβολή"));
                updateUIFromAccount(); d.dispose();
            });
            d.add(save); d.setVisible(true);
        }

        //4.ΑΦΑΙΡΕΣΗ
        private void removeSelectedOwner() {
            int row = jointOwnersTable.getSelectedRow();
            if(row != -1) {
                String afm = (String) jointOwnersModel.getValueAt(row, 2);
                account.removeJointOwner(afm);
                updateUIFromAccount();
            } else JOptionPane.showMessageDialog(this, "Επιλέξτε συνδικαιούχο.");
        }

        //5.ΔΙΑΓΡΑΦΗ ΛΟΓΑΡΙΑΣΜΟΥ
        private void deleteAccountLogic() {
            if(account.getBalance() > 0) { JOptionPane.showMessageDialog(this, "Μηδενίστε το υπόλοιπο πρώτα."); return; }
            String otp = JOptionPane.showInputDialog(this, "OTP (1234):");
            if("1234".equals(otp)) {
                BankDataStore.getInstance().getAccounts().remove(account);
                cardLayout.show(mainContainer, "LIST");
                listPanel.refreshData();
                JOptionPane.showMessageDialog(this, "Ο λογαριασμός διαγράφηκε.");
            } else JOptionPane.showMessageDialog(this, "Λάθος OTP.");
        }

        private JButton createActionButton(String text, java.awt.event.ActionListener al) {
            JButton b = new JButton(text); b.setAlignmentX(Component.CENTER_ALIGNMENT); b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35)); b.addActionListener(al); return b;
        }
    }

    //Helpers
    private void showOpenAccountDialog() {
        JComboBox<String> c = new JComboBox<>(new String[]{"Τρεχούμενος", "Ταμιευτηρίου"});
        if(JOptionPane.showConfirmDialog(this, c, "Νέος Λογαριασμός", JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION) {
            BankDataStore.getInstance().getAccounts().add(AccountFactory.createAccount((String)c.getSelectedItem(), "GR"+System.currentTimeMillis(), currentUser.getFullName(), 0.0));
            listPanel.refreshData();
        }
    }
    private void showConsolidateDialog() { JOptionPane.showMessageDialog(this, "Λειτουργία Ενοποίησης (Demo)"); }
    private JButton createStyledButton(String text, Color bg) { JButton b = new JButton(text); b.setBackground(bg); b.setForeground(Color.WHITE); b.setFocusPainted(false); b.setFont(new Font("Segoe UI", Font.BOLD, 12)); return b; }
}