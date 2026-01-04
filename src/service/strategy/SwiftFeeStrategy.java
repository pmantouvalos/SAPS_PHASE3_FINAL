package service.strategy;

public class SwiftFeeStrategy implements FeeStrategy {
    @Override
    public double calculateFee(double amount) {
        // Π.χ. 5.00€ σταθερά συν 1% του ποσού
        return 5.00 + (amount * 0.01);
    }
}