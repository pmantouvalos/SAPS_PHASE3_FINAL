package view;

import javax.swing.*;
import java.awt.*;

// Imports για τα Panels και Entities
import view.auth.LoginPanel;
import view.dashboard.AdminDashboard;
import view.dashboard.ClientDashboard;
import model.entities.User;

public class MainFrame extends JFrame {
    
    private CardLayout cardLayout;
    private JPanel mainPanel;

    public MainFrame() {
        setTitle("E-banking Bank Of TUC");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        // Αρχικοποίηση Layout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Προσθήκη του Login Panel αρχικά
        mainPanel.add(new LoginPanel(), "LOGIN");

        add(mainPanel);
        
        // Εμφάνιση της Login οθόνης
        cardLayout.show(mainPanel, "LOGIN");
    }

    // --- Μέθοδοι Πλοήγησης (Καλoύνται από τον MainController) ---

    public void showLoginPanel() {
        cardLayout.show(mainPanel, "LOGIN");
    }

    public void showAdminDashboard(User user) {
        // Δημιουργία νέου instance AdminDashboard περνώντας ΜΟΝΟ τον User
        // Προϋπόθεση: Ο constructor του AdminDashboard πρέπει να είναι: public AdminDashboard(User user)
        mainPanel.add(new AdminDashboard(user), "ADMIN");
        cardLayout.show(mainPanel, "ADMIN");
    }

    public void showClientDashboard(User user) {
        // Δημιουργία νέου instance ClientDashboard περνώντας ΜΟΝΟ τον User
        // Προϋπόθεση: Ο constructor του ClientDashboard πρέπει να είναι: public ClientDashboard(User user)
        mainPanel.add(new ClientDashboard(user), "CLIENT");
        cardLayout.show(mainPanel, "CLIENT");
    }
}