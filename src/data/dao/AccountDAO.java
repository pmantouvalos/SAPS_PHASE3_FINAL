package data.dao;

import model.entities.Account;
import java.util.List;

public interface AccountDAO {
    List<Account> loadAccounts();
    void saveAccounts(List<Account> accounts);
}