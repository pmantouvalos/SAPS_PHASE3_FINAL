package gui.panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ProfilePanel extends JPanel implements Refreshable {
    
    private model.User user;
    
    //Fields
    private JTextField nameF, surnameF, afmF, addrF, emailF, phoneF;
    private JButton actionBtn;
    private boolean isEditing = false;

    public ProfilePanel(model.User user) {
        this.user = user;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(30, 50, 30, 50));
        
        //Header
        JLabel title = new JLabel("Το Προφίλ μου");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        add(title, BorderLayout.NORTH);
        
        //Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 10, 10, 10);
        g.anchor = GridBagConstraints.WEST;
        
        //Init Fields
        String[] parts = user.getFullName().split(" ");
        String name = parts.length > 0 ? parts[0] : "";
        String surname = parts.length > 1 ? parts[1] : "";
        
        nameF = createField(name);
        surnameF = createField(surname);
        afmF = createField(user.getAfm());
        addrF = createField(user.getAddress());
        emailF = createField(user.getEmail());
        phoneF = createField(user.getPhone());
        
        //Layout
        addLabelField(form, g, 0, "Όνομα :", nameF);
        addLabelField(form, g, 1, "Επώνυμο :", surnameF);
        addLabelField(form, g, 2, "ΑΦΜ :", afmF);
        addLabelField(form, g, 3, "Διεύθυνση :", addrF);
        addLabelField(form, g, 4, "Email :", emailF);
        addLabelField(form, g, 5, "Τηλέφωνο :", phoneF);
        
        add(form, BorderLayout.CENTER);
        
        //Button
        actionBtn = new JButton("Επεξεργασία Στοιχείων");
        actionBtn.setBackground(new Color(40, 45, 55));
        actionBtn.setForeground(Color.WHITE);
        actionBtn.setPreferredSize(new Dimension(200, 40));
        actionBtn.addActionListener(e -> toggleEditMode());
        
        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bot.setBackground(Color.WHITE);
        bot.add(actionBtn);
        add(bot, BorderLayout.SOUTH);
        
        setFieldsEditable(false);
    }
    
    private void toggleEditMode() {
        if (!isEditing) {
            //Enter Edit Mode
            isEditing = true;
            setFieldsEditable(true);
            actionBtn.setText("Αποθήκευση Αλλαγών");
            //Κλειδώνουμε ορισμένα πεδία που δεν αλλάζουν εύκολα
            nameF.setEditable(false); 
            surnameF.setEditable(false);
            afmF.setEditable(false);
        } else {
            //Save Changes
            String otp = JOptionPane.showInputDialog(this, "Επιβεβαίωση Αλλαγών\nΕισάγετε OTP (1234):");
            if("1234".equals(otp)) {
                // Update User
                user.setAddress(addrF.getText());
                user.setEmail(emailF.getText());
                user.setPhone(phoneF.getText());
                
                JOptionPane.showMessageDialog(this, "Τα στοιχεία ενημερώθηκαν επιτυχώς.");
                isEditing = false;
                setFieldsEditable(false);
                actionBtn.setText("Επεξεργασία Στοιχείων");
            } else {
                JOptionPane.showMessageDialog(this, "Λάθος OTP. Οι αλλαγές ακυρώθηκαν.");
            }
        }
    }
    
    private void setFieldsEditable(boolean val) {
        addrF.setEditable(val);
        emailF.setEditable(val);
        phoneF.setEditable(val);
        //Name, Surname, AFM are usually read-only
        nameF.setEditable(false);
        surnameF.setEditable(false);
        afmF.setEditable(false);
    }

    private JTextField createField(String txt) {
        JTextField f = new JTextField(20);
        f.setText(txt);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        return f;
    }
    
    private void addLabelField(JPanel p, GridBagConstraints g, int row, String lbl, JTextField f) {
        g.gridy = row;
        g.gridx = 0;
        JLabel l = new JLabel(lbl);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        p.add(l, g);
        g.gridx = 1;
        p.add(f, g);
    }

    @Override
    public void refresh() {
        //Reload data just in case
        addrF.setText(user.getAddress());
        emailF.setText(user.getEmail());
        phoneF.setText(user.getPhone());
    }
}