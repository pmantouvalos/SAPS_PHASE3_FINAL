package data.impl;

import model.entities.Account;
import model.entities.Account.JointOwner;
import model.enums.AccountType;
import data.dao.AccountDAO;


import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CsvAccountDAO implements AccountDAO {
    private static final String DIR = "files/";
    private static final String FILE_NAME = "accounts.csv";
    private static final String SEP = ",";

    @Override
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
                    

                    // --- FACTORY IMPLEMENTATION ---
                    Account a = service.factory.AccountFactory.createAccount(p[1], p[0], p[2], parseDoubleSafe(p[3]));
                    
                    if (a != null) {
                        // Προσθήκη Συνδικαιούχων
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

    @Override
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

    private double parseDoubleSafe(String val) {
        try { return Double.parseDouble(val.replace(",", ".")); } catch (Exception e) { return 0.0; }
    }
}