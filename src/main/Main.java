package main;

import gui.MainFrame;
import service.BankDataStore;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        //1.Φόρτωση Δεδομένων από CSV
        BankDataStore.getInstance().loadAllData();

        //2.Ρύθμιση Αποθήκευσης στον Τερματισμό
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            BankDataStore.getInstance().saveAllData();
        }));

        //3.Εκκίνηση GUI
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}