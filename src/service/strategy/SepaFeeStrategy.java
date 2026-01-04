package service.strategy;

public class SepaFeeStrategy implements FeeStrategy {
    @Override
    public double calculateFee(double amount) {
        // Π.χ. 2.00€ σταθερά συν 0.1% του ποσού
        return 2.00 + (amount * 0.001); 
    }
}