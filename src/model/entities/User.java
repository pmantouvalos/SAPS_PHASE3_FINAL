package model.entities;

import model.enums.Role;
import java.io.Serializable;

public class User implements Serializable {
    // Βασικά Στοιχεία
    private String username;
    private String password;
    private String fullName;
    private Role role;
    
    // Στοιχεία Επικοινωνίας
    private String afm;
    private String email;
    private String phone;
    private String address;

    // --- ΠΕΔΙΑ ΓΙΑ CHAIN OF RESPONSIBILITY (VALIDATION) ---
    private boolean isLocked;          // Για UserStatusHandler
    private double limitTransfer;      // Για DailyLimitHandler
    private double limitWithdrawal;    // Για DailyLimitHandler (Cashier)
    private double limitPayment;       // Για DailyLimitHandler (Payments)

    // --- ΠΕΔΙΑ ΓΙΑ ΡΥΘΜΙΣΕΙΣ ΕΙΔΟΠΟΙΗΣΕΩΝ ---
    private boolean notifyLogin;
    private boolean notifyTransaction;
    private boolean notifyStandingOrderFailed;
    private boolean notifyBillExpiring;

    public User(String username, String password, String fullName, Role role) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        
        // Default values (Αυτά τα ορίζει και το Factory, αλλά καλό είναι να υπάρχουν κι εδώ)
        this.isLocked = false;
        
        // Default Limits (Μπορούν να αλλάξουν από το SettingsPanel)
        this.limitTransfer = 2000.0;
        this.limitWithdrawal = 600.0;
        this.limitPayment = 1000.0;
        
        // Default Notifications
        this.notifyTransaction = true;
        this.notifyLogin = false;
    }

    // --- Getters & Setters ---

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getAfm() { return afm; }
    public void setAfm(String afm) { this.afm = afm; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    // --- Security / Limits Getters & Setters ---

    public boolean isLocked() { return isLocked; }
    public void setLocked(boolean locked) { isLocked = locked; }

    public double getLimitTransfer() { return limitTransfer; }
    public void setLimitTransfer(double limitTransfer) { this.limitTransfer = limitTransfer; }

    public double getLimitWithdrawal() { return limitWithdrawal; }
    public void setLimitWithdrawal(double limitWithdrawal) { this.limitWithdrawal = limitWithdrawal; }

    public double getLimitPayment() { return limitPayment; }
    public void setLimitPayment(double limitPayment) { this.limitPayment = limitPayment; }

    // --- Notification Preferences ---

    public boolean isNotifyLogin() { return notifyLogin; }
    public void setNotifyLogin(boolean notifyLogin) { this.notifyLogin = notifyLogin; }

    public boolean isNotifyTransaction() { return notifyTransaction; }
    public void setNotifyTransaction(boolean notifyTransaction) { this.notifyTransaction = notifyTransaction; }

    public boolean isNotifyStandingOrderFailed() { return notifyStandingOrderFailed; }
    public void setNotifyStandingOrderFailed(boolean notifyStandingOrderFailed) { this.notifyStandingOrderFailed = notifyStandingOrderFailed; }

    public boolean isNotifyBillExpiring() { return notifyBillExpiring; }
    public void setNotifyBillExpiring(boolean notifyBillExpiring) { this.notifyBillExpiring = notifyBillExpiring; }
    
    @Override
    public String toString() {
        return fullName + " (" + role + ")";
    }
}