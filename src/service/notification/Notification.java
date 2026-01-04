package service.notification;

import service.notification.sender.MessageSender;

public abstract class Notification {
    protected MessageSender messageSender; //Η "Γέφυρα"

    public Notification(MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    //Αυτή είναι η μέθοδος που πρέπει να κάνουν Override τα παιδιά
    public abstract void send(String message); 
}