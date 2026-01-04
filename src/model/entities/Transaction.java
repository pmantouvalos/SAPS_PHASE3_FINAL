package model.entities;

import model.enums.TransactionType;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

public class Transaction implements Serializable {
    private String id;
    private double amount;
    private TransactionType type; // Enum
    private String description;
    private LocalDate date;
    private String senderIban;
    private String receiverIban;
    private double fee;
    private double balanceAfter;
    private long timestamp;

    // PUBLIC CONSTRUCTOR (Καλείται από τον εξωτερικό TransactionBuilder)
    public Transaction(double amount, TransactionType type, String description, 
                       LocalDate date, String senderIban, String receiverIban, 
                       double fee, double balanceAfter) {
        this.id = UUID.randomUUID().toString();
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.date = date;
        this.senderIban = senderIban;
        this.receiverIban = receiverIban;
        this.fee = fee;
        this.balanceAfter = balanceAfter;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters
    public String getId() { return id; }
    public double getAmount() { return amount; }
    public TransactionType getType() { return type; }
    public String getDescription() { return description; }
    public LocalDate getDate() { return date; }
    public String getSender() { return senderIban; }
    public String getReceiver() { return receiverIban; }
    public double getFee() { return fee; }
    public double getBalanceAfter() { return balanceAfter; }
    public long getTimestamp() { return timestamp; }
 // ... υπόλοιπος κώδικας ...

 
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }


}