package gui.panels;

import model.Account;
import model.StandingOrder;
import model.User; // Χρειάζεται import
import service.BankDataStore;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List; // Χρειάζεται import

public class StandingOrdersPanel extends JPanel implements Refreshable {
    
    private CardLayout cardLayout;
    private JPanel mainContainer;
    
    //Αποθήκευση του τρέχοντος χρήστη για να βρούμε τους λογαριασμούς του
    private User currentUser; 
    
    //Τα 3 υπο-panels
    private ListPanel listPanel;
    private CreateOrderPanel createPanel;
    private EditOrderPanel editPanel;
    
    //Format ημερομηνίας
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yy");

    public StandingOrdersPanel(User user) {
        this.currentUser = user;
        
        setLayout(new BorderLayout());
        
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        
        //Αρχικοποίηση
        listPanel = new ListPanel();
        createPanel = new CreateOrderPanel();
        editPanel = new EditOrderPanel();
        
        //Προσθήκη στο CardLayout
        mainContainer.add(listPanel, "LIST");
        mainContainer.add(createPanel, "CREATE");
        mainContainer.add(editPanel, "EDIT");
        
        add(mainContainer, BorderLayout.CENTER);
        cardLayout.show(mainContainer, "LIST");
        
        //Πρώτο γέμισμα δεδομένων
        refresh();
    }

    @Override
    public void refresh() {
        listPanel.refreshData();
        createPanel.refreshAccountCombo();
    }

    //1.PANEL: ΛΙΣΤΑ ΠΑΓΙΩΝ ΕΝΤΟΛΩΝ

    private class ListPanel extends JPanel {
        private JTable table;
        private DefaultTableModel model;

        public ListPanel() {
            setLayout(new BorderLayout(10, 10));
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(20, 20, 20, 20));

            //Header
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(Color.WHITE);
            JLabel title = new JLabel("Οι Πάγιες Εντολές μου");
            title.setFont(new Font("Segoe UI", Font.BOLD, 24));
            
            //Κουμπί "Δημιουργία Νέας"
            JButton createBtn = new JButton("Δημιουργία Νέας Πάγιας Εντολής");
            createBtn.setBackground(new Color(40, 45, 55));
            createBtn.setForeground(Color.WHITE);
            createBtn.setFocusPainted(false);
            createBtn.addActionListener(e -> {
                createPanel.refreshAccountCombo(); //Βεβαιωνόμαστε ότι ενημερώνεται πριν ανοίξει
                cardLayout.show(mainContainer, "CREATE");
            });
            
            header.add(title, BorderLayout.WEST);
            header.add(createBtn, BorderLayout.EAST);
            add(header, BorderLayout.NORTH);

            //Table
            String[] cols = {"Είδος", "Από", "Προς", "Ποσό", "Αιτιολογία", "Συχνότητα", "Κατάσταση", "Έναρξη", "Λήξη"};
            model = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            table = new JTable(model);
            table.setRowHeight(30);
            table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
            add(new JScrollPane(table), BorderLayout.CENTER);

            //Action Buttons
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
            model.setRowCount(0); //Καθαρισμός πίνακα
            
            //1.Παίρνουμε ΟΛΕΣ τις πάγιες εντολές
            java.util.List<StandingOrder> allOrders = BankDataStore.getInstance().getStandingOrders();
            
            //2.Παίρνουμε τους λογαριασμούς του τρέχοντος χρήστη
            //(Χρησιμοποιούμε τον currentUser της εξωτερικής κλάσης StandingOrdersPanel)
            java.util.List<Account> myAccounts = BankDataStore.getInstance().getAccountsForUser(currentUser);
            
            //3.Φτιάχνουμε μια λίστα με τα IBAN του χρήστη για γρήγορο έλεγχο
            java.util.List<String> myIbans = new java.util.ArrayList<>();
            for (Account acc : myAccounts) {
                myIbans.add(acc.getIban());
            }

