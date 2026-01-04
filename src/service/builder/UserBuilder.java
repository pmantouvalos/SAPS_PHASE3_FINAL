package service.builder;

import model.entities.User;
import model.enums.Role;
import model.enums.UserStatus;

public class UserBuilder {
    private String username;
    private String password;
    private String fullName;
    private Role role;
    private String afm = "-";
    private String email = "-";
    private String phone = "-";
    private UserStatus status = UserStatus.ACTIVE;
    
    // Default Limits
    private double limitTransfer = 1000.0;
    private double limitWithdrawal = 600.0;
    private double limitPayment = 1500.0;
    
    // Default Notifications
    private boolean notifyLogin = true;
    private boolean notifyTransaction = true;

    // Υποχρεωτικά πεδία στον Constructor του Builder
    public UserBuilder(String username, String password, String fullName, Role role) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }

    public UserBuilder setAfm(String afm) { this.afm = afm; return this; }
    public UserBuilder setEmail(String email) { this.email = email; return this; }
    public UserBuilder setPhone(String phone) { this.phone = phone; return this; }
    public UserBuilder setStatus(UserStatus status) { this.status = status; return this; }
    
    public UserBuilder setLimits(double transfer, double withdraw, double payment) {
        this.limitTransfer = transfer;
        this.limitWithdrawal = withdraw;
        this.limitPayment = payment;
        return this;
    }
    
    public UserBuilder setNotifications(boolean login, boolean transaction) {
        this.notifyLogin = login;
        this.notifyTransaction = transaction;
        return this;
    }

    public User build() {
        User u = new User(username, password, fullName, role);
        u.setAfm(afm);
        u.setEmail(email);
        u.setPhone(phone);
        // Αν έχεις βάλει status στο User: u.setStatus(status);
        u.setLimitTransfer(limitTransfer);
        u.setLimitWithdrawal(limitWithdrawal);
        u.setLimitPayment(limitPayment);
        u.setNotifyLogin(notifyLogin);
        u.setNotifyTransaction(notifyTransaction);
        return u;
    }
}