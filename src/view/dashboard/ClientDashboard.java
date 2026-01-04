package view.dashboard;

import controller.MainController;
import model.entities.User;
import model.enums.Role;
import utils.TimeManager;
import view.panels.*; // Import όλα τα panels (Home, Accounts, Transfers, etc.)

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ClientDashboard extends JPanel {
    private JPanel contentArea;
    private CardLayout cardLayout;
    private JLabel dateLbl;
    private User currentUser;

    private final Color NAV_BG = new Color(30, 30, 30);
    private final Color NAV_TEXT = Color.WHITE;
    private final Color NAV_HOVER = new Color(60, 60, 60);

    public ClientDashboard(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout());

        // --- TOP BAR ---
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(NAV_BG);
        topBar.setPreferredSize(new Dimension(1200, 50));

        // A. Left Menu (Navigation)
        JPanel leftMenu = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftMenu.setOpaque(false);

        leftMenu.add(createNavButton("Αρχική", "HOME"));
        leftMenu.add(createNavButton("Λογαριασμοί", "ACCOUNTS"));
        leftMenu.add(createNavButton("Μεταφορές", "TRANSFERS"));
        leftMenu.add(createNavButton("Πληρωμές", "PAYMENTS"));
        leftMenu.add(createNavButton("Πάγιες Εντολές", "STANDING"));
        
        // ΕΙΔΙΚΗ ΠΡΟΣΘΗΚΗ ΓΙΑ ΕΠΙΧΕΙΡΗΣΕΙΣ
        if (user.getRole() == Role.BUSINESS) {
            JButton issueBtn = createNavButton("Έκδοση Λογαριασμών", "ISSUE_BILLS");
            issueBtn.setForeground(new Color(100, 255, 100)); // Λίγο διαφορετικό χρώμα για έμφαση
            leftMenu.add(issueBtn);
        }
        
        leftMenu.add(createNavButton("Κινήσεις", "HISTORY"));

        // B. Right Menu (Info & Actions)
        JPanel rightMenu = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        rightMenu.setOpaque(false);

        // Κουμπί Προσομοίωσης Χρόνου
        JButton nextDayBtn = new JButton(">>");
        nextDayBtn.setBackground(Color.CYAN);
        nextDayBtn.setToolTipText("Προχώρα στην επόμενη μέρα");
        nextDayBtn.setPreferredSize(new Dimension(50, 25));
        nextDayBtn.addActionListener(e -> {
            TimeManager.getInstance().advanceTime();
            dateLbl.setText(TimeManager.getInstance().getFormattedDate());
            refreshAll(); // Ενημέρωση όλων των panels
        });
        
        dateLbl = new JLabel(TimeManager.getInstance().getFormattedDate());
        dateLbl.setForeground(Color.WHITE);
        dateLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JLabel userLbl = new JLabel(user.getFullName());
        userLbl.setForeground(Color.WHITE);
        userLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JButton profileBtn = createDropdownButton("Προφίλ ▼");
        
        JButton logoutBtn = new JButton("Αποσύνδεση");
        logoutBtn.setBackground(new Color(180, 50, 50));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setPreferredSize(new Dimension(100, 25));
        // Κλήση Controller για Logout
        logoutBtn.addActionListener(e -> MainController.getInstance().onLogout());

        rightMenu.add(nextDayBtn);
        rightMenu.add(dateLbl);
        rightMenu.add(Box.createHorizontalStrut(10));
        rightMenu.add(userLbl);
        rightMenu.add(profileBtn);
        rightMenu.add(logoutBtn);

        topBar.add(leftMenu, BorderLayout.WEST);
        topBar.add(rightMenu, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // --- CONTENT AREA (CARD LAYOUT) ---
        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);
        
        // Προσθήκη όλων των Panels
        contentArea.add(new HomePanel(user), "HOME");
        contentArea.add(new AccountsPanel(user), "ACCOUNTS");
        contentArea.add(new TransfersPanel(user), "TRANSFERS");
        contentArea.add(new PaymentsPanel(user), "PAYMENTS");
        contentArea.add(new StandingOrdersPanel(user), "STANDING");
        contentArea.add(new HistoryPanel(user), "HISTORY");
        
        // Panels Διαχείρισης
        contentArea.add(new SettingsPanel(user), "SETTINGS");
        contentArea.add(new ProfilePanel(user), "PROFILE");
        
        // Panel Επιχείρησης (αν χρειάζεται)
        if (user.getRole() == Role.BUSINESS) {
            contentArea.add(new IssueBillsPanel(user), "ISSUE_BILLS");
        }

        add(contentArea, BorderLayout.CENTER);
    }

    private JButton createNavButton(String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(130, 50));
        btn.setBackground(NAV_BG);
        btn.setForeground(NAV_TEXT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        // Hover Effect
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(NAV_HOVER); }
            public void mouseExited(MouseEvent e) { btn.setBackground(NAV_BG); }
        });
        
        btn.addActionListener(e -> switchPage(cardName));
        return btn;
    }

    private JButton createDropdownButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(NAV_BG);
        btn.setForeground(NAV_TEXT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        
        JPopupMenu popup = new JPopupMenu();
        
        JMenuItem profileItem = new JMenuItem("Τα στοιχεία μου");
        profileItem.addActionListener(e -> switchPage("PROFILE"));
        
        JMenuItem settingsItem = new JMenuItem("Ρυθμίσεις & Ασφάλεια");
        settingsItem.addActionListener(e -> switchPage("SETTINGS"));
        
        popup.add(profileItem);
        popup.add(settingsItem);
        
        btn.addActionListener(e -> popup.show(btn, 0, btn.getHeight()));
        return btn;
    }

    private void switchPage(String pageName) {
        cardLayout.show(contentArea, pageName);
        refreshAll();
    }

    private void refreshAll() {
        for(Component c : contentArea.getComponents()) {
            if(c instanceof Refreshable) ((Refreshable)c).refresh();
        }
    }
}