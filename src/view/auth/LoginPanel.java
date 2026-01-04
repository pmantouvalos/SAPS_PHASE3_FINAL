package view.auth;

import controller.MainController;
import data.BankDataStore;
import model.entities.User;
import service.notification.SecurityNotification;
import service.notification.sender.EmailSender;
import utils.TimeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.net.URL;

public class LoginPanel extends JPanel {

    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginPanel() {
        setLayout(new GridLayout(1, 2)); // 50-50 Split

        // --- ΑΡΙΣΤΕΡΟ PANEL (ΛΟΓΟΤΥΠΟ) ---
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(new Color(112, 128, 144)); 
        leftPanel.setLayout(new GridBagLayout()); 

        JLabel logoLabel = new JLabel();
        
        // --- ΦΟΡΤΩΣΗ ΕΙΚΟΝΑΣ ---
        // Προσπαθεί να φορτώσει το logo.png από το src/resources/
        boolean loaded = loadLogo(logoLabel, "logo.png");
        
        // Αν δεν βρει την εικόνα, βάζει το Emoji ως Fallback
        if (!loaded) {
            logoLabel.setText("<html><div style='font-size:60px; color:white; border: 2px solid white; padding: 20px;'>🏛️</div></html>");
        }
        
        JLabel brandLabel = new JLabel("<html><span style='color:#3399FF'>Bank</span> <span style='color:white'>Of</span> <span style='color:#CC0000'>TUC</span></html>");
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        leftPanel.add(logoLabel, gbc);
        gbc.gridy = 1; gbc.insets = new Insets(20, 0, 0, 0);
        leftPanel.add(brandLabel, gbc);

        // --- ΔΕΞΙ PANEL (LOGIN FORM) ---
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 30, 10, 30);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0; 
        
        // Username
        JLabel userLbl = new JLabel("Όνομα χρήστη");
        userLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        usernameField = new JTextField();
        usernameField.setPreferredSize(new Dimension(250, 40));
        addPlaceholder(usernameField, "Εισάγετε το όνομα χρήστη σας");
        
        // Password
        JLabel passLbl = new JLabel("Κωδικός Πρόσβασης");
        passLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(250, 40));
        addPlaceholder(passwordField, "Εισάγετε τον κωδικό πρόσβασης");
        passwordField.setEchoChar((char) 0); 

        // Buttons
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

        // Προσθήκη στο Layout
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

    /**
     * Φορτώνει την εικόνα από το src/resources/logo.png
     */
    private boolean loadLogo(JLabel label, String imageName) {
        try {
            // 1. Δοκιμή φόρτωσης ως Resource (αν είναι στο classpath μέσα σε φάκελο resources)
            URL url = getClass().getResource("/resources/" + imageName);
            
            // 2. Αν δεν βρεθεί, δοκιμή απευθείας από το file system στο src/resources/
            if (url == null) {
                File f = new File("src/resources/" + imageName);
                if (f.exists()) {
                    url = f.toURI().toURL();
                }
            }

            if (url != null) {
                ImageIcon icon = new ImageIcon(url);
                // Resize εικόνας για να χωράει όμορφα (150x150 pixels)
                Image img = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(img));
                return true;
            }
        } catch (Exception e) {
            System.err.println("Σφάλμα κατά τη φόρτωση της εικόνας: " + e.getMessage());
        }
        return false;
    }

    private void attemptLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.equals("Εισάγετε το όνομα χρήστη σας")) username = "";
        if (password.equals("Εισάγετε τον κωδικό πρόσβασης")) password = "";
        
        User user = BankDataStore.getInstance().authenticate(username, password);

        if (user != null) {
            if (user.isLocked()) { 
                JOptionPane.showMessageDialog(this, "Ο λογαριασμός είναι κλειδωμένος.\nΕπικοινωνήστε με την τράπεζα.", "Access Denied", JOptionPane.ERROR_MESSAGE);
                return;
            } 
            
            if (user.isNotifyLogin()) {
                try {
                    SecurityNotification notification = new SecurityNotification(new EmailSender());
                    String date = TimeManager.getInstance().getFormattedDate();
                    String msgContent = "Αγαπητέ/ή " + user.getFullName() + ",\n" +
                                      "Εντοπίστηκε νέα σύνδεση στις: " + date + ".";
                    notification.send(msgContent);
                } catch (Exception e) {
                    System.err.println("Notification failed: " + e.getMessage());
                }
            }
            
            MainController.getInstance().onLoginSuccess(user);
            
            usernameField.setText("");
            addPlaceholder(usernameField, "Εισάγετε το όνομα χρήστη σας");
            passwordField.setText("");
            addPlaceholder(passwordField, "Εισάγετε τον κωδικό πρόσβασης");
            
        } else {
            JOptionPane.showMessageDialog(this, "Λάθος όνομα χρήστη ή κωδικός.", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onForgotPassword() {
        String username = JOptionPane.showInputDialog(this, "Βήμα 1/3: Εισάγετε το όνομα χρήστη:");
        if (username == null || username.trim().isEmpty()) return;

        User user = BankDataStore.getInstance().getUser(username);
        
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Ο χρήστης δεν βρέθηκε.", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String afm = JOptionPane.showInputDialog(this, "Βήμα 2/3: Για ταυτοποίηση, εισάγετε το ΑΦΜ σας:");
        if (afm == null) return;

        if (user.getAfm() != null && user.getAfm().equals(afm)) {
            String newPass = JOptionPane.showInputDialog(this, "Βήμα 3/3: Εισάγετε τον ΝΕΟ κωδικό πρόσβασης:");
            if (newPass != null && !newPass.trim().isEmpty()) {
                user.setPassword(newPass);
                BankDataStore.getInstance().saveAllData(); 
                JOptionPane.showMessageDialog(this, "Ο κωδικός άλλαξε επιτυχώς!\nΜπορείτε να συνδεθείτε.", "Επιτυχία", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Το ΑΦΜ δεν ταιριάζει με τον χρήστη.", "Αποτυχία Ταυτοποίησης", JOptionPane.ERROR_MESSAGE);
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