package controller;

import view.MainFrame;
import model.entities.User;
import model.enums.Role;
import data.BankDataStore;
import service.notification.SecurityNotification;
import service.notification.sender.EmailSender;

import javax.swing.JOptionPane;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainController {
    private static MainController instance;
    private MainFrame mainFrame;

    private MainController() {}

    public static synchronized MainController getInstance() {
        if (instance == null) {
            instance = new MainController();
        }
        return instance;
    }

    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    /**
     * Καλείται από το LoginPanel όταν η ταυτοποίηση είναι επιτυχής.
     * Αποφασίζει ποιο Dashboard θα δείξει, ελέγχει αν ο χρήστης είναι κλειδωμένος 
     * και στέλνει ειδοποιήσεις.
     */
    public void onLoginSuccess(User user) {
        
        // 1. ΕΛΕΓΧΟΣ ΑΣΦΑΛΕΙΑΣ: Είναι ο χρήστης κλειδωμένος;
        if (user.isLocked()) {
            JOptionPane.showMessageDialog(mainFrame, 
                "Ο λογαριασμός σας έχει κλειδωθεί για λόγους ασφαλείας.\nΠαρακαλώ επικοινωνήστε με την τράπεζα.", 
                "Άρνηση Πρόσβασης", 
                JOptionPane.ERROR_MESSAGE);
            return; // Διακοπή - Ο χρήστης δεν συνδέεται
        }

        // 2. Ενημέρωση του Session στο DataStore
        BankDataStore.getInstance().setLoggedUser(user);

        // 3. ΕΙΔΟΠΟΙΗΣΗ (Αν ο χρήστης έχει ενεργοποιήσει το 'Notify Login')
        if (user.isNotifyLogin()) {
            // Χρήση του Notification Pattern (Bridge)
            try {
                EmailSender sender = new EmailSender();
                SecurityNotification notif = new SecurityNotification(sender);
                
                String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
                notif.send("Νέα είσοδος στο e-banking.\nΧρήστης: " + user.getUsername() + "\nΏρα: " + time);
            } catch (Exception e) {
                System.err.println("Failed to send login notification: " + e.getMessage());
            }
        }

        // 4. Απόφαση πλοήγησης βάσει ρόλου
        if (user.getRole() == Role.ADMIN) {
            mainFrame.showAdminDashboard(user);
        } else {
            // Και οι Ιδιώτες και οι Επιχειρήσεις χρησιμοποιούν το ClientDashboard
            mainFrame.showClientDashboard(user);
        }
    }

    /**
     * Καλείται από τα Dashboards για αποσύνδεση.
     */
    public void onLogout() {
        // 1. Καθαρισμός Session
        BankDataStore.getInstance().setLoggedUser(null);
        
        // 2. Επιστροφή στο Login
        mainFrame.showLoginPanel();
    }
}