            //4.Φιλτράρισμα και Προσθήκη στον Πίνακα
            for (StandingOrder so : allOrders) {
                
                //ΚΡΙΣΙΜΟΣ ΕΛΕΓΧΟΣ
                //Εμφανίζουμε την εντολή ΜΟΝΟ αν το Source IBAN ανήκει στον χρήστη
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
                StandingOrder so = BankDataStore.getInstance().getStandingOrders().get(row);
                editPanel.loadOrder(so);
                cardLayout.show(mainContainer, "EDIT");
            } else {
                JOptionPane.showMessageDialog(this, "Παρακαλώ επιλέξτε μια εντολή.");
            }
        }

        private void onDeleteSelected() {
            int row = table.getSelectedRow();
            if (row != -1) {
                int confirm = JOptionPane.showConfirmDialog(this, "Είστε σίγουροι για τη διαγραφή;", "Διαγραφή", JOptionPane.YES_NO_OPTION);
                if(confirm == JOptionPane.YES_OPTION) {
                    BankDataStore.getInstance().getStandingOrders().remove(row);
                    refreshData();
                    //Αποθήκευση αλλαγών
                    BankDataStore.getInstance().saveAllData();
                    JOptionPane.showMessageDialog(this, "Η εντολή διαγράφηκε.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Παρακαλώ επιλέξτε μια εντολή.");
            }
        }
    }

    //2.PANEL: ΔΗΜΙΟΥΡΓΙΑ ΝΕΑΣ ΠΑΓΙΑΣ ΕΝΤΟΛΗΣ
    private class CreateOrderPanel extends JPanel {
        private JComboBox<String> typeBox, accBox;
        private JTextField targetF, amountF, descF, startF, endF, freqF;

        public CreateOrderPanel() {
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(20, 40, 20, 40));

            //Header
            JPanel head = new JPanel(new BorderLayout());
            head.setBackground(Color.WHITE);
            JLabel t = new JLabel("Δημιουργία Νέας Πάγιας Εντολής");
            t.setFont(new Font("Segoe UI", Font.PLAIN, 26));
            
            JButton back = new JButton("Ακύρωση");
            back.addActionListener(e -> cardLayout.show(mainContainer, "LIST"));
            
            head.add(t, BorderLayout.WEST); head.add(back, BorderLayout.EAST);
            add(head, BorderLayout.NORTH);

            //Φόρμα
            JPanel form = new JPanel(new GridLayout(1, 2, 40, 0));
            form.setBackground(Color.WHITE);
            
            //Αριστερή Στήλη
            JPanel left = new JPanel(new GridLayout(6, 1, 10, 10));
            left.setBackground(Color.WHITE);
            
            typeBox = new JComboBox<>(new String[]{"Μεταφορά", "Πληρωμή"});
            left.add(createLabeledField("Επιλέξτε Τύπο Πάγιας:", typeBox));
            
            targetF = new JTextField();
            left.add(createLabeledField("Συμπληρώστε RF code / IBAN παραλήπτη:", targetF));
            
            accBox = new JComboBox<>();
            left.add(createLabeledField("Επιλέξτε Λογαριασμό προέλευσης:", accBox));

            //Δεξιά Στήλη
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

            //Κουμπί Δημιουργία
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
            //Χρήση του currentUser για να βρούμε τους λογαριασμούς του
            if (currentUser != null) {
                List<Account> myAccs = BankDataStore.getInstance().getAccountsForUser(currentUser);
                for(Account a : myAccs) {
                    //Εμφάνιση IBAN και υπολοίπου για ευκολία
                    accBox.addItem(a.getIban()); 
                }
            }
        }
        
