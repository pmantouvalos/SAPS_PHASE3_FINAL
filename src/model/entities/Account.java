package model.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import service.observer.AccountObserver;

public abstract class Account implements Serializable {
    
    
    protected String iban;
    protected String ownerName; // Full Name
    protected double balance;
    protected List<Transaction> transactions;
    protected List<JointOwner> jointOwners;

    
    private transient List<AccountObserver> observers; // list of observers is not serializable

    public Account(String iban, String ownerName, double balance) {
        this.iban = iban;
        this.ownerName = ownerName;
        this.balance = balance;
        this.transactions = new ArrayList<>();
        this.jointOwners = new ArrayList<>();
        this.observers = new ArrayList<>();
    }

    // --- MONITOR OBJECT & BUSINESS LOGIC ---

    // Deposit(Thread-Safe)
    public synchronized void deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Το ποσό κατάθεσης πρέπει να είναι θετικό.");
        }
        this.balance += amount;
        notifyObservers(); 
    }

    // Withdraw (Thread-Safe)
    public synchronized void withdraw(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Το ποσό πρέπει να είναι θετικό.");
        }
        if (this.balance < amount) {
            throw new IllegalArgumentException("Ανεπαρκές υπόλοιπο.");
        }
        this.balance -= amount;
        notifyObservers(); // Ειδοποίηση UI
    }

    // Balance setting (Thread-Safe)
    public synchronized void setBalance(double balance) { 
        this.balance = balance; 
        notifyObservers(); // Ειδοποίηση UI
    }

    public synchronized void addTransaction(Transaction t) { 
        // Προσθήκη στην αρχή της λίστας για να φαίνονται πρώτα τα πιο πρόσφατα
        this.transactions.add(0, t); 
    }

    // --- OBSERVER PATTERN ---

    public void addObserver(AccountObserver o) {
        if (observers == null) observers = new ArrayList<>();
        observers.add(o);
    }

    public void removeObserver(AccountObserver o) {
        if (observers != null) observers.remove(o);
    }

    protected void notifyObservers() {
        // We check if list in null (it may happen after deserialization)
        if (observers != null) {
            for (AccountObserver o : observers) {
                o.onAccountChanged(this);
            }
        }
    }

    // --- JOINT OWNERS MANAGEMENT ---

    public void addJointOwner(JointOwner jo) {
        this.jointOwners.add(jo);
    }

    public void removeJointOwner(String afm) {
        jointOwners.removeIf(jo -> jo.getAfm().equals(afm));
    }

    // --- GETTERS ---
        public abstract String getAccountType(); 

    public String getIban() { 
    	return iban; }
    
    public String getOwnerName() { 
    	return ownerName; }
    
    public synchronized double getBalance() { 
    	return balance; } // Synchronized read
    
    public List<Transaction> getTransactions() { 
    	return transactions; }
    
    public List<JointOwner> getJointOwners() { 
    	return jointOwners; }

    // --- INNER CLASS: JointOwner ---
        public static class JointOwner implements Serializable {
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
}