package data.dao;

import java.time.LocalDate;

public interface SystemDAO {
    LocalDate load();
    void save(LocalDate date);
}