package Utilities;

import Models.Administrator;
import Models.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import Services.UserService;

public class DataBaseManager {

    private static final EntityManagerFactory emf =
        Persistence.createEntityManagerFactory("CoworkingProgram");

    public static void init() {
        seedDefaultAdmin();
    }

    private static void seedDefaultAdmin() {
        
        UserService userService = new UserService();

        boolean hasAdmin = userService.findAll()
                         .stream()
                         .anyMatch(User::isAdmin);
if (!hasAdmin) {
            Administrator sysAdmin = new Administrator(
                1L,
                "Sistema",
                "Administrador",
                "admin@coworking.com",
                "123456"
            );
            userService.save(sysAdmin);
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
