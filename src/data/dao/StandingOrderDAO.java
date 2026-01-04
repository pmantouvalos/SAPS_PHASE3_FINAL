package data.dao;

import model.entities.StandingOrder;
import java.util.List;

public interface StandingOrderDAO {
    List<StandingOrder> load();
    void save(List<StandingOrder> orders);
}