package gui.panels;

import model.Bill;
import model.User;
import service.BankDataStore;
import utils.TimeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class IssueBillsPanel extends JPanel implements Refreshable {
    
    private User currentUser;
    private JTextField afmField, amountField, descField, dateField;
    
    //Στοιχεία για τον πίνακα ιστορικού
    private JTable issuedTable;
    private DefaultTableModel tableModel;
    
    public IssueBillsPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(0, 20)); //Κενό μεταξύ πάνω και κάτω μέρους
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 40, 20, 40));
        
        //HEADER
        JLabel title = new JLabel("Διαχείριση Οφειλών Πελατών");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(new Color(0, 51, 102));
        add(title, BorderLayout.NORTH);
        
        //SPLIT PANE: FORM (Top) kai TABLE (Bottom)
        JPanel mainContent = new JPanel(new GridLayout(2, 1, 0, 20));
        mainContent.setBackground(Color.WHITE);
        
        //1.FORM PANEL (Φόρμα Έκδοσης)
        JPanel formPanel = new JPanel(new BorderLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder("Έκδοση Νέας Οφειλής"));
        
        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setBackground(Color.WHITE);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 10, 5, 10); 
        g.anchor = GridBagConstraints.WEST; 
        g.fill = GridBagConstraints.HORIZONTAL;
        
        //ΑΦΜ
        g.gridx=0; g.gridy=0; formGrid.add(new JLabel("ΑΦΜ Πελάτη:"), g);
        g.gridx=1; afmField = new JTextField(20); formGrid.add(afmField, g);
        
        //Ποσό
        g.gridx=0; g.gridy=1; formGrid.add(new JLabel("Ποσό (€):"), g);
        g.gridx=1; amountField = new JTextField(20); formGrid.add(amountField, g);
        
        //Αιτιολογία (ΑΥΤΟΜΑΤΗ ΣΥΜΠΛΗΡΩΣΗ)
        g.gridx=0; g.gridy=2; formGrid.add(new JLabel("Αιτιολογία:"), g);
        g.gridx=1; 
        descField = new JTextField(20);
        //Προσυμπλήρωση με: "Λογαριασμός  [Όνομα Επιχείρησης]"
        descField.setText("Λογαριασμός  " + currentUser.getFullName());
        formGrid.add(descField, g);
        
        //Ημερομηνία Λήξης
        g.gridx=0; g.gridy=3; formGrid.add(new JLabel("Λήξη (dd/MM/yy):"), g);
        g.gridx=1; dateField = new JTextField(20); 
        dateField.setText(TimeManager.getInstance().getDate().plusMonths(1).format(DateTimeFormatter.ofPattern("dd/MM/yy")));
        formGrid.add(dateField, g);
        
        formPanel.add(formGrid, BorderLayout.CENTER);
        
        JButton issueBtn = new JButton("Έκδοση Λογαριασμού");
        issueBtn.setBackground(new Color(0, 100, 0)); 
        issueBtn.setForeground(Color.WHITE);
        issueBtn.addActionListener(e -> onIssueClicked());
        
        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT)); 
        btnP.setBackground(Color.WHITE);
        btnP.add(issueBtn);
        formPanel.add(btnP, BorderLayout.SOUTH);
        
        mainContent.add(formPanel);
        
        //2.TABLE PANEL (Πίνακας Απλήρωτων Λογαριασμών της Επιχείρησης)
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBackground(Color.WHITE);
        listPanel.setBorder(BorderFactory.createTitledBorder("Ιστορικό Εκδοθέντων (Απλήρωτοι)"));
        
        String[] cols = {"RF Code", "Αιτιολογία", "Ποσό", "Ημ. Λήξης"};
        tableModel = new DefaultTableModel(cols, 0) { 
            @Override public boolean isCellEditable(int r, int c) { return false; } 
        };
        issuedTable = new JTable(tableModel);
        issuedTable.setRowHeight(25);
        listPanel.add(new JScrollPane(issuedTable), BorderLayout.CENTER);
        
        mainContent.add(listPanel);
        
        add(mainContent, BorderLayout.CENTER);
        
        refreshData();
    }
    
    public void refreshData() {
        //Γέμισμα πίνακα με λογαριασμούς που έχει εκδώσει ΑΥΤΗ η επιχείρηση και δεν έχουν πληρωθεί
        tableModel.setRowCount(0);
        for(Bill b : BankDataStore.getInstance().getPendingBills()) {
            //Ελέγχουμε αν ο εκδότης (provider) είναι η τρέχουσα επιχείρηση
            if(b.getProvider().equals(currentUser.getFullName())) {
                tableModel.addRow(new Object[]{
                    b.getRfCode(), b.getDescription(), b.getAmount() + "€", b.getDueDate()
                });
            }
        }
    }
    
    private void onIssueClicked() {
        if(afmField.getText().isEmpty() || amountField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Παρακαλώ συμπληρώστε ΑΦΜ και Ποσό.");
            return;
        }
        try {
            double amt = Double.parseDouble(amountField.getText());
            // Δημιουργία τυχαίου RF
            String rf = "RF" + (1000000000 + new Random().nextInt(900000000));
            LocalDate due = LocalDate.parse(dateField.getText(), DateTimeFormatter.ofPattern("dd/MM/yy"));
            
            String autoDescription = "Λογαριασμός " + currentUser.getFullName();

            Bill newBill = new Bill(
                afmField.getText().trim(),           //1.ΑΦΜ Πελάτη (από το πεδίο που προσθέσαμε)
                rf,                                  //2.RF Code
                currentUser.getFullName(),           //3.Πάροχος (Η εταιρεία)
                amt,                                 //4.Ποσό
                autoDescription,                     //5.Περιγραφή (ΑΥΤΟΜΑΤΗ)
                TimeManager.getInstance().getDate(), //6.Ημερομηνία Έκδοσης
                due                                  //7.Ημερομηνία Λήξης
            );
            //Προσθήκη στο κεντρικό Store (για να το βλέπουν οι πελάτες και να εμφανίζεται στον πίνακα από κάτω)
            BankDataStore.getInstance().getPendingBills().add(newBill);
            
            JOptionPane.showMessageDialog(this, "Επιτυχής Έκδοση!\nRF Κωδικός: " + rf);
            
            //Καθαρισμός πεδίων και επαναφορά της default αιτιολογίας
            afmField.setText(""); 
            amountField.setText(""); 
            descField.setText("Λογαριασμός  " + currentUser.getFullName());
            
            refreshData(); //Ανανέωση του πίνακα από κάτω
            
        } catch(Exception ex) { 
            JOptionPane.showMessageDialog(this, "Λάθος μορφή δεδομένων (ελέγξτε ποσό και ημερομηνία)."); 
        }
    }

    @Override
    public void refresh() {
        refreshData();
    }
}