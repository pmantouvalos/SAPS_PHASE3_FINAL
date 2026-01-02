package model;

import java.time.LocalDate;

public class Bill {
    private String ownerAfm; //Το ΑΦΜ του χρήστη που χρωστάει
    private String rfCode;
    private String provider;
    private double amount;
    private String description;
    private LocalDate issueDate;
    private LocalDate dueDate;

    //Ενημερωμένος Constructor
    public Bill(String ownerAfm, String rfCode, String provider, double amount, String description, LocalDate issueDate, LocalDate dueDate) {
        this.ownerAfm = ownerAfm;
        this.rfCode = rfCode;
        this.provider = provider;
        this.amount = amount;
        this.description = description;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
    }

    //Getter για το ΑΦΜ
    public String getOwnerAfm() { return ownerAfm; }

    //Υπόλοιποι Getters
    public String getRfCode() { return rfCode; }
    public String getProvider() { return provider; }
    public double getAmount() { return amount; }
    public String getDescription() { return description; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getDueDate() { return dueDate; }

}