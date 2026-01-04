package view.panels;

import data.BankDataStore;
import model.entities.Account;
import model.entities.Bill;
import service.command.PaymentCommand;
import utils.TimeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PaymentsPanel extends JPanel implements Refreshable {
    
    private CardLayout cardLayout;
    private JPanel mainContainer;
    private model.entities.User currentUser;
    
    // Sub-Panels
    private JPanel menuPanel;
    private NewPaymentPanel newPaymentPanel;
    private PendingBillsPanel pendingBillsPanel;
    
    private static final DecimalFormat df = new DecimalFormat("0.00");
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yy");

    public PaymentsPanel(model.entities.User user) {
        this.currentUser = user;
        setLayout(new BorderLayout());
        
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        
        initMenuPanel();
        newPaymentPanel = new NewPaymentPanel();
        pendingBillsPanel = new PendingBillsPanel();
        
        mainContainer.add(menuPanel, "MENU");
        mainContainer.add(newPaymentPanel, "NEW_PAYMENT");
        mainContainer.add(pendingBillsPanel, "PENDING");
        
        add(mainContainer, BorderLayout.CENTER);
        cardLayout.show(mainContainer, "MENU");
    }

    private void initMenuPanel() {
        menuPanel = new JPanel(new GridBagLayout());
        menuPanel.setBackground(Color.WHITE);
        
        JButton btnNew = createBigButton("Νέα Πληρωμή (RF)");
        JButton btnPending = createBigButton("Εκκρεμείς Οφειλές");
        
        btnNew.addActionListener(e -> {
            newPaymentPanel.refresh();
            cardLayout.show(mainContainer, "NEW_PAYMENT");
        });
        
        btnPending.addActionListener(e -> {
            pendingBillsPanel.refresh();
            cardLayout.show(mainContainer, "PENDING");
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 30, 20, 30);
        menuPanel.add(btnNew, gbc);
        menuPanel.add(btnPending, gbc);
    }
    
    private JButton createBigButton(String text) {
        JButton b = new JButton(text);
        b.setPreferredSize(new Dimension(250, 80));
        b.setBackground(new Color(40, 45, 55));
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 16));
        b.setFocusPainted(false);
        return b;
    }

    @Override
    public void refresh() {
        newPaymentPanel.refresh();
        pendingBillsPanel.refresh();
    }

    // 1. PANEL: ΝΕΑ ΠΛΗΡΩΜΗ (RF Form)
    private class NewPaymentPanel extends JPanel {
        private JTextField rfField, amountField;
        private JComboBox<String> accountBox;

        public NewPaymentPanel() {
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(20, 40, 20, 40));
            
            // Header
            JPanel head = new JPanel(new BorderLayout());
            head.setBackground(Color.WHITE);
            JLabel t = new JLabel("Νέα Πληρωμή με RF"); 
            t.setFont(new Font("Segoe UI", Font.PLAIN, 26));
            
            JButton back = new JButton("← Πίσω");
            back.addActionListener(e -> cardLayout.show(mainContainer, "MENU"));
            head.add(t, BorderLayout.WEST); head.add(back, BorderLayout.EAST);
            add(head, BorderLayout.NORTH);
            
            // Form
            JPanel form = new JPanel(new GridBagLayout());
            form.setBackground(Color.WHITE);
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(10, 10, 10, 10);
            g.fill = GridBagConstraints.HORIZONTAL; g.anchor = GridBagConstraints.WEST;
            
            g.gridx=0; g.gridy=0; form.add(createLabel("RF Κωδικός :"), g);
            g.gridx=0; g.gridy=1;
            rfField = new JTextField(20); 
            rfField.setPreferredSize(new Dimension(250, 40));
            rfField.setBorder(BorderFactory.createTitledBorder("Συμπληρώστε το RF code"));
            form.add(rfField, g);
            
            g.gridx=0; g.gridy=2; form.add(createLabel("Ποσό (€) :"), g);
            g.gridx=0; g.gridy=3;
            amountField = new JTextField(20); 
            amountField.setPreferredSize(new Dimension(250, 40));
            amountField.setBorder(BorderFactory.createTitledBorder("Γράψτε το ποσό πληρωμής"));
            form.add(amountField, g);
            
            g.gridx=1; g.gridy=0; form.add(createLabel("Από Λογαριασμό :"), g);
            g.gridx=1; g.gridy=1;
            accountBox = new JComboBox<>(); 
            accountBox.setPreferredSize(new Dimension(250, 40));
            form.add(accountBox, g);
            
            add(form, BorderLayout.CENTER);
            
            JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            btnP.setBackground(Color.WHITE);
            JButton payBtn = new JButton("Συνέχεια");
            payBtn.setBackground(new Color(40, 45, 55)); 
            payBtn.setForeground(Color.WHITE);
            payBtn.setPreferredSize(new Dimension(150, 40));
            payBtn.addActionListener(e -> onPayClicked());
            btnP.add(payBtn);
            add(btnP, BorderLayout.SOUTH);
        }
        
        public void refresh() {
            accountBox.removeAllItems();
            List<Account> myAccs = BankDataStore.getInstance().getAccountsForUser(currentUser);
            for(Account a : myAccs) {
                accountBox.addItem(a.getIban() + " (" + df.format(a.getBalance()) + "€)");
            }
        }
        
        private void onPayClicked() {
            if (currentUser.isLocked()) {
                JOptionPane.showMessageDialog(this, "Ο χρήστης είναι κλειδωμένος.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if(rfField.getText().isEmpty() || amountField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Συμπληρώστε τα πεδία."); return;
            }

            try {
                String rfInput = rfField.getText().trim();
                double amt = Double.parseDouble(amountField.getText());
                String srcIban = ((String)accountBox.getSelectedItem()).split(" ")[0];

                Bill foundBill = BankDataStore.getInstance().getBillByRf(rfInput);

                if (foundBill == null) {
                    JOptionPane.showMessageDialog(this, 
                        "Ο κωδικός RF δεν βρέθηκε ή ο λογαριασμός έχει ήδη εξοφληθεί.", 
                        "Άκυρο RF", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (!foundBill.getOwnerAfm().equals(currentUser.getAfm())) {
                    JOptionPane.showMessageDialog(this, 
                        "Αυτός ο κωδικός RF αφορά οφειλή άλλου χρήστη.\nΔεν έχετε δικαίωμα προβολής ή πληρωμής.", 
                        "Μη Εξουσιοδοτημένη Πρόσβαση", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                LocalDate today = TimeManager.getInstance().getDate();
                if (today.isBefore(foundBill.getIssueDate())) {
                    JOptionPane.showMessageDialog(this, 
                        "Δεν μπορείτε να πληρώσετε λογαριασμό πριν την ημερομηνία έκδοσής του.\n" +
                        "Ημ/νία Έκδοσης: " + foundBill.getIssueDate().format(dtf), 
                        "Σφάλμα Ημερομηνίας", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (amt != foundBill.getAmount()) {
                    JOptionPane.showMessageDialog(this, 
                        "Το ποσό πληρωμής πρέπει να είναι ακριβώς " + foundBill.getAmount() + "€", 
                        "Λάθος Ποσό", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                double limit = currentUser.getLimitPayment();
                if (amt > limit) {
                    JOptionPane.showMessageDialog(this, 
                        "Η πληρωμή ακυρώθηκε.\n" +
                        "Το ποσό (" + amt + "€) υπερβαίνει το όριο πληρωμών (" + limit + "€).", 
                        "Υπέρβαση Ορίου", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Account sourceAcc = BankDataStore.getInstance().getAccountByIban(srcIban);
                if (sourceAcc.getBalance() < amt) {
                      JOptionPane.showMessageDialog(this, "Ανεπαρκές Υπόλοιπο.", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
                      return;
                }

                showConfirmation(srcIban, foundBill.getProvider(), foundBill.getDescription(), amt, foundBill);
                
            } catch(NumberFormatException ex) { 
                JOptionPane.showMessageDialog(this, "Λάθος μορφή ποσού."); 
            }
        }
        
        private JLabel createLabel(String t) {
            JLabel l = new JLabel(t); l.setFont(new Font("Segoe UI", Font.BOLD, 14)); return l;
        }
    }

    // 2. PANEL: ΕΚΚΡΕΜΕΙΣ ΟΦΕΙΛΕΣ (Πίνακας)
    private class PendingBillsPanel extends JPanel {
        private JTable table;
        private DefaultTableModel model;
        private JComboBox<String> accBox;
        
        public PendingBillsPanel() {
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(20, 20, 20, 20));
            
            // Header
            JPanel head = new JPanel(new BorderLayout()); head.setBackground(Color.WHITE);
            JLabel t = new JLabel("Εκκρεμείς Πληρωμές"); t.setFont(new Font("Segoe UI", Font.BOLD, 22));
            JButton b = new JButton("← Πίσω"); b.addActionListener(e -> cardLayout.show(mainContainer, "MENU"));
            head.add(t, BorderLayout.WEST); head.add(b, BorderLayout.EAST);
            add(head, BorderLayout.NORTH);
            
            // Table
            String[] cols = {"RF", "Επιχείρηση", "Ποσό", "Αιτιολογία", "Λήξη", "Έκδοση"};
            model = new DefaultTableModel(cols, 0) { 
                @Override public boolean isCellEditable(int r, int c) { return false; } 
            };
            table = new JTable(model); 
            table.setRowHeight(30);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            add(new JScrollPane(table), BorderLayout.CENTER);
            
            // Footer
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT)); 
            footer.setBackground(Color.WHITE);
            
            footer.add(new JLabel("Πληρωμή Από: "));
            accBox = new JComboBox<>(); 
            accBox.setPreferredSize(new Dimension(250, 35));
            footer.add(accBox);
            
            JButton payBtn = new JButton("Πληρωμή"); 
            payBtn.setBackground(new Color(40, 45, 55)); 
            payBtn.setForeground(Color.WHITE);
            payBtn.setPreferredSize(new Dimension(150, 35));
            
            payBtn.addActionListener(e -> onPaySelected());
            footer.add(payBtn);
            
            add(footer, BorderLayout.SOUTH);
        }
        
        public void refresh() {
            model.setRowCount(0);
            List<Bill> allBills = BankDataStore.getInstance().getPendingBills();
            
            for(Bill b : allBills) {
                if (b.getOwnerAfm().equals(currentUser.getAfm())) {
                    model.addRow(new Object[]{
                        b.getRfCode(), 
                        b.getProvider(), 
                        df.format(b.getAmount()), 
                        b.getDescription(), 
                        b.getDueDate().format(dtf),
                        b.getIssueDate().format(dtf)
                    });
                }
            }
            
            accBox.removeAllItems();
            List<Account> myAccs = BankDataStore.getInstance().getAccountsForUser(currentUser);
            for(Account a : myAccs) {
                accBox.addItem(a.getIban() + " (" + df.format(a.getBalance()) + "€)");
            }
        }
        
        private void onPaySelected() {
            if (currentUser.isLocked()) {
                JOptionPane.showMessageDialog(this, "Ο χρήστης είναι κλειδωμένος.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int row = table.getSelectedRow();
            if(row == -1) { JOptionPane.showMessageDialog(this, "Επιλέξτε μια οφειλή."); return; }
            
            String rf = (String) model.getValueAt(row, 0);
            Bill selectedBill = BankDataStore.getInstance().getBillByRf(rf);
            
            if(selectedBill != null) {
                LocalDate today = TimeManager.getInstance().getDate();
                if (today.isBefore(selectedBill.getIssueDate())) {
                    JOptionPane.showMessageDialog(this, 
                        "Δεν μπορείτε να πληρώσετε λογαριασμό πριν την ημερομηνία έκδοσης.\n" +
                        "Ημ/νία Έκδοσης: " + selectedBill.getIssueDate().format(dtf), 
                        "Σφάλμα Ημερομηνίας", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                double limit = currentUser.getLimitPayment();
                if (selectedBill.getAmount() > limit) {
                    JOptionPane.showMessageDialog(this, 
                        "Η πληρωμή ακυρώθηκε. Υπέρβαση ορίου (" + limit + "€).", 
                        "Υπέρβαση Ορίου", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String src = ((String)accBox.getSelectedItem()).split(" ")[0];
                showConfirmation(src, selectedBill.getProvider(), selectedBill.getDescription(), selectedBill.getAmount(), selectedBill);
            }
        }
    }
    
    private void showConfirmation(String srcIban, String providerName, String desc, double amount, Bill bill) {
        JDialog d = new JDialog((Frame)null, "Επιβεβαίωση Πληρωμής", true);
        d.setSize(400, 350); 
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout()); 
        d.getContentPane().setBackground(Color.WHITE);
        
        JPanel p = new JPanel(new GridBagLayout()); 
        p.setBackground(Color.WHITE);
        GridBagConstraints g = new GridBagConstraints(); 
        g.insets = new Insets(5,10,5,10); 
        g.anchor = GridBagConstraints.WEST;
        
        addRow(p, g, 0, "Από:", srcIban);
        addRow(p, g, 1, "Προς:", providerName);
        addRow(p, g, 2, "Ποσό:", df.format(amount) + " €");
        addRow(p, g, 3, "Αιτιολογία:", desc);
        addRow(p, g, 4, "Προμήθεια:", "0.00 €"); 
        
        g.gridy=5; g.gridx=0; 
        JLabel l = new JLabel("Σύνολο:"); l.setFont(new Font("Segoe UI", Font.BOLD, 14)); 
        p.add(l, g);
        g.gridx=1; 
        p.add(new JLabel(df.format(amount) + " €"), g);
        
        d.add(p, BorderLayout.CENTER);
        
        JPanel b = new JPanel(); b.setBackground(Color.WHITE);
        JButton ok = new JButton("Επιβεβαίωση");
        ok.addActionListener(e -> {
            
            new PaymentCommand(srcIban, providerName, amount, desc, bill).execute();
            
            if (bill != null) {
                BankDataStore.getInstance().getPendingBills().remove(bill);
                BankDataStore.getInstance().saveAllData(); 
            }
            
            d.dispose();
            
            newPaymentPanel.refresh();
            pendingBillsPanel.refresh();
            
            newPaymentPanel.rfField.setText("");
            newPaymentPanel.amountField.setText("");
            
            JOptionPane.showMessageDialog(this, "Η πληρωμή ολοκληρώθηκε επιτυχώς!"); 
        });
        
        JButton cancel = new JButton("Ακύρωση");
        cancel.addActionListener(e -> d.dispose());
        
        b.add(cancel); b.add(ok);
        d.add(b, BorderLayout.SOUTH);
        d.setVisible(true);
    }
    
    private void addRow(JPanel p, GridBagConstraints g, int r, String l, String v) {
        g.gridy=r; g.gridx=0; 
        JLabel lbl = new JLabel(l); lbl.setFont(new Font("Segoe UI", Font.BOLD, 12)); 
        p.add(lbl, g);
        g.gridx=1; p.add(new JLabel(v), g);
    }
}