        private void onCreateClicked() {
            try {
                String type = (String) typeBox.getSelectedItem();
                String src = (String) accBox.getSelectedItem();
                String trg = targetF.getText().trim(); // RF ή IBAN
                
                if (src == null || src.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Παρακαλώ επιλέξτε λογαριασμό προέλευσης.");
                    return;
                }

                double amt = Double.parseDouble(amountF.getText());
                String desc = descF.getText();
                int freq = Integer.parseInt(freqF.getText());
                
                LocalDate start = LocalDate.parse(startF.getText(), dtf);
                LocalDate end = LocalDate.parse(endF.getText(), dtf);
                
                //ΒΑΣΙΚΟΙ ΕΛΕΓΧΟΙ ΗΜΕΡΟΜΗΝΙΩΝ
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
                //ΕΛΕΓΧΟΙ ΓΙΑ ΠΛΗΡΩΜΗ ΛΟΓΑΡΙΑΣΜΩΝ (RF CODES)
                if (trg.startsWith("RF")) {
                    
                    //1.Έλεγχος: Υπάρχει ο λογαριασμός και είναι απλήρωτος;
                    //Η getBillByRf ψάχνει ΜΟΝΟ στους 'pendingBills' (εκκρεμείς).
                    //Αν επιστρέψει null, σημαίνει ότι είτε δεν υπάρχει, είτε ΕΧΕΙ ΗΔΗ ΠΛΗΡΩΘΕΙ.
                    model.Bill bill = BankDataStore.getInstance().getBillByRf(trg);
                    
                    if (bill == null) {
                        JOptionPane.showMessageDialog(this, 
                            "ΣΦΑΛΜΑ: Ο λογαριασμός με αυτό το RF Code δεν βρέθηκε στις οφειλές σας.\n" +
                            "Ελέγξτε τον κωδικό ή βεβαιωθείτε ότι δεν έχει ήδη εξοφληθεί.", 
                            "Άκυρος Λογαριασμός", JOptionPane.ERROR_MESSAGE);
                        return; // Διακοπή
                    }

                    //2. Έλεγχος: Πληρωμή πριν την Έκδοση
                    //Δεν επιτρέπουμε η εντολή να ξεκινάει ΠΡΙΝ την ημερομηνία έκδοσης του λογαριασμού.
                    if (start.isBefore(bill.getIssueDate())) {
                        JOptionPane.showMessageDialog(this, 
                            "ΣΦΑΛΜΑ: Δεν μπορείτε να προγραμματίσετε πληρωμή πριν την έκδοση του λογαριασμού.\n" +
                            "Ημ/νία Έκδοσης Λογαριασμού: " + bill.getIssueDate().format(dtf),
                            "Σφάλμα Ημερομηνίας", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    //3. Έλεγχος: Απαγόρευση Επανάληψης (Recurring) για το ίδιο RF
                    //Αν η διάρκεια (Start -> End) είναι μεγαλύτερη από τη συχνότητα, σημαίνει ότι
                    //η εντολή θα προσπαθήσει να τρέξει πάνω από μία φορά.
                    long daysDuration = java.time.temporal.ChronoUnit.DAYS.between(start, end);
                    if (daysDuration >= freq) {
                        JOptionPane.showMessageDialog(this, 
                            "ΣΦΑΛΜΑ: Οι λογαριασμοί (RF) είναι μοναδικοί και εξοφλούνται εφάπαξ.\n" +
                            "Δεν μπορείτε να ορίσετε επαναλαμβανόμενη εντολή για το ίδιο RF Code.\n" +
                            "Συμβουλή: Βάλτε Ημ/νία Λήξης ίδια με την Έναρξη.", 
                            "Μη Εγκυρη Επανάληψη", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                // Αν όλα ΟΚ, δημιουργία=
                StandingOrder so = new StandingOrder(type, src, trg, amt, desc, freq, start, end);
                BankDataStore.getInstance().getStandingOrders().add(so);
                
                // Αποθήκευση
                BankDataStore.getInstance().saveAllData();
                
                JOptionPane.showMessageDialog(this, "Η πάγια εντολή δημιουργήθηκε!");
                listPanel.refreshData();
                cardLayout.show(mainContainer, "LIST");
                
                // Reset fields
                targetF.setText(""); amountF.setText(""); descF.setText(""); 
                startF.setText(""); endF.setText(""); freqF.setText("");
                
            } catch(NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Λάθος αριθμητική τιμή.");
            } catch(java.time.format.DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Λάθος ημερομηνία. Μορφή: dd/MM/yy");
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

    // 3. PANEL: ΕΠΕΞΕΡΓΑΣΙΑ ΠΑΓΙΑΣ ΕΝΤΟΛΗΣ
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
                
                //Αποθήκευση
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