package model.enums;

public enum AccountType {
    CURRENT("Τρεχούμενος"),
    SAVINGS("Ταμιευτηρίου"),
    BUSINESS("Επαγγελματικός");

    private final String label;
    
    AccountType(String label) { 
    	this.label = label; }
    
    public String getLabel() { 
    	return label; }
    
    // Χρήσιμο για το CSV loading
    public static AccountType fromString(String text) {
        for (AccountType b : AccountType.values()) {
            if (b.label.equalsIgnoreCase(text) || b.name().equalsIgnoreCase(text)) {
            	return b;
            }
        }
        return CURRENT;
    }
}