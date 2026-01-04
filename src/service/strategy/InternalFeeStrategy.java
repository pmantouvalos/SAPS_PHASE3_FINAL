package service.strategy;

public class InternalFeeStrategy implements FeeStrategy {
    @Override
    public double calculateFee(double amount) {
        // Π.χ. Σταθερή χρέωση 0.50€ για εσωτερικές μεταφορές
        return 0.50; 
    }
}