package service.builder;

import model.entities.StandingOrder;
import java.time.LocalDate;

public class StandingOrderBuilder {
    private String type; // e.g. "Transfer", "Payment"
    private String sourceIban;
    private String target; // IBAN or RF
    private double amount;
    private String description;
    private int frequencyDays = 30; // Default monthly
    private LocalDate startDate = LocalDate.now();
    private LocalDate endDate;

    public StandingOrderBuilder() {}

    public StandingOrderBuilder setType(String type) { this.type = type; return this; }
    public StandingOrderBuilder setSourceIban(String src) { this.sourceIban = src; return this; }
    public StandingOrderBuilder setTarget(String target) { this.target = target; return this; }
    public StandingOrderBuilder setAmount(double amount) { this.amount = amount; return this; }
    public StandingOrderBuilder setDescription(String desc) { this.description = desc; return this; }
    public StandingOrderBuilder setFrequencyDays(int days) { this.frequencyDays = days; return this; }
    public StandingOrderBuilder setStartDate(LocalDate date) { this.startDate = date; return this; }
    public StandingOrderBuilder setEndDate(LocalDate date) { this.endDate = date; return this; }

    public StandingOrder build() {
        return new StandingOrder(type, sourceIban, target, amount, description, frequencyDays, startDate, endDate);
    }
}