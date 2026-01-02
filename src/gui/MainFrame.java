package gui;

import model.Role;
import model.User;
import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private User currentUser;

    public MainFrame() {
        setTitle("E-banking Bank Of TUC");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        //Αρχική οθόνη Login
        mainPanel.add(new LoginPanel(this), "LOGIN");

        add(mainPanel);
        cardLayout.show(mainPanel, "LOGIN");
    }

    public void login(User user) {
        this.currentUser = user;
        
        if (user.getRole() == Role.ADMIN) {
            //Περνάμε το user στον constructor του AdminDashboard
            mainPanel.add(new AdminDashboard(this, user), "ADMIN");
            cardLayout.show(mainPanel, "ADMIN");
        } else {
            //Για απλούς χρήστες
            mainPanel.add(new ClientDashboard(this, user), "CLIENT");
            cardLayout.show(mainPanel, "CLIENT");
        }
    }

    public void logout() {
        this.currentUser = null;
        cardLayout.show(mainPanel, "LOGIN");
    }
}