package service.builder;

import model.entities.Bill;
import java.time.LocalDate;

public class BillBuilder {
    private String ownerAfm;
    private String rfCode;
    private String provider;
    private double amount;
    private String description;
    private LocalDate issueDate;
    private LocalDate dueDate;

    public BillBuilder() {
        this.issueDate = LocalDate.now();
        this.dueDate = LocalDate.now().plusMonths(1); // Default 1 μήνας διορία
    }

    public BillBuilder setOwnerAfm(String afm) { this.ownerAfm = afm; return this; }
    public BillBuilder setRfCode(String rf) { this.rfCode = rf; return this; }
    public BillBuilder setProvider(String provider) { this.provider = provider; return this; }
    public BillBuilder setAmount(double amount) { this.amount = amount; return this; }
    public BillBuilder setDescription(String desc) { this.description = desc; return this; }
    public BillBuilder setIssueDate(LocalDate date) { this.issueDate = date; return this; }
    public BillBuilder setDueDate(LocalDate date) { this.dueDate = date; return this; }

    public Bill build() {
        // Χρήση του constructor της Bill
        return new Bill(ownerAfm, rfCode, provider, amount, description, issueDate, dueDate);
    }
}