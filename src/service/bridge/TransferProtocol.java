package service.bridge;

import model.entities.Account;

public interface TransferProtocol {
    /**
     * Εκτελεί τη μεταφορά.
     * @return Το Transaction ID αν πετύχει (π.χ. από το API ή generated).
     * @throws Exception Αν αποτύχει, με το μήνυμα του λάθους.
     */
    String executeTransfer(Account source, String targetIban, double amount) throws Exception;
    
    String getProtocolName();
}