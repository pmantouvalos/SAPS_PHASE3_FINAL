package view.panels;

import model.entities.Account;
import model.entities.StandingOrder;
import model.entities.User;
import service.factory.StandingOrderFactory; // Import Factory
import data.BankDataStore;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StandingOrdersPanel extends JPanel implements Refreshable {
    
    private CardLayout cardLayout;
    private JPanel mainContainer;
    private User currentUser; 
    
    // Sub-Panels
    private ListPanel listPanel;
    private CreateOrderPanel createPanel;
    private EditOrderPanel editPanel;
    
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yy");

    public StandingOrdersPanel(User user) {
        this.currentUser = user;
        
        setLayout(new BorderLayout());
        
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        
        listPanel = new ListPanel();
        createPanel = new CreateOrderPanel();
        editPanel = new EditOrderPanel();
        
        mainContainer.add(listPanel, "LIST");
        mainContainer.add(createPanel, "CREATE");
        mainContainer.add(editPanel, "EDIT");
        
        add(mainContainer, BorderLayout.CENTER);
        cardLayout.show(mainContainer, "LIST");
        
        refresh();
    }

    @Override
    public void refresh() {
        listPanel.refreshData();
        createPanel.refreshAccountCombo();
    }

    // 1. PANEL: ΛΙΣΤΑ ΠΑΓΙΩΝ ΕΝΤΟΛΩΝ
    private class ListPanel extends JPanel {
        private JTable table;
        private DefaultTableModel model;

        public ListPanel() {
            setLayout(new BorderLayout(10, 10));
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(20, 20, 20, 20));

            // Header
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(Color.WHITE);
            JLabel title = new JLabel("Οι Πάγιες Εντολές μου");
            title.setFont(new Font("Segoe UI", Font.BOLD, 24));
            
            JButton createBtn = new JButton("Δημιουργία Νέας Πάγιας Εντολής");
            createBtn.setBackground(new Color(40, 45, 55));
            createBtn.setForeground(Color.WHITE);
            createBtn.setFocusPainted(false);
            createBtn.addActionListener(e -> {
                createPanel.refreshAccountCombo();
                cardLayout.show(mainContainer, "CREATE");
            });
            
            header.add(title, BorderLayout.WEST);
            header.add(createBtn, BorderLayout.EAST);
            add(header, BorderLayout.NORTH);

            // Table
            String[] cols = {"Είδος", "Από", "Προς", "Ποσό", "Αιτιολογία", "Συχνότητα", "Κατάσταση", "Έναρξη", "Λήξη"};
            model = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            table = new JTable(model);
            table.setRowHeight(30);
            table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
            add(new JScrollPane(table), BorderLayout.CENTER);

            // Action Buttons
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            actions.setBackground(Color.WHITE);
            
            JButton editBtn = new JButton("Επεξεργασία");
            editBtn.addActionListener(e -> onEditSelected());
            
            JButton delBtn = new JButton("Διαγραφή");
            delBtn.setBackground(new Color(180, 50, 50));
            delBtn.setForeground(Color.WHITE);
            delBtn.addActionListener(e -> onDeleteSelected());
            
            actions.add(editBtn);
            actions.add(delBtn);
            add(actions, BorderLayout.SOUTH);
        }

        public void refreshData() {
            model.setRowCount(0); 
            
            List<StandingOrder> allOrders = BankDataStore.getInstance().getStandingOrders();
            List<Account> myAccounts = BankDataStore.getInstance().getAccountsForUser(currentUser);
            
            java.util.List<String> myIbans = new java.util.ArrayList<>();
            for (Account acc : myAccounts) {
                myIbans.add(acc.getIban());
            }

            for (StandingOrder so : allOrders) {
                if (myIbans.contains(so.getSourceIban())) {
                    model.addRow(new Object[]{
                        so.getType(),
                        so.getSourceIban(),
                        so.getTarget(),
                        String.format("%.2f €", so.getAmount()),
                        so.getDescription(),
                        so.getFrequencyDays() + " μέρες",
                        so.isActive() ? "Ενεργή" : "Ανενεργή",
                        so.getStartDate().format(dtf),
                        so.getEndDate().format(dtf)
                    });
                }
            }
        }

        private void onEditSelected() {
            int row = table.getSelectedRow();
            if (row != -1) {
                // Προσοχή: Εδώ χρειάζεται αντιστοίχιση γιατί το table δείχνει φιλτραρισμένα
                // Για απλότητα παίρνουμε την πρώτη που ταιριάζει με τα κριτήρια (IBAN + Target + Amount)
                // Σωστότερο θα ήταν να αποθηκεύουμε το ID, αλλά για τώρα:
                String src = (String)model.getValueAt(row, 1);
                String trg = (String)model.getValueAt(row, 2);
                
                StandingOrder so = BankDataStore.getInstance().getStandingOrders().stream()
                        .filter(s -> s.getSourceIban().equals(src) && s.getTarget().equals(trg))
                        .findFirst().orElse(null);
                        
                if(so != null) {
                    editPanel.loadOrder(so);
                    cardLayout.show(mainContainer, "EDIT");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Παρακαλώ επιλέξτε μια εντολή.");
            }
        }

        private void onDeleteSelected() {
            int row = table.getSelectedRow();
            if (row != -1) {
                int confirm = JOptionPane.showConfirmDialog(this, "Είστε σίγουροι για τη διαγραφή;", "Διαγραφή", JOptionPane.YES_NO_OPTION);
                if(confirm == JOptionPane.YES_OPTION) {
                    String src = (String)model.getValueAt(row, 1);
                    String trg = (String)model.getValueAt(row, 2);
                    
                    BankDataStore.getInstance().getStandingOrders().removeIf(s -> s.getSourceIban().equals(src) && s.getTarget().equals(trg));
                    refreshData();
                    BankDataStore.getInstance().saveAllData();
                    JOptionPane.showMessageDialog(this, "Η εντολή διαγράφηκε.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Παρακαλώ επιλέξτε μια εντολή.");
            }
        }
    }

    // 2. PANEL: ΔΗΜΙΟΥΡΓΙΑ ΝΕΑΣ ΠΑΓΙΑΣ ΕΝΤΟΛΗΣ
    private class CreateOrderPanel extends JPanel {
        private JComboBox<String> typeBox, accBox;
        private JTextField targetF, amountF, descF, startF, endF, freqF;

        public CreateOrderPanel() {
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(20, 40, 20, 40));

            // Header
            JPanel head = new JPanel(new BorderLayout());
            head.setBackground(Color.WHITE);
            JLabel t = new JLabel("Δημιουργία Νέας Πάγιας Εντολής");
            t.setFont(new Font("Segoe UI", Font.PLAIN, 26));
            
            JButton back = new JButton("Ακύρωση");
            back.addActionListener(e -> cardLayout.show(mainContainer, "LIST"));
            
            head.add(t, BorderLayout.WEST); head.add(back, BorderLayout.EAST);
            add(head, BorderLayout.NORTH);

            // Form
            JPanel form = new JPanel(new GridLayout(1, 2, 40, 0));
            form.setBackground(Color.WHITE);
            
            JPanel left = new JPanel(new GridLayout(6, 1, 10, 10));
            left.setBackground(Color.WHITE);
            
            typeBox = new JComboBox<>(new String[]{"Μεταφορά", "Πληρωμή"});
            left.add(createLabeledField("Επιλέξτε Τύπο Πάγιας:", typeBox));
            
            targetF = new JTextField();
            left.add(createLabeledField("Συμπληρώστε RF code / IBAN παραλήπτη:", targetF));
            
            accBox = new JComboBox<>();
            left.add(createLabeledField("Επιλέξτε Λογαριασμό προέλευσης:", accBox));

            JPanel right = new JPanel(new GridLayout(6, 1, 10, 10));
            right.setBackground(Color.WHITE);
            
            amountF = new JTextField();
            right.add(createLabeledField("Γράψτε το ποσό (€):", amountF));
            
            descF = new JTextField();
            right.add(createLabeledField("Γράψτε αιτιολογία:", descF));
            
            startF = new JTextField(); startF.setBorder(BorderFactory.createTitledBorder("Ημερομηνία Έναρξης (dd/MM/yy)"));
            right.add(startF);
            
            endF = new JTextField(); endF.setBorder(BorderFactory.createTitledBorder("Ημερομηνία Λήξης (dd/MM/yy)"));
            right.add(endF);
            
            freqF = new JTextField();
            right.add(createLabeledField("Επανάληψη ανά πόσες μέρες (π.χ. 30):", freqF));

            form.add(left);
            form.add(right);
            add(form, BorderLayout.CENTER);

            // Button
            JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            btnP.setBackground(Color.WHITE);
            JButton createBtn = new JButton("Δημιουργία");
            createBtn.setBackground(new Color(40, 45, 55));
            createBtn.setForeground(Color.WHITE);
            createBtn.setPreferredSize(new Dimension(150, 40));
            createBtn.addActionListener(e -> onCreateClicked());
            btnP.add(createBtn);
            add(btnP, BorderLayout.SOUTH);
        }
        
        public void refreshAccountCombo() {
            accBox.removeAllItems();
            if (currentUser != null) {
                List<Account> myAccs = BankDataStore.getInstance().getAccountsForUser(currentUser);
                for(Account a : myAccs) {
                    accBox.addItem(a.getIban()); 
                }
            }
        }
        
        private void onCreateClicked() {
            try {
                String type = (String) typeBox.getSelectedItem();
                String src = (String) accBox.getSelectedItem();
                String trg = targetF.getText().trim();
                
                if (src == null || src.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Παρακαλώ επιλέξτε λογαριασμό προέλευσης.");
                    return;
                }

                double amt = Double.parseDouble(amountF.getText());
                String desc = descF.getText();
                int freq = Integer.parseInt(freqF.getText());
                
                LocalDate start = LocalDate.parse(startF.getText(), dtf);
                LocalDate end = LocalDate.parse(endF.getText(), dtf);
                
                LocalDate simDate = utils.TimeManager.getInstance().getDate();
                if (start.isBefore(simDate)) {
                    JOptionPane.showMessageDialog(this, "Η ημερομηνία έναρξης δεν μπορεί να είναι στο παρελθόν!");
                    return;
                }
                if (end.isBefore(start)) {
                    JOptionPane.showMessageDialog(this, "Η ημερομηνία λήξης είναι πριν την έναρξη.");
                    return;
                }
                if (freq <= 0) {
                    JOptionPane.showMessageDialog(this, "Η συχνότητα πρέπει να είναι τουλάχιστον 1 ημέρα.");
                    return;
                }

                // ΧΡΗΣΗ FACTORY
                StandingOrder so = StandingOrderFactory.createOrder(type, src, trg, amt, desc);
                so.setFrequencyDays(freq);
                so.setStartDate(start);
                so.setEndDate(end);
                
                BankDataStore.getInstance().getStandingOrders().add(so);
                BankDataStore.getInstance().saveAllData();
                
                JOptionPane.showMessageDialog(this, "Η πάγια εντολή δημιουργήθηκε!");
                listPanel.refreshData();
                cardLayout.show(mainContainer, "LIST");
                
                // Reset
                targetF.setText(""); amountF.setText(""); descF.setText(""); 
                startF.setText(""); endF.setText(""); freqF.setText("");
                
            } catch(Exception ex) {
                JOptionPane.showMessageDialog(this, "Σφάλμα: " + ex.getMessage());
            }
        }
        
        private JPanel createLabeledField(String label, JComponent comp) {
            JPanel p = new JPanel(new BorderLayout());
            p.setBackground(Color.WHITE);
            p.add(new JLabel(label), BorderLayout.NORTH);
            p.add(comp, BorderLayout.CENTER);
            return p;
        }
    }

    // 3. PANEL: ΕΠΕΞΕΡΓΑΣΙΑ
    private class EditOrderPanel extends JPanel {
        private JTextField typeF, srcF, targetF, descF;
        private JTextField amountF, startF, endF, freqF;
        private JRadioButton activeBtn, inactiveBtn;
        private StandingOrder currentOrder;

        public EditOrderPanel() {
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(20, 40, 20, 40));
            
            JPanel head = new JPanel(new BorderLayout());
            head.setBackground(Color.WHITE);
            JLabel t = new JLabel("Επεξεργασία Πάγιας Εντολής");
            t.setFont(new Font("Segoe UI", Font.PLAIN, 26));
            JButton back = new JButton("Ακύρωση");
            back.addActionListener(e -> cardLayout.show(mainContainer, "LIST"));
            head.add(t, BorderLayout.WEST); head.add(back, BorderLayout.EAST);
            add(head, BorderLayout.NORTH);
            
            JPanel form = new JPanel(new GridLayout(1, 2, 40, 0));
            form.setBackground(Color.WHITE);
            
            JPanel left = new JPanel(new GridLayout(5, 1, 10, 10));
            left.setBackground(Color.WHITE);
            
            typeF = new JTextField(); typeF.setEditable(false);
            left.add(createLabeled("Ο τύπος πάγιας δεν αλλάζει:", typeF));
            
            srcF = new JTextField(); srcF.setEditable(false);
            left.add(createLabeled("Ο λογαριασμός προέλευσης δεν αλλάζει:", srcF));
            
            targetF = new JTextField(); targetF.setEditable(false);
            left.add(createLabeled("RF code/ IBAN παραλήπτη:", targetF));
            
            descF = new JTextField(); descF.setEditable(false);
            left.add(createLabeled("Αιτιολογία:", descF));
            
            amountF = new JTextField();
            left.add(createLabeled("Ποσό:", amountF));
            
            JPanel right = new JPanel(new GridLayout(5, 1, 10, 10));
            right.setBackground(Color.WHITE);
            
            startF = new JTextField();
            right.add(createLabeled("Ημερομηνία Έναρξης:", startF));
            
            endF = new JTextField();
            right.add(createLabeled("Ημερομηνία Λήξης:", endF));
            
            freqF = new JTextField();
            right.add(createLabeled("Συχνότητα (μέρες):", freqF));
            
            JPanel statusP = new JPanel(new FlowLayout(FlowLayout.LEFT));
            statusP.setBackground(Color.WHITE);
            activeBtn = new JRadioButton("Ενεργή");
            inactiveBtn = new JRadioButton("Ανενεργή");
            ButtonGroup bg = new ButtonGroup(); bg.add(activeBtn); bg.add(inactiveBtn);
            statusP.add(activeBtn); statusP.add(inactiveBtn);
            
            JPanel wrapStatus = new JPanel(new BorderLayout());
            wrapStatus.setBackground(Color.WHITE);
            wrapStatus.add(new JLabel("Κατάσταση:"), BorderLayout.NORTH);
            wrapStatus.add(statusP, BorderLayout.CENTER);
            right.add(wrapStatus);

            form.add(left); form.add(right);
            add(form, BorderLayout.CENTER);
            
            JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            btnP.setBackground(Color.WHITE);
            JButton saveBtn = new JButton("Αποθήκευση");
            saveBtn.setBackground(new Color(40, 45, 55));
            saveBtn.setForeground(Color.WHITE);
            saveBtn.setPreferredSize(new Dimension(150, 40));
            saveBtn.addActionListener(e -> onSaveClicked());
            btnP.add(saveBtn);
            add(btnP, BorderLayout.SOUTH);
        }
        
        public void loadOrder(StandingOrder so) {
            this.currentOrder = so;
            typeF.setText(so.getType());
            srcF.setText(so.getSourceIban());
            targetF.setText(so.getTarget());
            descF.setText(so.getDescription());
            amountF.setText(String.valueOf(so.getAmount()));
            startF.setText(so.getStartDate().format(dtf));
            endF.setText(so.getEndDate().format(dtf));
            freqF.setText(String.valueOf(so.getFrequencyDays()));
            
            if(so.isActive()) activeBtn.setSelected(true); else inactiveBtn.setSelected(true);
        }
        
        private void onSaveClicked() {
            try {
                currentOrder.setAmount(Double.parseDouble(amountF.getText()));
                currentOrder.setStartDate(LocalDate.parse(startF.getText(), dtf));
                currentOrder.setEndDate(LocalDate.parse(endF.getText(), dtf));
                currentOrder.setFrequencyDays(Integer.parseInt(freqF.getText()));
                currentOrder.setActive(activeBtn.isSelected());
                
                BankDataStore.getInstance().saveAllData();
                
                JOptionPane.showMessageDialog(this, "Η επεξεργασία ολοκληρώθηκε");
                listPanel.refreshData();
                cardLayout.show(mainContainer, "LIST");
                
            } catch(Exception ex) {
                JOptionPane.showMessageDialog(this, "Λάθος δεδομένα: " + ex.getMessage());
            }
        }
        
        private JPanel createLabeled(String label, JComponent comp) {
            JPanel p = new JPanel(new BorderLayout());
            p.setBackground(Color.WHITE);
            p.add(new JLabel(label), BorderLayout.NORTH);
            p.add(comp, BorderLayout.CENTER);
            return p;
        }
    }
}