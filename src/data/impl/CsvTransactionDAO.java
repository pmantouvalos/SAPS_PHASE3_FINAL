package data.impl;

import model.entities.Account;
import model.entities.Transaction;
import model.enums.TransactionType;
import data.dao.TransactionDAO;
import service.factory.TransactionFactory; // Import Factory

import java.io.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public class CsvTransactionDAO implements TransactionDAO {
    private static final String FILE_NAME = "files/transactions.csv";
    private static final String SEP = ",";

    @Override
    public void load(List<Account> accounts) {
        File f = new File(FILE_NAME);
        if (!f.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split(SEP);
                
                // Έλεγχος αν έχουμε αρκετά πεδία (τουλάχιστον μέχρι την ημερομηνία)
                if (p.length < 6) continue;

                try {
                    String iban = p[0];
                    // Βρίσκουμε τον λογαριασμό στη λίστα
                    Account acc = accounts.stream()
                            .filter(a -> a.getIban().equals(iban))
                            .findFirst()
                            .orElse(null);

                    if (acc != null) {
                        // Safe Enum Parsing (Κρατάμε τη λογική προστασίας από το παλιό DAO)
                        TransactionType type = parseTransactionType(p[1]);

                        // Προετοιμασία τιμών (Handling optional fields)
                        String sender = (p.length > 7) ? p[7] : "-";
                        String receiver = (p.length > 8) ? p[8] : "-";
                        double balanceAfter = (p.length > 9) ? parseDoubleSafe(p[9]) : 0.0;

                        // --- FACTORY IMPLEMENTATION ---
                        // Χρησιμοποιούμε τη μέθοδο 'fromCsv' για να διατηρήσουμε την παλιά ημερομηνία
                        Transaction t = TransactionFactory.createTransactionFromCsv(
                                parseDoubleSafe(p[3]), // amount
                                type,                  // type (Enum)
                                p[2],                  // description
                                parseDoubleSafe(p[4]), // fee
                                LocalDate.parse(p[5]), // date (Ιστορική)
                                sender,                // sender
                                receiver,              // receiver
                                balanceAfter           // balanceAfter
                        );
                        
                        // Αν το CSV έχει timestamp (p[6]), το προσθέτουμε χειροκίνητα αν χρειάζεται,
                        // αλλιώς ο Builder έχει βάλει default.
                        if (p.length > 6) {
                            try { t.setTimestamp(Long.parseLong(p[6])); } catch (Exception ignored){}
                        }

                        acc.addTransaction(t);
                    }
                } catch (Exception e) {
                   // System.err.println("Skip tx: " + line);
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public void save(List<Account> accounts) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Account a : accounts) {
                for (Transaction t : a.getTransactions()) {
                    pw.printf(Locale.US, "%s,%s,%s,%.2f,%.2f,%s,%d,%s,%s,%.2f%n",
                            a.getIban(), 
                            t.getType().name(), // Save Enum Name
                            t.getDescription(),
                            t.getAmount(), 
                            t.getFee(), 
                            t.getDate(),
                            t.getTimestamp(),
                            t.getSender(), 
                            t.getReceiver(), 
                            t.getBalanceAfter());
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private TransactionType parseTransactionType(String text) {
        if (text == null) return TransactionType.TRANSFER;
        try { return TransactionType.valueOf(text); } 
        catch (IllegalArgumentException e) { 
            // Fallback: Check labels if CSV has Greek
            for(TransactionType t : TransactionType.values()){
                if(t.toString().equalsIgnoreCase(text)) return t;
            }
            return TransactionType.TRANSFER; 
        }
    }

    private double parseDoubleSafe(String val) {
        try { return Double.parseDouble(val.replace(",", ".")); } catch (Exception e) { return 0.0; }
    }
}