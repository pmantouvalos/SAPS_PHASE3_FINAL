package service;
import model.*;
import java.util.ArrayList;
import java.util.List;
import dao.AccountDAO;

public class BankDataStore {
    private static BankDataStore instance;
    private List<User> users;
    private List<Account> accounts;
    private List<StandingOrder> standingOrders;
    private User currentUser;
    
    private CsvService csvService = new CsvService();
    private AccountDAO accountDAO;
    
    private List<Bill> pendingBills;
    
    private model.User loggedUser;
    
    public static final double FEE_INTERNAL = 0.25; //Δωρεάν οι ενδοτραπεζικές
    public static final double FEE_SEPA = 1.00;     //1€ για SEPA
    public static final double FEE_SWIFT = 5.00;    //5€ για SWIFT
    public static final double FEE_STANDING_ORDER = 0.50; //0.50€ για Πάγιες
    

    private BankDataStore() {
        users = new ArrayList<>();
        accounts = new ArrayList<>();
        standingOrders = new ArrayList<>();
        pendingBills = new ArrayList<>();
        accountDAO = new AccountDAO();
    
    }

    public List<Bill> getPendingBills() { return pendingBills; }
    
    public static synchronized BankDataStore getInstance() {
        if (instance == null) instance = new BankDataStore();
        return instance;
    }

    public User authenticate(String u, String p) {
        for(User user : users) {
            if(user.getUsername().equals(u) && user.getPassword().equals(p)) {
                this.currentUser = user;
                return user;
            }
        }
        return null;
    }

    public void logout() { currentUser = null; }
    public User getCurrentUser() { return currentUser; }
    public List<Account> getAccounts() { return accounts; }
    public List<User> getUsers() { return users; }
    public List<StandingOrder> getStandingOrders() { return standingOrders; }

    public List<Account> getMyAccounts() {
        List<Account> myAccs = new ArrayList<>();
        if(currentUser == null || currentUser.getRole() == Role.ADMIN) return myAccs;
        for(Account a : accounts) {
            if(a.getOwnerName().equals(currentUser.getFullName())) myAccs.add(a);
        }
        return myAccs;
    }

    public Account getAccountByIban(String iban) {
        return accounts.stream().filter(a -> a.getIban().equals(iban)).findFirst().orElse(null);
    }
    
 //φέρνει λογαριασμούς Ιδιοκτήτη ΚΑΙ Συνδικαιούχου
    public java.util.List<model.Account> getAccountsForUser(model.User user) {
        java.util.List<model.Account> result = new java.util.ArrayList<>();
        for (model.Account acc : accounts) {
            boolean isOwner = acc.getOwnerName().equals(user.getFullName());
            boolean isJoint = acc.getJointOwners().stream()
                    .anyMatch(jo -> jo.getAfm().equals(user.getAfm()));
            
            if (isOwner || isJoint) {
                result.add(acc);
            }
        }
        return result;
    }
    
    public model.User getUser(String username) {
    	if (users == null) return null;
        for (model.User u : users) {
            if (u.getUsername().equals(username)) {
                return u;
            }
        }
        return null;
    }
    
    
 //Φόρτωση Όλων (Καλείται στην εκκίνηση)
    public void loadAllData() {
        this.users = csvService.loadUsers();
        //this.accounts = csvService.loadAccounts();
        
        this.accounts = accountDAO.loadAccounts();
        csvService.loadTransactions(this.accounts); // Συνδέει τις κινήσεις με τους λογαριασμούς
        this.pendingBills = csvService.loadBills();
        this.standingOrders = csvService.loadStandingOrders();
        
     //2.ΦΟΡΤΩΣΗ ΗΜΕΡΟΜΗΝΙΑΣ
        java.time.LocalDate savedDate = csvService.loadSystemDate();
        utils.TimeManager.getInstance().setDate(savedDate);
        
        System.out.println("Data loaded successfully from CSV.");
    }

    //Αποθήκευση Όλων (Καλείται στον τερματισμό)
    public void saveAllData() {
        csvService.saveUsers(this.users);
        //csvService.saveAccounts(this.accounts);
        accountDAO.saveAccounts(this.accounts);
        csvService.saveTransactions(this.accounts);
        csvService.saveBills(this.pendingBills);
        csvService.saveStandingOrders(this.standingOrders);
        
     //3.ΑΠΟΘΗΚΕΥΣΗ ΗΜΕΡΟΜΗΝΙΑΣ
        csvService.saveSystemDate(utils.TimeManager.getInstance().getDate());
        
        System.out.println("Data saved successfully to CSV.");
    }
    
    public model.Account getCentralBankAccount() {
        for (model.Account acc : accounts) {
            //Ψάχνουμε τον λογαριασμό που ανήκει στο "Bank OfTuc" (όπως είναι στο CSV)
            if (acc.getOwnerName().equals("Bank OfTuc")) {
                return acc;
            }
        }
        //Αν δεν βρεθεί, επιστρέφουμε null (θα πρέπει να το χειριστούμε για να μην κρασάρει)
        return null;
    }
    
 //Βρίσκει τον χρήστη με βάση το ονοματεπώνυμο (όπως είναι αποθηκευμένο στον λογαριασμό)
    public model.User getUserByFullName(String fullName) {
        for (model.User u : users) {
            if (u.getFullName().equalsIgnoreCase(fullName)) {
                return u;
            }
        }
        return null;
    }
    
    public model.User getUserByAfm(String afm) {
        for (model.User u : users) {
            if (u.getAfm().equals(afm)) {
                return u;
            }
        }
        return null;
    }
    
    public void setLoggedUser(model.User user) {
        this.loggedUser = user;
    }

    public model.User getLoggedUser() {
        return loggedUser;
    }
    
    public Bill getBillByRf(String rfCode) {
        for (Bill b : pendingBills) { 
            if (b.getRfCode().equals(rfCode)) {
                return b;
            }
        }
        return null;
    }
    
}