package service.factory;

import model.entities.StandingOrder;
import service.bridge.InternalProtocol; // <-- ΝΕΟ IMPORT
import service.command.Command;
import service.command.PaymentCommand;
import service.command.TransferCommand;
import service.strategy.StandingOrderFeeStrategy; // Βεβαιώσου ότι υπάρχει αυτή η κλάση

public class CommandFactory {

    /**
     * Δέχεται μια Πάγια Εντολή και επιστρέφει το κατάλληλο Command.
     */
    public static Command createCommandFromOrder(StandingOrder order) {
        if (order == null || order.getType() == null) return null;

        String type = order.getType();

        if (type.equalsIgnoreCase("Transfer")) {
            // ΔΙΟΡΘΩΣΗ: Προσθέσαμε το 'new InternalProtocol()' στον constructor
            return new TransferCommand(
                order.getSourceIban(),
                order.getTarget(), 
                order.getAmount(),
                new StandingOrderFeeStrategy(), // Στρατηγική χρέωσης
                new InternalProtocol(),         // <-- Πρωτόκολλο (Bridge)
                order.getDescription(),
                true // Silent mode (για να μην πετάει popups όταν τρέχει αυτόματα)
            );
        } 
        else if (type.equalsIgnoreCase("Payment")) {
            // Η PaymentCommand δεν άλλαξε, οπότε παραμένει ως έχει
            return new PaymentCommand(
                order.getSourceIban(),
                order.getTarget(), 
                order.getAmount(),
                order.getDescription(),
                null // Δεν υπάρχει Bill object
            );
        }

        return null; 
    }
}