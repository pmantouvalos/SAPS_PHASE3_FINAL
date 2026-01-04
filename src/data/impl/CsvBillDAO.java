package data.impl;

import model.entities.Bill;
import data.dao.BillDAO;
import service.factory.BillFactory; // Import Factory αντί για Builder

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CsvBillDAO implements BillDAO {
    private static final String FILE_NAME = "files/bills.csv";
    private static final String SEP = ",";

    @Override
    public List<Bill> load() {
        List<Bill> list = new ArrayList<>();
        File f = new File(FILE_NAME);
        if (!f.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split(SEP);
                
                // Χρειαζόμαστε και τα 7 πεδία (συμπεριλαμβανομένων των ημερομηνιών)
                if (p.length < 7) continue;

                try {
                    // --- FACTORY IMPLEMENTATION ---
                    // Καλούμε την 'createBillFull' επειδή φορτώνουμε ήδη υπάρχοντα δεδομένα
                    // που έχουν συγκεκριμένη ημερομηνία έκδοσης και λήξης.
                    Bill b = BillFactory.createBillFull(
                            p[0],                       // Owner AFM
                            p[1],                       // RF Code
                            p[2],                       // Provider
                            parseDoubleSafe(p[3]),      // Amount
                            p[4],                       // Description
                            LocalDate.parse(p[5]),      // Issue Date
                            LocalDate.parse(p[6])       // Due Date
                    );

                    list.add(b);
                } catch (Exception e) {
                    System.err.println("Skipping invalid bill: " + line);
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public void save(List<Bill> bills) {
        // Η Save παραμένει ίδια, καθώς διαβάζει τα δεδομένα από το αντικείμενο
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Bill b : bills) {
                pw.printf(Locale.US, "%s,%s,%s,%.2f,%s,%s,%s%n",
                        b.getOwnerAfm(), b.getRfCode(), b.getProvider(),
                        b.getAmount(), b.getDescription(),
                        b.getIssueDate(), b.getDueDate());
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
    
    private double parseDoubleSafe(String val) {
        try { return Double.parseDouble(val.replace(",", ".")); } catch (Exception e) { return 0.0; }
    }
}