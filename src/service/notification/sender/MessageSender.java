package service.notification.sender;
public interface MessageSender {
    void sendMessage(String subject, String body);
}