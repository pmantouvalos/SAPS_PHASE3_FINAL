package view.panels;

import model.entities.User;
import model.enums.Role;
import data.BankDataStore;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminUsersPanel extends JPanel implements Refreshable {
    private JTextField searchField;
    private JTable usersTable;
    private DefaultTableModel model;

    public AdminUsersPanel() {
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // SEARCH BAR
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Αναζήτηση (Username/ΑΦΜ):"));
        searchField = new JTextField(20);
        JButton searchBtn = new JButton("Αναζήτηση");
        searchBtn.addActionListener(e -> refreshData(searchField.getText()));
        top.add(searchField);
        top.add(searchBtn);
        
        JButton showAllBtn = new JButton("Προβολή Όλων");
        showAllBtn.addActionListener(e -> { searchField.setText(""); refreshData(""); });
        top.add(showAllBtn);
        add(top, BorderLayout.NORTH);

        // TABLE
        String[] cols = {"Username", "Ονοματεπώνυμο", "ΑΦΜ", "Ρόλος", "Κατάσταση"};
        model = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        usersTable = new JTable(model);
        usersTable.setRowHeight(25);
        add(new JScrollPane(usersTable), BorderLayout.CENTER);

        // --- ACTIONS ---
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        JButton editBtn = new JButton("Επεξεργασία Προφίλ");
        editBtn.addActionListener(e -> editUser());
        
        JButton lockBtn = new JButton("Κλείδωμα / Ξεκλείδωμα");
        lockBtn.setBackground(Color.ORANGE);
        lockBtn.addActionListener(e -> toggleLock());

        JButton deleteBtn = new JButton("Διαγραφή Χρήστη");
        deleteBtn.setBackground(Color.RED);
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.addActionListener(e -> deleteUser());

        actions.add(editBtn); actions.add(lockBtn); actions.add(deleteBtn);
        add(actions, BorderLayout.SOUTH);

        refreshData("");
    }

    private void refreshData(String query) {
        model.setRowCount(0);
        List<User> users = BankDataStore.getInstance().getUsers();
        
        for (User u : users) {
            // Φίλτρο Αναζήτησης
            if (!query.isEmpty()) {
                boolean match = u.getUsername().contains(query) || u.getAfm().contains(query) || u.getFullName().contains(query);
                if (!match) continue;
            }
            
            model.addRow(new Object[]{
                u.getUsername(), u.getFullName(), u.getAfm(), u.getRole(), 
                u.isLocked() ? "ΚΛΕΙΔΩΜΕΝΟΣ" : "Ενεργός"
            });
        }
    }

    private User getSelectedUser() {
        int row = usersTable.getSelectedRow();
        if (row == -1) return null;
        String uname = (String) model.getValueAt(row, 0);
        return BankDataStore.getInstance().getUser(uname);
    }

    private void editUser() {
        User u = getSelectedUser();
        if (u == null) { JOptionPane.showMessageDialog(this, "Επιλέξτε χρήστη."); return; }

        JTextField nameF = new JTextField(u.getFullName());
        JTextField afmF = new JTextField(u.getAfm());
        JTextField emailF = new JTextField(u.getEmail());
        JTextField phoneF = new JTextField(u.getPhone());

        Object[] msg = { "Όνομα:", nameF, "ΑΦΜ:", afmF, "Email:", emailF, "Τηλέφωνο:", phoneF };

        int res = JOptionPane.showConfirmDialog(this, msg, "Επεξεργασία Προφίλ", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            u.setAfm(afmF.getText());
            u.setEmail(emailF.getText());
            u.setPhone(phoneF.getText());
            refreshData(searchField.getText());
            JOptionPane.showMessageDialog(this, "Τα στοιχεία ενημερώθηκαν.");
        }
    }

    private void toggleLock() {
        User u = getSelectedUser();
        if (u == null) { JOptionPane.showMessageDialog(this, "Επιλέξτε χρήστη."); return; }
        
        if (u.getRole() == Role.ADMIN) { JOptionPane.showMessageDialog(this, "Δεν μπορείτε να κλειδώσετε Admin."); return; }

        u.setLocked(!u.isLocked());
        refreshData(searchField.getText());
        JOptionPane.showMessageDialog(this, "Η κατάσταση του χρήστη άλλαξε.");
    }

    private void deleteUser() {
        User u = getSelectedUser();
        if (u == null) { JOptionPane.showMessageDialog(this, "Επιλέξτε χρήστη."); return; }
        if (u.getRole() == Role.ADMIN) { JOptionPane.showMessageDialog(this, "Αδυναμία διαγραφής Admin."); return; }

        int confirm = JOptionPane.showConfirmDialog(this, "Είστε σίγουρος; Θα διαγραφούν και οι λογαριασμοί του.", "Διαγραφή", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            BankDataStore.getInstance().getUsers().remove(u);
            refreshData(searchField.getText());
        }
    }

    @Override
    public void refresh() { refreshData(searchField.getText()); }
}