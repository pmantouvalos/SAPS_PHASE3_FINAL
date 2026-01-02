package gui.panels;

import model.Account;
import model.Transaction;
import service.BankDataStore;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HistoryPanel extends JPanel implements Refreshable {
    
    private CardLayout cardLayout;
    private JPanel mainContainer;
    private model.User currentUser;
    
    //Panels
    private ListPanel listPanel;
    private SearchPanel searchPanel;
    
    //Δεδομένα
    private List<Transaction> allTransactions;
    private List<Transaction> displayedTransactions;
    
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yy");

    public HistoryPanel(model.User user) {
        this.currentUser = user;
        setLayout(new BorderLayout());
        
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        
        // Init Lists
        allTransactions = new ArrayList<>();
        displayedTransactions = new ArrayList<>();
        
        listPanel = new ListPanel();
        searchPanel = new SearchPanel();
        
        mainContainer.add(listPanel, "LIST");
        mainContainer.add(searchPanel, "SEARCH");
        
        add(mainContainer, BorderLayout.CENTER);
        cardLayout.show(mainContainer, "LIST");
    }

    @Override
    public void refresh() {
        //1.Συγκέντρωση κινήσεων από ΟΛΟΥΣ τους σχετικούς λογαριασμούς
        allTransactions.clear();
        
        for (Account a : BankDataStore.getInstance().getAccounts()) {
            //Είμαι Ιδιοκτήτης;
            boolean isOwner = a.getOwnerName().equals(currentUser.getFullName());
            //Είμαι Συνδικαιούχος; (Έλεγχος βάσει ΑΦΜ)
            boolean isJoint = a.getJointOwners().stream()
                    .anyMatch(jo -> jo.getAfm().equals(currentUser.getAfm()));

            if (isOwner || isJoint) {
                allTransactions.addAll(a.getTransactions());
            }
        }
        
        //2.Ταξινόμηση: Πρώτα φθίνουσα Ημερομηνία, μετά φθίνουσα Ώρα (Timestamp)
        allTransactions.sort((t1, t2) -> {
            int dateCmp = t2.getDate().compareTo(t1.getDate());
            if (dateCmp != 0) return dateCmp;
            return Long.compare(t2.getTimestamp(), t1.getTimestamp());
        });
        
        //3.Επαναφορά λίστας προβολής
        displayedTransactions.clear();
        displayedTransactions.addAll(allTransactions);
        
        listPanel.updateTable();
    }

    //1.PANEL: ΛΙΣΤΑ ΚΙΝΗΣΕΩΝ (Πίνακας)
    private class ListPanel extends JPanel {
        private JTable table;
        private DefaultTableModel model;
        
        public ListPanel() {
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(20, 20, 20, 20));
            setBackground(Color.WHITE);
            
            //Header
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(Color.WHITE);
            JLabel title = new JLabel("Οι Κινήσεις μου");
            title.setFont(new Font("Segoe UI", Font.BOLD, 24));
            title.setForeground(new Color(80, 80, 80));
            
            JButton searchBtn = new JButton("Αναζήτηση");
            searchBtn.setBackground(new Color(40, 45, 55));
            searchBtn.setForeground(Color.WHITE);
            searchBtn.setFocusPainted(false);
            searchBtn.addActionListener(e -> cardLayout.show(mainContainer, "SEARCH"));
            
            header.add(title, BorderLayout.WEST);
            header.add(searchBtn, BorderLayout.EAST);
            add(header, BorderLayout.NORTH);
            
            //Table
            String[] cols = {"Είδος", "Από", "Προς", "Ποσό", "Αιτιολογία", "Ημερομηνία", "Προμήθεια", "Υπόλοιπο"};
            model = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            table = new JTable(model);
            table.setRowHeight(30);
            table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
            
            add(new JScrollPane(table), BorderLayout.CENTER);
        }
        
        public void updateTable() {
            model.setRowCount(0);
            for(Transaction t : displayedTransactions) {
                model.addRow(new Object[]{
                    t.getType(),
                    t.getSender(),
                    t.getReceiver(),
                    String.format("%.2f €", t.getAmount()),
                    t.getDescription(),
                    t.getDate().format(dtf),
                    String.format("%.2f €", t.getFee()),
                    String.format("%.2f €", t.getBalanceAfter())
                });
            }
        }
    }


    //2.PANEL: ΑΝΑΖΗΤΗΣΗ (Φίλτρα)
    private class SearchPanel extends JPanel {
        //Checkboxes
        private JCheckBox depositChk, withdrawChk, transferChk, paymentChk;
        //Dates
        private JTextField fromDateF, toDateF;
        
        public SearchPanel() {
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(20, 40, 20, 40));
            
            //Title
            JLabel title = new JLabel("Αναζήτηση Συναλλαγών");
            title.setFont(new Font("Segoe UI", Font.PLAIN, 26));
            title.setBorder(new EmptyBorder(0, 0, 30, 0));
            add(title, BorderLayout.NORTH);
            
            //Main Content Form
            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setBackground(Color.WHITE);
            
            //1.Checkboxes Row (Τύποι Συναλλαγών)
            JPanel checks = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
            checks.setBackground(Color.WHITE);
            
            depositChk = createCheck("Κατάθεση");
            withdrawChk = createCheck("Ανάληψη");
            transferChk = createCheck("Μεταφορά");
            paymentChk = createCheck("Πληρωμή");
            
            //Default selected
            depositChk.setSelected(true); withdrawChk.setSelected(true); 
            transferChk.setSelected(true); paymentChk.setSelected(true);
            
            checks.add(depositChk); checks.add(withdrawChk); 
            checks.add(transferChk); checks.add(paymentChk);
            
            content.add(checks);
            content.add(Box.createVerticalStrut(40));
            
            //2.Date Range Row
            JPanel dates = new JPanel(new GridLayout(1, 2, 20, 0));
            dates.setBackground(Color.WHITE);
            dates.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
            
            //From
            JPanel p1 = new JPanel(new BorderLayout()); p1.setBackground(Color.WHITE);
            p1.add(new JLabel("Από Ημερομηνία :"), BorderLayout.NORTH);
            fromDateF = new JTextField(); 
            fromDateF.setBorder(BorderFactory.createTitledBorder("π.χ. 01/01/25"));
            p1.add(fromDateF, BorderLayout.CENTER);
            
            //To
            JPanel p2 = new JPanel(new BorderLayout()); p2.setBackground(Color.WHITE);
            p2.add(new JLabel("Έως Ημερομηνία :"), BorderLayout.NORTH);
            toDateF = new JTextField();
            toDateF.setBorder(BorderFactory.createTitledBorder("π.χ. 31/12/25"));
            p2.add(toDateF, BorderLayout.CENTER);
            
            dates.add(p1); dates.add(p2);
            content.add(dates);
            
            add(content, BorderLayout.CENTER);
            
            //Bottom Actions
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            btnPanel.setBackground(Color.WHITE);
            
            JButton cancelBtn = new JButton("Ακύρωση");
            cancelBtn.addActionListener(e -> cardLayout.show(mainContainer, "LIST"));
            
            JButton execSearch = new JButton("Αναζήτηση");
            execSearch.setBackground(new Color(40, 45, 55));
            execSearch.setForeground(Color.WHITE);
            execSearch.setPreferredSize(new Dimension(120, 35));
            execSearch.addActionListener(e -> applyFilters());
            
            btnPanel.add(cancelBtn);
            btnPanel.add(execSearch);
            add(btnPanel, BorderLayout.SOUTH);
        }
        
        private JCheckBox createCheck(String txt) {
            JCheckBox c = new JCheckBox(txt);
            c.setBackground(Color.WHITE);
            c.setFont(new Font("Segoe UI", Font.BOLD, 14));
            return c;
        }
        
        private void applyFilters() {
            //1.Get Selected Types
            List<String> types = new ArrayList<>();
            if(depositChk.isSelected()) types.add("Κατάθεση");
            if(withdrawChk.isSelected()) types.add("Ανάληψη");
            if(transferChk.isSelected()) types.add("Μεταφορά");
            if(paymentChk.isSelected()) types.add("Πληρωμή");
            
            //2.Parse Dates
            LocalDate from = null, to = null;
            try {
                if(!fromDateF.getText().trim().isEmpty()) from = LocalDate.parse(fromDateF.getText(), dtf);
                if(!toDateF.getText().trim().isEmpty()) to = LocalDate.parse(toDateF.getText(), dtf);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Λάθος μορφή ημερομηνίας (dd/MM/yy)");
                return;
            }
            
            //3.Filter Logic
            LocalDate finalFrom = from;
            LocalDate finalTo = to;
            
            displayedTransactions = allTransactions.stream()
                .filter(t -> types.contains(t.getType()))
                .filter(t -> {
                    if (finalFrom != null && t.getDate().isBefore(finalFrom)) return false;
                    if (finalTo != null && t.getDate().isAfter(finalTo)) return false;
                    return true;
                })
                .collect(Collectors.toList());
            
            //4.Update UI and Switch back
            listPanel.updateTable();
            cardLayout.show(mainContainer, "LIST");
        }
    }
}