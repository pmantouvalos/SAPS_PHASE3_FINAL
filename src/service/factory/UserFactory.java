package service.factory;

import model.entities.User;
import model.enums.Role;
import service.builder.UserBuilder;

public class UserFactory {

    public static User createUser(String username, String password, String fullName, String roleStr) {
        Role role;
        try {
            role = Role.valueOf(roleStr.toUpperCase());
        } catch (Exception e) {
            role = Role.INDIVIDUAL; // Default
        }
        return createUser(username, password, fullName, role);
    }

    public static User createUser(String username, String password, String fullName, Role role) {
        UserBuilder builder = new UserBuilder(username, password, fullName, role);

        // Business Logic: Defaults ανάλογα τον ρόλο
        if (role == Role.ADMIN) {
            // Οι admins ίσως δεν χρειάζονται όρια συναλλαγών
            builder.setLimits(999999, 999999, 999999); 
        } else if (role == Role.BUSINESS) {
            // Οι εταιρικοί πελάτες έχουν μεγαλύτερα όρια
            builder.setLimits(5000, 2000, 10000);
        } else {
            // Απλοί πελάτες (Defaults από τον Builder: 1000, 600, 1500)
        }

        return builder.build();
    }
}