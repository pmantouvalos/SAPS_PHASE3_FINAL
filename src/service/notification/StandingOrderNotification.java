package service.notification;

import service.notification.sender.MessageSender;

public class StandingOrderNotification extends Notification {

    public StandingOrderNotification(MessageSender sender) {
        super(sender);
    }

    @Override
    public void send(String message) {
        String formattedBody = "<html><body>" +
                "<p>" + message + "</p>" +
                "<p><i>Παρακαλώ φορτίστε τον λογαριασμό σας για να εκτελεστεί η εντολή.</i></p>" +
                "</body></html>";
        
        messageSender.sendMessage("Ειδοποίηση Πάγιας Εντολής", formattedBody);
    }
}