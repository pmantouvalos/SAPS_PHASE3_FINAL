package service.strategy;

public interface FeeStrategy {
    // Υπολογίζει την προμήθεια με βάση το ποσό της συναλλαγής
    double calculateFee(double amount);
}