package dao;

import model.Account;
import model.Account.JointOwner;
import service.AccountFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AccountDAO {
    private static final String DIR = "data/";
    private static final String FILE_NAME = "accounts.csv";
    private static final String SEP = ",";

    public List<Account> loadAccounts() {
        List<Account> list = new ArrayList<>();
        File f = new File(DIR + FILE_NAME);
        if (!f.exists()) return list;

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
                                if(op.length >= 4) {
                                    a.addJointOwner(new JointOwner(op[0], op[1], op[2], op[3]));
                                }
                            }
                        }
                        list.add(a);
                    }
                } catch (Exception e) { 
                    System.err.println("Skip account: " + line); 
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
        return list;
    }

    public void saveAccounts(List<Account> accounts) {
        if(accounts == null) return;
        try (PrintWriter pw = new PrintWriter(new FileWriter(DIR + FILE_NAME))) {
            for (Account a : accounts) {
                if (a == null) continue;
                StringBuilder sb = new StringBuilder();
                if (a.getJointOwners() != null) {
                    for(JointOwner jo : a.getJointOwners()) {
                        sb.append(jo.getName()).append(":").append(jo.getSurname()).append(":")
                          .append(jo.getAfm()).append(":").append(jo.getAccessLevel()).append(";");
                    }
                }
                pw.printf(Locale.US, "%s,%s,%s,%.2f|%s%n",
                        a.getIban(), a.getAccountType(), a.getOwnerName(), a.getBalance(), sb.toString());
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ΜΗΝ ΞΕΧΑΣΕΤΕ ΑΥΤΗ ΤΗ ΜΕΘΟΔΟ
    private double parseDoubleSafe(String val) {
        try {
            return Double.parseDouble(val.replace(",", "."));
        } catch (Exception e) {
            return 0.0;
        }
    }
}