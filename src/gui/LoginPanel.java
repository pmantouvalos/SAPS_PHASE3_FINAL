package gui;

import service.BankDataStore;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import bridge.EmailSender;
import bridge.MessageSender;
import bridge.Notification;
import bridge.SecurityNotification;
import utils.TimeManager;

public class LoginPanel extends JPanel {

    private MainFrame mainFrame;
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginPanel(MainFrame frame) {
        this.mainFrame = frame;
        setLayout(new GridLayout(1, 2)); //50-50 Split

        //ΑΡΙΣΤΕΡΟ PANEL(ΛΟΓΟΤΥΠΟ)
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(new Color(112, 128, 144)); 
        leftPanel.setLayout(new GridBagLayout()); 

        JLabel logoLabel = new JLabel();
        
        String imagePath = "src/resources/logo.png";
        ImageIcon icon = new ImageIcon(imagePath);

        if (icon.getImageLoadStatus() == MediaTracker.COMPLETE && icon.getIconWidth() > 0) {
            Image img = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(img));
        } else {
            logoLabel.setText("<html><div style='font-size:60px; color:white; border: 2px solid white; padding: 20px;'>🏛️</div></html>");
        }
        
        JLabel brandLabel = new JLabel("<html><span style='color:#3399FF'>Bank</span> <span style='color:white'>Of</span> <span style='color:#CC0000'>TUC</span></html>");
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        leftPanel.add(logoLabel, gbc);
        gbc.gridy = 1; gbc.insets = new Insets(20, 0, 0, 0);
        leftPanel.add(brandLabel, gbc);

        //ΔΕΞΙ PANEL
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 30, 10, 30);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0; 
        
        //Username
        JLabel userLbl = new JLabel("Όνομα χρήστη");
        userLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        usernameField = new JTextField();
        usernameField.setPreferredSize(new Dimension(250, 40));
        addPlaceholder(usernameField, "Εισάγετε το όνομα χρήστη σας");
        
        //Password
        JLabel passLbl = new JLabel("Κωδικός Πρόσβασης");
        passLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(250, 40));
        addPlaceholder(passwordField, "Εισάγετε τον κωδικό πρόσβασης");
        passwordField.setEchoChar((char) 0); 

        //Buttons
        JButton forgotBtn = new JButton("Ξέχασα τον κωδικό πρόσβασης");
        forgotBtn.setBorderPainted(false);
        forgotBtn.setContentAreaFilled(false);
        forgotBtn.setForeground(Color.GRAY);
        forgotBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotBtn.setHorizontalAlignment(SwingConstants.LEFT);
        
        forgotBtn.addActionListener(e -> onForgotPassword());

        JButton loginBtn = new JButton("Σύνδεση");
        loginBtn.setBackground(new Color(100, 100, 100)); 
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginBtn.setPreferredSize(new Dimension(150, 45));
        loginBtn.setFocusPainted(false);
        loginBtn.addActionListener(e -> attemptLogin());

        //Layout Adding
        g.gridy = 0; rightPanel.add(userLbl, g);
        g.gridy = 1; rightPanel.add(usernameField, g);
        g.gridy = 2; g.insets = new Insets(20, 30, 10, 30); rightPanel.add(passLbl, g);
        g.gridy = 3; g.insets = new Insets(0, 30, 5, 30); rightPanel.add(passwordField, g);
        g.gridy = 4; g.insets = new Insets(0, 25, 20, 30); rightPanel.add(forgotBtn, g);
        g.gridy = 5; g.insets = new Insets(10, 30, 10, 30); g.fill = GridBagConstraints.NONE; g.anchor = GridBagConstraints.WEST;
        rightPanel.add(loginBtn, g);

        add(leftPanel);
        add(rightPanel);
    }

    private void onForgotPassword() {
        //1.Ζητάμε το Username
        String username = JOptionPane.showInputDialog(this, "Βήμα 1/3: Εισάγετε το όνομα χρήστη:");
        if (username == null || username.trim().isEmpty()) return;

        User user = BankDataStore.getInstance().getUser(username);
        
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Ο χρήστης δεν βρέθηκε.", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
            return;
        }

        //2.Ζητάμε το ΑΦΜ για ταυτοποίηση
        String afm = JOptionPane.showInputDialog(this, "Βήμα 2/3: Για ταυτοποίηση, εισάγετε το ΑΦΜ σας:");
        if (afm == null) return;

        if (user.getAfm().equals(afm)) {
            //3.Αν ταιριάζει το ΑΦΜ, ζητάμε νέο κωδικό
            String newPass = JOptionPane.showInputDialog(this, "Βήμα 3/3: Εισάγετε τον ΝΕΟ κωδικό πρόσβασης:");
            
            if (newPass != null && !newPass.trim().isEmpty()) {
                user.setPassword(newPass);
                JOptionPane.showMessageDialog(this, "Ο κωδικός άλλαξε επιτυχώς!\nΜπορείτε να συνδεθείτε.", "Επιτυχία", JOptionPane.INFORMATION_MESSAGE);
                
                //Καθαρίζουμε τα πεδία για να μπει με τα νέα στοιχεία
                usernameField.setText(username);
                usernameField.setForeground(Color.BLACK);
                passwordField.setText("");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Το ΑΦΜ δεν ταιριάζει με τον χρήστη.", "Αποτυχία Ταυτοποίησης", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void attemptLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.equals("Εισάγετε το όνομα χρήστη σας")) username = "";
        
        User user = BankDataStore.getInstance().getUser(username);

        if (user != null && user.getPassword().equals(password)) {
            if (user.isLocked()) {
                JOptionPane.showMessageDialog(this, "Ο λογαριασμός είναι κλειδωμένος.", "Access Denied", JOptionPane.ERROR_MESSAGE);
                return;
            } 
            
            BankDataStore.getInstance().setLoggedUser(user);
            
            mainFrame.login(user);
            
            if (user.isNotifyLogin()) {
                //1.Διαλέγουμε τον "Implementor" (Πώς θα το στείλουμε;) -> Email
                MessageSender sender = new EmailSender();
                
                //2.Διαλέγουμε το "Abstraction" (Τι είδους ειδοποίηση είναι;) -> Security
                Notification securityAlert = new SecurityNotification(sender);
                
                //3.Στέλνουμε το μήνυμα
                String date = TimeManager.getInstance().getFormattedDate();
                String msgContent = "Αγαπητέ/ή <b>" + user.getFullName() + "</b>,<br>" +
                                  "Εντοπίστηκε νέα σύνδεση στις: <b>" + date + "</b>.";
                
                securityAlert.send(msgContent);
            }
           
            
            usernameField.setText("");
            usernameField.setForeground(Color.BLACK);
            passwordField.setText("");
            
        } else {
            JOptionPane.showMessageDialog(this, "Λάθος όνομα χρήστη ή κωδικός.", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
        }
    }

   
    private void addPlaceholder(JTextField field, String text) {
        field.setText(text);
        field.setForeground(Color.GRAY);
        field.addFocusListener(new FocusListener() {
            @Override public void focusGained(FocusEvent e) {
                if (field.getText().equals(text)) {
                    field.setText(""); field.setForeground(Color.BLACK);
                    if (field instanceof JPasswordField) ((JPasswordField) field).setEchoChar('●');
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(Color.GRAY); field.setText(text);
                    if (field instanceof JPasswordField) ((JPasswordField) field).setEchoChar((char) 0);
                }
            }
        });
    }
}