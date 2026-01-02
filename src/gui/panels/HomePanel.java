package gui.panels;

import model.Account;
import model.Transaction;
import service.BankDataStore;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HomePanel extends JPanel implements Refreshable {
    private DefaultTableModel accModel, trxModel;
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yy");
    private model.User currentUser; //Χρειαζόμαστε τον χρήστη

    public HomePanel(model.User user) {
        this.currentUser = user; //Αποθήκευση χρήστη
        setLayout(new GridLayout(2, 1, 10, 20));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        // Accounts Table
        JPanel p1 = new JPanel(new BorderLayout()); p1.setBackground(Color.WHITE);
        JLabel l1 = new JLabel("Με μια ματιά - Οι λογαριασμοί μου"); l1.setFont(new Font("Segoe UI", Font.BOLD, 18));
        p1.add(l1, BorderLayout.NORTH);
        accModel = new DefaultTableModel(new String[]{"IBAN", "Τύπος", "Διαθέσιμο Υπόλοιπο"}, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        JTable accTable = new JTable(accModel); accTable.setRowHeight(30);
        p1.add(new JScrollPane(accTable), BorderLayout.CENTER);

        // Transactions Table
        JPanel p2 = new JPanel(new BorderLayout()); p2.setBackground(Color.WHITE);
        JLabel l2 = new JLabel("Οι τελευταίες συναλλαγές μου"); l2.setFont(new Font("Segoe UI", Font.BOLD, 18));
        p2.add(l2, BorderLayout.NORTH);
        trxModel = new DefaultTableModel(new String[]{"Είδος", "Από", "Προς", "Ποσό", "Αιτιολογία", "Ημερομηνία", "Προμήθεια", "Υπόλοιπο"}, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        JTable trxTable = new JTable(trxModel); trxTable.setRowHeight(30);
        p2.add(new JScrollPane(trxTable), BorderLayout.CENTER);

        add(p1); add(p2);
        refresh();
    }

    @Override
    public void refresh() {
        accModel.setRowCount(0);
        List<Transaction> allTransactions = new ArrayList<>();
        
        //ΑΝΑΖΗΤΗΣΗ ΣΕ ΟΛΟΥΣ ΤΟΥΣ ΛΟΓΑΡΙΑΣΜΟΥΣ
        for (Account a : BankDataStore.getInstance().getAccounts()) {
            boolean isOwner = a.getOwnerName().equals(currentUser.getFullName());
            boolean isJoint = a.getJointOwners().stream().anyMatch(jo -> jo.getAfm().equals(currentUser.getAfm()));

            //Αν είμαι Ιδιοκτήτης Η' Συνδικαιούχος
            if (isOwner || isJoint) {
                accModel.addRow(new Object[]{ a.getIban(), a.getAccountType(), String.format("%.2f €", a.getBalance()) });
                allTransactions.addAll(a.getTransactions());
            }
        }

        trxModel.setRowCount(0);
        allTransactions.sort((t1, t2) -> {
            int d = t2.getDate().compareTo(t1.getDate());
            if (d != 0) return d;
            return Long.compare(t2.getTimestamp(), t1.getTimestamp());
        });

        int limit = Math.min(allTransactions.size(), 5);
        for (int i = 0; i < limit; i++) {
            Transaction t = allTransactions.get(i);
            trxModel.addRow(new Object[]{ t.getType(), t.getSender(), t.getReceiver(), String.format("%.2f", t.getAmount()), t.getDescription(), t.getDate().format(dtf), String.format("%.2f", t.getFee()), String.format("%.2f", t.getBalanceAfter()) });
        }
    }
}