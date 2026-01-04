package view.dashboard;

import controller.MainController;
import model.entities.User;
import utils.TimeManager;
import view.panels.AdminAccountsPanel;
import view.panels.AdminUsersPanel;
import view.panels.Refreshable;

import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends JPanel {
    private CardLayout cardLayout;
    private JPanel contentArea;
    // private MainFrame mainFrame; // ΔΙΑΓΡΑΦΗΚΕ (Δεν χρειάζεται πλέον)
    private JLabel dateLabel;
    
    // Ο Constructor δέχεται ΜΟΝΟ τον User
    public AdminDashboard(User adminUser) {
        setLayout(new BorderLayout());

        // TOP NAVIGATION BAR
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(40, 45, 55));
        topBar.setPreferredSize(new Dimension(1200, 60));

        // LEFT: Navigation Buttons
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        navPanel.setOpaque(false);
        
        navPanel.add(createNavButton("Διαχείριση Χρηστών", "USERS"));
        navPanel.add(createNavButton("Διαχείριση Λογαριασμών", "ACCOUNTS"));
        
        topBar.add(navPanel, BorderLayout.WEST);

        // RIGHT: Time Control and Logout
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        rightPanel.setOpaque(false);

        // 1. Time Display and Control
        dateLabel = new JLabel(TimeManager.getInstance().getFormattedDate());
        dateLabel.setForeground(Color.CYAN);
        dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        

        JButton simBtn = new JButton(">> Προσομοίωση");
        simBtn.setBackground(new Color(0, 100, 100));
        simBtn.setForeground(Color.WHITE);
        simBtn.setFocusPainted(false);
        simBtn.addActionListener(e -> promptAdvanceTime());

        // 2. Logout Button
        JButton logoutBtn = new JButton("Αποσύνδεση");
        logoutBtn.setBackground(new Color(180, 0, 0));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        
        // --- MEDIATOR PATTERN: Κλήση Controller ---
        logoutBtn.addActionListener(e -> MainController.getInstance().onLogout());

        rightPanel.add(dateLabel);
        rightPanel.add(simBtn);
        rightPanel.add(Box.createHorizontalStrut(20));
        rightPanel.add(logoutBtn);

        topBar.add(rightPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // CONTENT AREA
        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);

        contentArea.add(new AdminUsersPanel(), "USERS");
        contentArea.add(new AdminAccountsPanel(), "ACCOUNTS");

        add(contentArea, BorderLayout.CENTER);
    }

    private JButton createNavButton(String text, String key) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(200, 40));
        btn.setBackground(new Color(60, 65, 75));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.addActionListener(e -> {
            cardLayout.show(contentArea, key);
            refreshCurrentPanel();
        });
        return btn;
    }

    // ΜΕ POPUP
    private void promptAdvanceTime() {
        String input = JOptionPane.showInputDialog(this, 
                "Πόσες ημέρες θέλετε να προχωρήσετε;", 
                "Προσομοίωση Χρόνου", 
                JOptionPane.QUESTION_MESSAGE);

        if (input == null || input.trim().isEmpty()) return;

        try {
            int days = Integer.parseInt(input);
            
            if (days <= 0) {
                JOptionPane.showMessageDialog(this, "Παρακαλώ δώστε θετικό αριθμό ημερών.");
                return;
            }
            
            for (int i = 0; i < days; i++) {
                TimeManager.getInstance().advanceTime();
            }

            dateLabel.setText(TimeManager.getInstance().getFormattedDate());
            refreshCurrentPanel();
            
            JOptionPane.showMessageDialog(this, 
                "Η προσομοίωση ολοκληρώθηκε.\nΠροχώρησαν " + days + " ημέρες.", 
                "Επιτυχία", JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Μη έγκυρος αριθμός.", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void refreshCurrentPanel() {
        for(Component c : contentArea.getComponents()) {
            if (c.isVisible() && c instanceof Refreshable) {
                ((Refreshable)c).refresh();
            }
        }
    }
}