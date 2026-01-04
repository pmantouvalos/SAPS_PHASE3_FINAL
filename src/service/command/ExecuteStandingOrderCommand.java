package service.command;

import model.entities.StandingOrder;
import service.factory.CommandFactory; // Import το Factory

public class ExecuteStandingOrderCommand implements Command {
    
    private StandingOrder order;

    public ExecuteStandingOrderCommand(StandingOrder order) {
        this.order = order;
    }

    @Override
    public void execute() {
        // Design Pattern: FACTORY
        // Ζητάμε από το Factory να μας δώσει την κατάλληλη εντολή (Transfer ή Payment)
        // Το Factory έχει ήδη ρυθμίσει το InternalProtocol για τις μεταφορές
        Command cmd = CommandFactory.createCommandFromOrder(this.order);

        // Αν το Factory επέστρεψε έγκυρη εντολή, την εκτελούμε
        if (cmd != null) {
            cmd.execute();
        } else {
            System.err.println("Άγνωστος τύπος πάγιας εντολής ή σφάλμα δεδομένων: " + order.getType());
        }
    }
}