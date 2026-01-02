package model;

import java.io.Serializable;
import java.time.LocalDate;

public class StandingOrder implements Serializable{
    private String type;        //"Μεταφορά" ή "Πληρωμή"
    private String sourceIban;  //Από ποιον λογαριασμό
    private String target;      //IBAN παραλήπτη ή Κωδικός RF
    private double amount;
    private String description;
    
    // Πεδία Χρονισμού (βάσει Storyboard)
    private int frequencyDays;        //"Επανάληψη ανά Χ ημέρες"
    private LocalDate startDate;      //"Ημερομηνία Έναρξης"
    private LocalDate endDate;        // "Ημερομηνία Λήξης"
    private LocalDate nextExecutionDate; //Πότε θα τρέξει ξανά
    
    private boolean active; //Ενεργή / Ανενεργή

    public StandingOrder(String type, String sourceIban, String target, double amount, 
                         String description, int frequencyDays, LocalDate startDate, LocalDate endDate) {
        this.type = type;
        this.sourceIban = sourceIban;
        this.target = target;
        this.amount = amount;
        this.description = description;
        this.frequencyDays = frequencyDays;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = true;
        
        //Η πρώτη εκτέλεση είναι η ημερομηνία έναρξης
        this.nextExecutionDate = startDate;
    }

    //Getters kai Setters
    public String getType() { return type; }
    public String getSourceIban() { return sourceIban; }
    public String getTarget() { return target; } //Επιστρέφει IBAN ή RF
    
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    
    public String getDescription() { return description; }
    
    public int getFrequencyDays() { return frequencyDays; }
    public void setFrequencyDays(int frequencyDays) { this.frequencyDays = frequencyDays; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    
    public LocalDate getNextExecutionDate() { return nextExecutionDate; }
    public void setNextExecutionDate(LocalDate date) { this.nextExecutionDate = date; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    //Μέθοδος που καλείται μετά από κάθε επιτυχημένη εκτέλεση
    public void updateNextExecutionDate() {
        this.nextExecutionDate = this.nextExecutionDate.plusDays(frequencyDays);
    }
}