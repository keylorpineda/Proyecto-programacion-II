package Utilities;

import Models.Administrator;
import Models.User;
import Services.UserManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class DataBaseManager {

    private static final EntityManagerFactory emf
            = Persistence.createEntityManagerFactory("CoworkingProgram");

    public static void init() {
        seedDefaultAdmin();
    }

    private static void seedDefaultAdmin() {
        UserManager um = UserManager.getInstance();
        boolean hasAdmin = um.getAllUsers()
                .stream()
                .anyMatch(User::isAdmin);
        if (!hasAdmin) {
            Administrator sysAdmin = new Administrator(
                    "Sistema",
                    "Administrador",
                    "000000000",
                    "admin",
                    "123456"
            );
            um.addUser(sysAdmin);
        }
    }

    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public static void close() {
        if (emf.isOpen()) {
            emf.close();
        }
    }
}
