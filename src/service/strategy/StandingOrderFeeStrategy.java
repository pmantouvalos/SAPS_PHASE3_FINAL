package service.strategy;

public class StandingOrderFeeStrategy implements FeeStrategy {

    @Override
    public double calculateFee(double amount) {
        // Σταθερή χρέωση 0.50€ για την εκτέλεση πάγιας εντολής
        return 0.50;
    }
}