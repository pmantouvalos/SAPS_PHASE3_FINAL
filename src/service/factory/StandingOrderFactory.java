package service.factory;

import model.entities.StandingOrder;
import service.builder.StandingOrderBuilder;
import java.time.LocalDate;

public class StandingOrderFactory {

    public static StandingOrder createOrder(String type, String srcIban, String target, double amount, String desc) {
        return new StandingOrderBuilder()
                .setType(type)
                .setSourceIban(srcIban)
                .setTarget(target)
                .setAmount(amount)
                .setDescription(desc)
                .setFrequencyDays(30) // Default monthly
                .setStartDate(LocalDate.now())
                .build();
    }
}