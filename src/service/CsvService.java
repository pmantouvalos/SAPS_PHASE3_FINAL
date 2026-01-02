package service;

import model.*;
import model.Account.JointOwner;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale; // ΣΗΜΑΝΤΙΚΗ ΠΡΟΣΘΗΚΗ

public class CsvService {
    private static final String DIR = "data/";
    private static final String SEP = ",";

    //SYSTEM DATE
    public void saveSystemDate(LocalDate date) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(DIR + "system.csv"))) {
            pw.println(date.toString());
        } catch (IOException e) { e.printStackTrace(); }
    }

    public LocalDate loadSystemDate() {
        File f = new File(DIR + "system.csv");
        if (!f.exists()) return LocalDate.now();
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line = br.readLine();
            if (line != null && !line.isEmpty()) return LocalDate.parse(line.trim());
        } catch (Exception e) { System.err.println("Error loading date."); }
        return LocalDate.now();
    }

    //SAVE METHODS (Με Locale.US για να βάζει τελεία στους αριθμούς)

 //SAVE USERS
    public void saveUsers(List<User> users) {
        if(users == null) return;
        try (PrintWriter pw = new PrintWriter(new FileWriter(DIR + "users.csv"))) {
            for (User u : users) {
                if (u == null) continue;
                
                String safeAddress = (u.getAddress() == null) ? "-" : u.getAddress().replace(",", " ");

                // Προσθέσαμε στο τέλος: notifyStandingOrderFailed, notifyBillExpiring
                pw.printf(Locale.US, "%s,%s,%s,%s,%s,%s,%s,%b,%.2f,%.2f,%.2f,%s,%b,%b,%b,%b%n",
                        u.getUsername(), u.getPassword(), u.getFullName(), u.getRole(),
                        u.getAfm(), u.getEmail(), u.getPhone(), u.isLocked(),
                        u.getLimitTransfer(), u.getLimitWithdrawal(), u.getLimitPayment(),
                        safeAddress,
                        u.isNotifyLogin(),
                        u.isNotifyTransaction(),
                        u.isNotifyStandingOrderFailed(),
                        u.isNotifyBillExpiring()
                );
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
/*
    public void saveAccounts(List<Account> accounts) {
        if(accounts == null) return;
        try (PrintWriter pw = new PrintWriter(new FileWriter(DIR + "accounts.csv"))) {
            for (Account a : accounts) {
                if (a == null) continue;
                StringBuilder sb = new StringBuilder();
                if (a.getJointOwners() != null) {
                    for(JointOwner jo : a.getJointOwners()) {
                        sb.append(jo.getName()).append(":").append(jo.getSurname()).append(":")
                          .append(jo.getAfm()).append(":").append(jo.getAccessLevel()).append(";");
                    }
                }
                // Χρήση Locale.US
                pw.printf(Locale.US, "%s,%s,%s,%.2f|%s%n",
                        a.getIban(), a.getAccountType(), a.getOwnerName(), a.getBalance(), sb.toString());
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
*/
    public void saveTransactions(List<Account> accounts) {
        if(accounts == null) return;
        try (PrintWriter pw = new PrintWriter(new FileWriter(DIR + "transactions.csv"))) {
            for (Account a : accounts) {
                if (a == null || a.getTransactions() == null) continue;
                for(Transaction t : a.getTransactions()) {
                    //Χρήση Locale.US - Αυτό λύνει το πρόβλημα με τα κόμματα
                    pw.printf(Locale.US, "%s,%s,%s,%.2f,%.2f,%s,%d,%s,%s,%.2f%n",
                            a.getIban(), t.getType(), t.getDescription(), t.getAmount(), t.getFee(),
                            t.getDate().toString(), t.getTimestamp(), 
                            (t.getSender()==null?"-":t.getSender()), 
                            (t.getReceiver()==null?"-":t.getReceiver()), 
                            t.getBalanceAfter());
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void saveBills(List<Bill> bills) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(DIR + "bills.csv"))) {
            for (Bill b : bills) {
                
                pw.printf(Locale.US, "%s,%s,%s,%.2f,%s,%s,%s%n", 
                        b.getOwnerAfm(), // 0
                        b.getRfCode(),   // 1
                        b.getProvider(), // 2
                        b.getAmount(),   // 3
                        b.getDescription(), // 4
                        b.getIssueDate(),   // 5
                        b.getDueDate()      // 6
                );
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
    
    public void saveStandingOrders(List<StandingOrder> orders) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(DIR + "standing_orders.csv"))) {
            for (StandingOrder s : orders) {
                //Προσθήκη του s.getNextExecutionDate() στο τέλος (9η στήλη)
                pw.printf(Locale.US, "%s,%s,%.2f,%s,%d,%s,%s,%s,%s%n", 
                    s.getSourceIban(), 
                    s.getTarget(), 
                    s.getAmount(), 
                    s.getDescription(), 
                    s.getFrequencyDays(), 
                    s.getStartDate(), 
                    s.getEndDate(), 
                    s.getType(),
                    s.getNextExecutionDate()
                );
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    //LOAD METHODS (Ίδια με πριν, αλλά πιο ανθεκτικά) ---

    public List<User> loadUsers() {
        List<User> list = new ArrayList<>();
        File f = new File(DIR + "users.csv");
        if(!f.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split(SEP);
                if(p.length < 4) continue;
                
                try {
                    Role r; try { r = Role.valueOf(p[3]); } catch (Exception e) { r = Role.INDIVIDUAL; }
                    
                    User u = new User(p[0], p[1], p[2], r);
                    
                    //Βασικά
                    if (p.length > 4) u.setAfm(p[4]);
                    if (p.length > 5) u.setEmail(p[5]);
                    if (p.length > 6) u.setPhone(p[6]);
                    if (p.length > 7) u.setLocked(Boolean.parseBoolean(p[7]));
                    
                    //Όρια
                    if(p.length >= 11) {
                        u.setLimitTransfer(parseDoubleSafe(p[8]));
                        u.setLimitWithdrawal(parseDoubleSafe(p[9]));
                        u.setLimitPayment(parseDoubleSafe(p[10]));
                    }

                    //Διεύθυνση
                    if (p.length > 11) u.setAddress(p[11]);
                    
                    //Preferences
                    //Αν δεν υπάρχουν στο αρχείο, ορίζονται ως true από τον constructor
                    if (p.length > 12) u.setNotifyLogin(Boolean.parseBoolean(p[12]));
                    if (p.length > 13) u.setNotifyTransaction(Boolean.parseBoolean(p[13]));
                    
                    // --- ΝΕΑ ΠΕΔΙΑ ---
                    if (p.length > 14) u.setNotifyStandingOrderFailed(Boolean.parseBoolean(p[14]));
                    if (p.length > 15) u.setNotifyBillExpiring(Boolean.parseBoolean(p[15]));
                    
                    list.add(u);
                } catch (Exception e) { 
                    System.err.println("Skip user: " + line); 
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
    
    /*
    
    public List<Account> loadAccounts() {
        List<Account> list = new ArrayList<>();
        File f = new File(DIR + "accounts.csv");
        if(!f.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                try {
                    String[] mainParts = line.split("\\|"); 
                    String[] p = mainParts[0].split(SEP);
                    if (p.length < 4) continue;
                    Account a = AccountFactory.createAccount(p[1], p[0], p[2], parseDoubleSafe(p[3]));
                    if (a != null) {
                        if(mainParts.length > 1 && !mainParts[1].isEmpty()) {
                            String[] owners = mainParts[1].split(";");
                            for(String ownerStr : owners) {
                                String[] op = ownerStr.split(":");
                                if(op.length >= 4) a.addJointOwner(new JointOwner(op[0], op[1], op[2], op[3]));
                            }
                        }
                        list.add(a);
                    }
                } catch (Exception e) { System.err.println("Skip account: " + line); }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

*/

    public void loadTransactions(List<Account> accounts) {
        File f = new File(DIR + "transactions.csv");
        if(!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                //Εδώ είναι το πρόβλημα με τα παλιά αρχεία. 
                //Αν η γραμμή έχει πολλά κόμματα λόγω ελληνικών, την αγνοούμε για να μην σκάσει.
                String[] p = line.split(SEP);
                if (p.length < 6) continue;
                
                try {
                    Account acc = accounts.stream().filter(a -> a != null && a.getIban().equals(p[0])).findFirst().orElse(null);
                    if(acc != null) {
                        //Αν το αρχείο είναι παλιό (με κόμματα), το parseDoubleSafe θα προσπαθήσει να το σώσει
                        Transaction t = new Transaction.Builder(parseDoubleSafe(p[3]))
                                .setType(p[1])
                                .setDescription(p[2])
                                .setFee(parseDoubleSafe(p[4]))
                                .setDate(LocalDate.parse(p[5]))
                                .setSender(p.length > 7 ? p[7] : "")
                                .setReceiver(p.length > 8 ? p[8] : "")
                                .setBalanceAfter(p.length > 9 ? parseDoubleSafe(p[9]) : 0.0)
                                .build();
                        acc.addTransaction(t);
                    }
                } catch (Exception e) { 
                    //System.err.println("Skip transaction: " + line); // Μπορείτε να το σχολιάσετε αν γεμίζει η κονσόλα
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    public List<Bill> loadBills() {
        List<Bill> list = new ArrayList<>();
        File f = new File(DIR + "bills.csv");
        if(!f.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if(line.trim().isEmpty()) continue;
                String[] p = line.split(SEP);
                if (p.length < 7) continue; // Πλέον θέλουμε 7 πεδία
                
                //Δημιουργία με το ΑΦΜ (p[0])
                list.add(new Bill(
                        p[0], // ownerAfm
                        p[1], // rfCode
                        p[2], // provider
                        parseDoubleSafe(p[3]), // amount
                        p[4], // description
                        LocalDate.parse(p[5]), // issueDate
                        LocalDate.parse(p[6])  // dueDate
                ));
            }
        } catch(Exception e) { e.printStackTrace(); }
        return list;
    }
    
    public List<StandingOrder> loadStandingOrders() {
        List<StandingOrder> list = new ArrayList<>();
        File f = new File(DIR + "standing_orders.csv");
        if(!f.exists()) return list;
        
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if(line.trim().isEmpty()) continue;
                String[] p = line.split(SEP);
                
                //Δημιουργία του αντικειμένου με τα βασικά στοιχεία (όπως το είχες)
                StandingOrder so = new StandingOrder(
                    p[7], // Type
                    p[0], // Source
                    p[1], // Target
                    parseDoubleSafe(p[2]), // Amount
                    p[3], // Description
                    Integer.parseInt(p[4]), // Frequency
                    LocalDate.parse(p[5]), // Start
                    LocalDate.parse(p[6])  // End
                );
                
                //Ελέγχουμε αν υπάρχει αποθηκευμένη η "επόμενη ημερομηνία" (9η στήλη, index 8)
                if (p.length > 8 && p[8] != null && !p[8].isEmpty() && !p[8].equals("null")) {
                    try {
                        so.setNextExecutionDate(LocalDate.parse(p[8]));
                    } catch (Exception e) {
                        //Αν αποτύχει, κρατάμε την default (που είναι η start date)
                        System.err.println("Error parsing next execution date: " + p[8]);
                    }
                }
               
                
                list.add(so);
            }
        } catch(Exception e) { e.printStackTrace(); }
        return list;
    }
    //Helper: Αντικαθιστά το κόμμα με τελεία πριν τη μετατροπή
    private double parseDoubleSafe(String val) {
        try {
            // Αν είναι π.χ. "84,69", το κάνει "84.69"
            return Double.parseDouble(val.replace(",", "."));
        } catch (Exception e) {
            return 0.0;
        }
    }
}