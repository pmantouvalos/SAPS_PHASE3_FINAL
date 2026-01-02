package bridge;

public class TransactionNotification extends Notification {

    public TransactionNotification(MessageSender sender) {
        super(sender);
    }

    @Override
    public void send(String message) {
        //Προσθήκη επικεφαλίδας για συναλλαγές
        messageSender.sendMessage("E-Banking Transaction Alert", message);
    }
}