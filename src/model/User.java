package model;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String username;
    private String password;
    private String fullName;
    private Role role;
    private String email;
    private String phone;
    
    private String afm;
    private String address;
    
    private boolean isLocked = false;
    
    //Όρια
    private double limitTransfer = 500.0;
    private double limitWithdrawal = 500.0;
    private double limitPayment = 500.0;
    
    
    private boolean notifyLogin = true;       //Ειδοποίηση όταν συνδέεται
    private boolean notifyTransaction = true; //Ειδοποίηση σε συναλλαγές
    private boolean notifyStandingOrderFailed = true; // Ειδοποίηση για αποτυχία πάγιας
    private boolean notifyBillExpiring = true;        // Ειδοποίηση για λογαριασμούς (Bills)
    
    

    public User(String username, String password, String fullName, Role role) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        
    }

    public boolean isNotifyStandingOrderFailed() { return notifyStandingOrderFailed; }
    public void setNotifyStandingOrderFailed(boolean notifyStandingOrderFailed) { 
        this.notifyStandingOrderFailed = notifyStandingOrderFailed; 
    }

    public boolean isNotifyBillExpiring() { return notifyBillExpiring; }
    public void setNotifyBillExpiring(boolean notifyBillExpiring) { 
        this.notifyBillExpiring = notifyBillExpiring; 
    }
    
    public boolean isNotifyLogin() { return notifyLogin; }
    public void setNotifyLogin(boolean notifyLogin) { this.notifyLogin = notifyLogin; }

    public boolean isNotifyTransaction() { return notifyTransaction; }
    public void setNotifyTransaction(boolean notifyTransaction) { this.notifyTransaction = notifyTransaction; }
    
    //Getters kai Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getFullName() { return fullName; }
    public Role getRole() { return role; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getAfm() { return afm; }
    public void setAfm(String afm) { this.afm = afm; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public double getLimitTransfer() { return limitTransfer; }
    public void setLimitTransfer(double limit) { this.limitTransfer = limit; }
    
    public double getLimitWithdrawal() { return limitWithdrawal; }
    public void setLimitWithdrawal(double limit) { this.limitWithdrawal = limit; }
    
    public double getLimitPayment() { return limitPayment; }
    public void setLimitPayment(double limit) { this.limitPayment = limit; }

    public boolean isLocked() { return isLocked; }
    public void setLocked(boolean locked) { isLocked = locked; }

}