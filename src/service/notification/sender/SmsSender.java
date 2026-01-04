package service.notification.sender;

import javax.swing.JOptionPane;

public class SmsSender implements MessageSender {

    @Override
    public void sendMessage(String subject, String body) {
        String cleanBody = body.replaceAll("<[^>]*>", ""); 
        
        //Προσομοίωση εμφάνισης σε κινητό
        String mobileScreen = " _______________________\n" +
                              "|  	 ΝΕΟ ΜΗΝΥΜΑ SMS    |\n" +
                              "|-----------------------|\n" +
                              "| " + subject + "\n" +
                              "|-----------------------|\n" +
                              "| " + cleanBody + "\n" +
                              "|_______________________|";

        JOptionPane.showMessageDialog(null, 
            mobileScreen, 
            "Ειδοποίηση SMS", 
            JOptionPane.INFORMATION_MESSAGE);
    }
}