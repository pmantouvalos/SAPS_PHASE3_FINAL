package model.enums;

public enum TransactionType {
    DEPOSIT("Κατάθεση"),
    WITHDRAWAL("Ανάληψη"),
    TRANSFER("Μεταφορά"),
    PAYMENT("Πληρωμή"),
    FEE("Προμήθεια"),
    INTEREST("Τόκοι");

    private final String label;
    
    TransactionType(String label) { 
    	this.label = label; }
    
    @Override public String toString() { 
    	return label; }
}