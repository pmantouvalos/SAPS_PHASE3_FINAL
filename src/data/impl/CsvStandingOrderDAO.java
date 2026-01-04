package data.impl;

import model.entities.StandingOrder;
import data.dao.StandingOrderDAO;
import service.factory.StandingOrderFactory; // Import Factory

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CsvStandingOrderDAO implements StandingOrderDAO {
    private static final String FILE_NAME = "files/standing_orders.csv";
    private static final String SEP = ",";

    @Override
    public List<StandingOrder> load() {
        List<StandingOrder> list = new ArrayList<>();
        File f = new File(FILE_NAME);
        if (!f.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split(SEP);
                
                // Χρειαζόμαστε τουλάχιστον 8 πεδία (το p[7] είναι ο τύπος)
                if (p.length < 8) continue;

                try {
                    // --- FACTORY IMPLEMENTATION ---
                    

                    StandingOrder so = StandingOrderFactory.createOrder(
                            p[7],                   // Type ("Transfer" ή "Payment")
                            p[0],                   // Source IBAN
                            p[1],                   // Target
                            parseDoubleSafe(p[2]),  // Amount
                            p[3]                    // Description
                    );

                    // 2. Επαναφορά των ιστορικών δεδομένων από το CSV (Rehydration)
                    // Το Factory βάζει default frequency=30 και startDate=Now.
                    // Εμείς τα αντικαθιστούμε με τα πραγματικά από το αρχείο.
                    so.setFrequencyDays(Integer.parseInt(p[4]));
                    so.setStartDate(LocalDate.parse(p[5]));
                    so.setEndDate(LocalDate.parse(p[6]));
                    
                    // Handle Next Execution Date (αν υπάρχει και δεν είναι null)
                    if (p.length > 8 && !p[8].equals("null") && !p[8].isEmpty()) {
                        so.setNextExecutionDate(LocalDate.parse(p[8]));
                    }
                    
                    list.add(so);
                } catch (Exception e) {
                    System.err.println("Skipping standing order: " + line);
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public void save(List<StandingOrder> orders) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (StandingOrder s : orders) {
                pw.printf(Locale.US, "%s,%s,%.2f,%s,%d,%s,%s,%s,%s%n",
                        s.getSourceIban(), s.getTarget(), s.getAmount(), s.getDescription(),
                        s.getFrequencyDays(), s.getStartDate(), s.getEndDate(),
                        s.getType(), s.getNextExecutionDate());
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private double parseDoubleSafe(String val) {
        try { return Double.parseDouble(val.replace(",", ".")); } catch (Exception e) { return 0.0; }
    }
}