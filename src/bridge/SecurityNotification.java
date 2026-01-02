package bridge;

public class SecurityNotification extends Notification {

    public SecurityNotification(MessageSender messageSender) {
        super(messageSender);
    }

    @Override
    public void send(String message) {
        
        String formattedBody = "!!! Security Alert !!!\n" + message + "\n";
        messageSender.sendMessage("Security Notification", formattedBody);
    }
}