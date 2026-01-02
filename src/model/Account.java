package model;

import java.util.ArrayList;
import java.util.List;

public abstract class Account {
    protected String iban;
    protected String ownerName; //Full Name
    protected double balance;
    protected List<Transaction> transactions;
    protected List<JointOwner> jointOwners;

    public Account(String iban, String ownerName, double balance) {
        this.iban = iban;
        this.ownerName = ownerName;
        this.balance = balance;
        this.transactions = new ArrayList<>();
        this.jointOwners = new ArrayList<>();
    }

    //Getters kai Setters
    public String getIban() { return iban; }
    public String getOwnerName() { return ownerName; }
    public double getBalance() { return balance; }
    public List<Transaction> getTransactions() { return transactions; }
    public List<JointOwner> getJointOwners() { return jointOwners; }

    public void setBalance(double balance) { this.balance = balance; }
    public void addTransaction(Transaction t) { this.transactions.add(0, t); }
    public abstract String getAccountType();

    public void addJointOwner(JointOwner jo) {
        this.jointOwners.add(jo);
    }

    public void removeJointOwner(String afm) {
        jointOwners.removeIf(jo -> jo.getAfm().equals(afm));
    }

    //INNER CLASS: JointOwner (Συνδικαιούχος)
    public static class JointOwner {
        private String name;
        private String surname;
        private String afm;
        private String accessLevel; // "Πλήρης", "Μερική", "Προβολή"

        public JointOwner(String name, String surname, String afm, String accessLevel) {
            this.name = name;
            this.surname = surname;
            this.afm = afm;
            this.accessLevel = accessLevel;
        }

        public String getName() { return name; }
        public String getSurname() { return surname; }
        public String getAfm() { return afm; }
        public String getAccessLevel() { return accessLevel; }
        public void setAccessLevel(String level) { this.accessLevel = level; }
        
        @Override
        public String toString() { return surname + " " + name; }
    }
    
    public void withdraw(double amount) {
        if (amount > 0 && this.balance >= amount) {
            this.balance -= amount;
        } else {
            throw new IllegalArgumentException("Μη έγκυρο ποσό ή ανεπαρκές υπόλοιπο.");
        }
    }
    
    public void deposit(double amount) {
        if (amount > 0) {
            this.balance += amount;
        } else {
            throw new IllegalArgumentException("Το ποσό κατάθεσης πρέπει να είναι θετικό.");
        }
    }
    
}