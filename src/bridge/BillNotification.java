package bridge;

public class BillNotification extends Notification {

    public BillNotification(MessageSender sender) {
        super(sender);
    }

    @Override
    public void send(String message) {
        String formattedBody = "<html><body>" +
                "<h3 style='color:blue;'> Ειδοποίηση Λογαριασμού</h3>" +
                "<p>" + message + "</p>" +
                "</body></html>";
        
        messageSender.sendMessage("Υπενθύμιση Λογαριασμού", formattedBody);
    }
}