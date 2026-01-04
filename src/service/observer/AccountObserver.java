package service.observer;

import model.entities.Account;

public interface AccountObserver {
    /**
     * Καλείται αυτόματα όταν αλλάξει η κατάσταση του λογαριασμού
     * (π.χ. υπόλοιπο, νέα κίνηση).
     * * @param account Ο λογαριασμός που άλλαξε.
     */
    void onAccountChanged(Account account);
}