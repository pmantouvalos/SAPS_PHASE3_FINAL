package service.builder;

import model.entities.Transaction;
import model.enums.TransactionType;
import java.time.LocalDate;

public class TransactionBuilder {
    private double amount;
    private TransactionType type = TransactionType.TRANSFER;
    private String description = "";
    private LocalDate date = LocalDate.now();
    private String senderIban = "-";
    private String receiverIban = "-";
    private double fee = 0.0;
    private double balanceAfter = 0.0;

    public TransactionBuilder(double amount) {
        this.amount = amount;
    }

    public TransactionBuilder setType(TransactionType type) { this.type = type; return this; }
    
    // Helper για String (αν χρειαστεί)
    public TransactionBuilder setType(String typeStr) {
        try { this.type = TransactionType.valueOf(typeStr); } 
        catch (Exception e) { this.type = TransactionType.TRANSFER; }
        return this;
    }

    public TransactionBuilder setDescription(String desc) { this.description = desc; return this; }
    public TransactionBuilder setDate(LocalDate date) { this.date = date; return this; }
    public TransactionBuilder setSender(String sender) { this.senderIban = sender; return this; }
    public TransactionBuilder setReceiver(String receiver) { this.receiverIban = receiver; return this; }
    public TransactionBuilder setFee(double fee) { this.fee = fee; return this; }
    public TransactionBuilder setBalanceAfter(double balance) { this.balanceAfter = balance; return this; }

    public Transaction build() {
        // Καλεί τον Public Constructor της Transaction
        return new Transaction(amount, type, description, date, senderIban, receiverIban, fee, balanceAfter);
    }
}