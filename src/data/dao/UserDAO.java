package data.dao;

import model.entities.User;
import java.util.List;

public interface UserDAO {
    List<User> load();
    void save(List<User> users);
}