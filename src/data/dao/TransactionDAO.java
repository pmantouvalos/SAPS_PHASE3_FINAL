package data.dao;

import model.entities.Account;
import java.util.List;

public interface TransactionDAO {
    // Η φόρτωση εδώ είναι ειδική γιατί "γεμίζει" τους υπάρχοντες λογαριασμούς
    void load(List<Account> accounts);
    void save(List<Account> accounts);
}