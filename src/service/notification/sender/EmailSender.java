package service.notification.sender;

import javax.swing.JOptionPane;

public class EmailSender implements MessageSender {
    @Override
    public void sendMessage(String subject, String body) {
        //Προσομοίωση αποστολής Email με παράθυρο διαλόγου
        JOptionPane.showMessageDialog(null, 
            "<html><body><h3> " + subject + "</h3>" +
            body + 
            "</body></html>", 
            "Email Notification System", 
            JOptionPane.INFORMATION_MESSAGE);
    }
}