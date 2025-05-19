package Services;

import Models.User;
import Models.Customer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import Utilities.DataBaseManager;

public class UserManager {

    private static UserManager instance;
    private User currentUser;

    private UserManager() {
    }

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    public void addUser(User user) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
            System.out.println("✔ Usuario persistido correctamente: " + user.getUserName());
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public boolean authenticateUser(String username, String password) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            TypedQuery<User> q = em.createQuery(
                    "SELECT u FROM User u WHERE u.userName = :u AND u.password = :p",
                    User.class);
            q.setParameter("u", username);
            q.setParameter("p", password);
            User found = q.getResultStream().findFirst().orElse(null);
            if (found != null) {
                this.currentUser = found;
                return true;
            }
            return false;
        } finally {
            em.close();
        }
    }

    public User getUserByUsername(String username) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            return em.find(User.class, username);
        } finally {
            em.close();
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        this.currentUser = null;
    }
}
