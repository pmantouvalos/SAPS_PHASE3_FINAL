package service.bridge;

import data.BankDataStore;
import model.entities.Account;
import java.util.UUID;

public class InternalProtocol implements TransferProtocol {

    @Override
    public String executeTransfer(Account source, String targetIban, double amount) throws Exception {
        Account target = BankDataStore.getInstance().getAccountByIban(targetIban);
        if (target == null) throw new Exception("Ο λογαριασμός προορισμού δεν βρέθηκε.");

        target.deposit(amount);
        
        // Επιστρέφουμε ένα τοπικό ID
        return "INT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Override
    public String getProtocolName() { return "INTERNAL"; }
}