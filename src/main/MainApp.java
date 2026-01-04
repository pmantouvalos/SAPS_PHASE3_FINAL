package main;

import controller.MainController;
import view.MainFrame;
import data.BankDataStore;
import javax.swing.SwingUtilities;

public class MainApp {
    public static void main(String[] args) {
        // 1. Φόρτωση Δεδομένων
        BankDataStore.getInstance().loadAllData();

        // 2. Hook για αποθήκευση κατά την έξοδο
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            BankDataStore.getInstance().saveAllData();
        }));

        // 3. Εκκίνηση GUI & Controller Wiring
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            
            // Σύνδεση: Ο Controller μαθαίνει για το View
            MainController.getInstance().setMainFrame(frame);
            
            frame.setVisible(true);
        });
    }
}