package data.impl;

import java.io.*;
import java.time.LocalDate;
import data.dao.SystemDAO;
import service.builder.SystemBuilder; // Import τον Builder

public class CsvSystemDAO implements SystemDAO {
    private static final String FILE_NAME = "files/system.csv";

    @Override
    public LocalDate load() {
        File f = new File(FILE_NAME);
        
        // Αν δεν υπάρχει αρχείο, ο Builder επιστρέφει από default το LocalDate.now()
        if (!f.exists()) {
            return new SystemBuilder().build();
        }

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line = br.readLine();
            
            // Χρήση του SystemBuilder για το parsing και τα defaults
            return new SystemBuilder()
                    .setDate(line)
                    .build();

        } catch (Exception e) {
            System.err.println("Error reading system file. Defaulting to now.");
            return new SystemBuilder().build();
        }
    }

    @Override
    public void save(LocalDate date) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_NAME))) {
            pw.println(date.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}