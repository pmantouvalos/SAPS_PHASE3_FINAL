package data;

import model.entities.*;
import model.enums.AccountType;
import model.enums.Role;
import data.impl.*; 
import service.factory.AccountFactory;
import service.factory.UserFactory;
import utils.TimeManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.io.*; 

public class BankDataStore {
    private static BankDataStore instance;

    // Data Cache
    private List<User> users;
    private List<Account> accounts;
    private List<StandingOrder> standingOrders;
    private List<Bill> pendingBills;
    private User loggedUser;

    // DAOs
    private CsvUserDAO userDAO;
    private CsvAccountDAO accountDAO;
    private CsvTransactionDAO transactionDAO;
    private CsvBillDAO billDAO;
    private CsvStandingOrderDAO standingOrderDAO;

    // Σταθερές για χρεώσεις (αν χρειάζονται αλλού)
    public static final double FEE_INTERNAL = 0.0;
    public static final double FEE_STANDING_ORDER = 0.50;

    private BankDataStore() {
        // --- ΑΛΛΑΓΗ: Χρήση του φακέλου "files" ---
        File directory = new File("files");
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                System.out.println("Ο φάκελος 'files' δημιουργήθηκε επιτυχώς.");
            }
        }
        
        users = new ArrayList<>();
        accounts = new ArrayList<>();
        standingOrders = new ArrayList<>();
        pendingBills = new ArrayList<>();

        // Init DAOs
        userDAO = new CsvUserDAO();
        accountDAO = new CsvAccountDAO();
        transactionDAO = new CsvTransactionDAO();
        billDAO = new CsvBillDAO();
        standingOrderDAO = new CsvStandingOrderDAO();
    }

    public static synchronized BankDataStore getInstance() {
        if (instance == null) instance = new BankDataStore();
        return instance;
    }

    // --- LOADING & SAVING ---

    public void loadAllData() {
        System.out.println("Loading Data from 'files' directory...");
        
        // 1. Φόρτωση από αρχεία
        this.users = userDAO.load();
        this.accounts = accountDAO.loadAccounts(); 
        this.transactionDAO.load(this.accounts);   
        this.pendingBills = billDAO.load();
        this.standingOrders = standingOrderDAO.load();

        // Load System Date
        loadSystemDate();
        
        // 2. BOOTSTRAP: Αν δεν υπάρχουν δεδομένα, φτιάξε τα αρχικά
        initializeDefaultDataIfNeeded();
        
        System.out.println("Data Loaded Successfully.");
    }

    private void initializeDefaultDataIfNeeded() {
        // A. Αν δεν υπάρχουν χρήστες -> Φτιάξε Admin
        if (users.isEmpty()) {
            System.out.println("No users found. Creating default Admin.");
            User admin = UserFactory.createUser("admin", "admin123", "Administrator", Role.ADMIN);
            admin.setAfm("000000000");
            admin.setEmail("admin@bankoftuc.gr");
            users.add(admin);
            
            // Προαιρετικά: Φτιάξε έναν απλό χρήστη για δοκιμές
            User user = UserFactory.createUser("user", "123", "Test User", Role.INDIVIDUAL);
            user.setAfm("123456789");
            users.add(user);
        }

        // B. Αν δεν υπάρχει Κεντρικός Λογαριασμός Τράπεζας -> Φτιάξε τον
        if (getCentralBankAccount() == null) {
            System.out.println("Creating Central Bank Account.");
            Account central = AccountFactory.createAccount(
                "Επιχειρηματικός", // String για τον τύπο όπως περιμένει το Factory
                "GR_BANK_OF_TUC", 
                "Bank OfTuc", 
                10000000.00
            );
            accounts.add(central);
        }
    }

    public void saveAllData() {
        System.out.println("Saving Data...");
        
        userDAO.save(this.users);
        accountDAO.saveAccounts(this.accounts);
        transactionDAO.save(this.accounts);
        billDAO.save(this.pendingBills);
        standingOrderDAO.save(this.standingOrders);
        
        saveSystemDate();
        
        System.out.println("Data Saved Successfully.");
    }

    // --- SYSTEM DATE HELPERS (Διορθωμένα Paths) ---
    
    private void loadSystemDate() {
        // ΑΛΛΑΓΗ: files/system.csv
        try (BufferedReader br = new BufferedReader(new FileReader("files/system.csv"))) {
            String line = br.readLine();
            if (line != null && !line.isEmpty()) {
                TimeManager.getInstance().setDate(LocalDate.parse(line.trim()));
            }
        } catch (Exception e) { 
            // Αν δεν υπάρχει αρχείο, ξεκινάμε με τη σημερινή
            TimeManager.getInstance().setDate(LocalDate.now());
        }
    }

    private void saveSystemDate() {
        // ΑΛΛΑΓΗ: files/system.csv
        try (PrintWriter pw = new PrintWriter(new FileWriter("files/system.csv"))) {
            pw.println(TimeManager.getInstance().getDate());
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- GETTERS & FINDERS ---

    public List<User> getUsers() { return users; }
    public List<Account> getAccounts() { return accounts; }
    public List<Bill> getPendingBills() { return pendingBills; }
    public List<StandingOrder> getStandingOrders() { return standingOrders; }
    
    public void setLoggedUser(User user) { this.loggedUser = user; }
    public User getLoggedUser() { return loggedUser; }

    public User authenticate(String u, String p) {
        return users.stream().filter(user -> user.getUsername().equals(u) && user.getPassword().equals(p)).findFirst().orElse(null);
    }

    public Account getAccountByIban(String iban) {
        return accounts.stream().filter(a -> a.getIban().equals(iban)).findFirst().orElse(null);
    }
    
    public User getUser(String username) {
        return users.stream().filter(u -> u.getUsername().equals(username)).findFirst().orElse(null);
    }
    
    public User getUserByAfm(String afm) {
        return users.stream().filter(u -> u.getAfm() != null && u.getAfm().equals(afm)).findFirst().orElse(null);
    }
    
    public User getUserByFullName(String name) {
        return users.stream().filter(u -> u.getFullName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Bill getBillByRf(String rf) {
        return pendingBills.stream().filter(b -> b.getRfCode().equals(rf)).findFirst().orElse(null);
    }
    
    public Account getCentralBankAccount() {
        return accounts.stream().filter(a -> a.getOwnerName().equals("Bank OfTuc")).findFirst().orElse(null);
    }

    public List<Account> getAccountsForUser(User user) {
        List<Account> res = new ArrayList<>();
        if(user == null) return res;
        for (Account acc : accounts) {
            boolean isOwner = acc.getOwnerName().equals(user.getFullName());
            // Έλεγχος joint owners με null safety
            boolean isJoint = acc.getJointOwners() != null && 
                              acc.getJointOwners().stream().anyMatch(jo -> jo.getAfm().equals(user.getAfm()));
            
            if (isOwner || isJoint) res.add(acc);
        }
        return res;
    }
}