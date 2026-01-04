package data.impl;

import model.entities.User;
import data.dao.UserDAO;
import service.factory.UserFactory; // Import το Factory

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CsvUserDAO implements UserDAO {
    private static final String FILE_NAME = "files/users.csv";
    private static final String SEP = ",";

    @Override
    public List<User> load() {
        List<User> users = new ArrayList<>();
        File f = new File(FILE_NAME);
        if (!f.exists()) return users;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split(SEP);
                
                // Χρειαζόμαστε τουλάχιστον τα βασικά πεδία (0-3)
                if (p.length < 4) continue;

                try {
                    // 1. FACTORY: Δημιουργία με Defaults βάσει Ρόλου
                    // p[0]=user, p[1]=pass, p[2]=name, p[3]=role
                    User u = UserFactory.createUser(p[0], p[1], p[2], p[3]);

                    // 2. CONTACT INFO (Αν υπάρχουν στο CSV)
                    if (p.length > 4) u.setAfm(clean(p[4]));
                    if (p.length > 5) u.setEmail(clean(p[5]));
                    if (p.length > 6) u.setPhone(clean(p[6]));
                    if (p.length > 7) u.setAddress(clean(p[7]));

                    // 3. SECURITY & LIMITS
                    if (p.length > 8) u.setLocked(Boolean.parseBoolean(p[8]));
                    
                    if (p.length > 11) { // Αν υπάρχουν τα όρια
                        u.setLimitTransfer(parseDoubleSafe(p[9]));
                        u.setLimitWithdrawal(parseDoubleSafe(p[10]));
                        u.setLimitPayment(parseDoubleSafe(p[11]));
                    }

                    // 4. NOTIFICATIONS
                    if (p.length > 15) { // Αν υπάρχουν οι ρυθμίσεις ειδοποιήσεων
                        u.setNotifyLogin(Boolean.parseBoolean(p[12]));
                        u.setNotifyTransaction(Boolean.parseBoolean(p[13]));
                        u.setNotifyStandingOrderFailed(Boolean.parseBoolean(p[14]));
                        u.setNotifyBillExpiring(Boolean.parseBoolean(p[15]));
                    }

                    users.add(u);
                } catch (Exception e) {
                    System.err.println("Skipping invalid user line: " + line);
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
        return users;
    }

    @Override
    public void save(List<User> users) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (User u : users) {
                // Προστασία για null strings
                String afm = (u.getAfm() == null) ? "null" : u.getAfm();
                String email = (u.getEmail() == null) ? "null" : u.getEmail();
                String phone = (u.getPhone() == null) ? "null" : u.getPhone();
                String address = (u.getAddress() == null) ? "null" : u.getAddress().replace(",", " "); // Αφαίρεση κομμάτων

                // Εγγραφή με αυστηρή σειρά 0-15
                pw.printf(Locale.US, 
                        "%s,%s,%s,%s," +       // 0-3: Basic
                        "%s,%s,%s,%s," +       // 4-7: Contact
                        "%b,%.2f,%.2f,%.2f," + // 8-11: Security (Locked + 3 Limits)
                        "%b,%b,%b,%b%n",       // 12-15: Notifications
                        
                        u.getUsername(), u.getPassword(), u.getFullName(), u.getRole(),
                        afm, email, phone, address,
                        u.isLocked(), u.getLimitTransfer(), u.getLimitWithdrawal(), u.getLimitPayment(),
                        u.isNotifyLogin(), u.isNotifyTransaction(), u.isNotifyStandingOrderFailed(), u.isNotifyBillExpiring()
                );
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    // Βοηθητική μέθοδος για καθαρισμό του "null" string κατά το load
    private String clean(String val) {
        if (val == null || val.equalsIgnoreCase("null")) return "";
        return val;
    }

    private double parseDoubleSafe(String val) {
        try { 
            if (val == null || val.equals("null")) return 0.0;
            return Double.parseDouble(val.replace(",", ".")); 
        } catch (Exception e) { return 0.0; }
    }
}