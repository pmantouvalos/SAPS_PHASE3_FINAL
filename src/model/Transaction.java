package model;

import java.time.LocalDate;
import java.util.UUID;

public class Transaction {
    private String id;
    private LocalDate date;
    private long timestamp;
    private String description;
    private double amount;
    private String type;
    
    private double fee;
    private double balanceAfter;
    private String sender;
    private String receiver;

    private Transaction(Builder builder) {
        this.id = builder.id;
        this.date = builder.date;
        this.timestamp = builder.timestamp;
        this.description = builder.description;
        this.amount = builder.amount;
        this.type = builder.type;
        this.fee = builder.fee;
        this.balanceAfter = builder.balanceAfter;
        this.sender = builder.sender;
        this.receiver = builder.receiver;
    }

    public String getId() { return id; }
    public LocalDate getDate() { return date; }
    public long getTimestamp() { return timestamp; } //Getter
    public String getDescription() { return description; }
    public double getAmount() { return amount; }
    public String getType() { return type; }
    public double getFee() { return fee; }
    public double getBalanceAfter() { return balanceAfter; }
    public String getSender() { return sender; }
    public String getReceiver() { return receiver; }

    public static class Builder {
        private String id;
        private double amount;
        private LocalDate date;
        private long timestamp;
        private String description = "";
        private String type = "Συναλλαγή";
        private double fee = 0.0;
        private double balanceAfter = 0.0;
        private String sender = "-";
        private String receiver = "-";

        public Builder(double amount) {
            this.id = UUID.randomUUID().toString(); //Μοναδικό ID
            this.amount = amount;
            this.date = LocalDate.now(); 
            this.timestamp = System.currentTimeMillis(); //Καταγραφή ώρας δημιουργίας
        }

        public Builder setType(String type) { this.type = type; return this; }
        public Builder setDescription(String description) { this.description = description; return this; }
        
        //Όταν ορίζουμε την ημερομηνία (από TimeManager), το timestamp παραμένει το τρέχον
        //ώστε να ξέρουμε ποια έγινε πρώτη και ποια δεύτερη
        public Builder setDate(LocalDate date) { this.date = date; return this; }
        
        public Builder setFee(double fee) { this.fee = fee; return this; }
        public Builder setBalanceAfter(double bal) { this.balanceAfter = bal; return this; }
        public Builder setSender(String s) { this.sender = s; return this; }
        public Builder setReceiver(String r) { this.receiver = r; return this; }

        public Transaction build() { return new Transaction(this); }
    }
}