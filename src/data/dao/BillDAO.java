package data.dao;

import model.entities.Bill;
import java.util.List;

public interface BillDAO {
    List<Bill> load();
    void save(List<Bill> bills);
}