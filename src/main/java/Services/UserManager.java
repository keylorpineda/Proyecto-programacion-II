package Services;

import Models.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import Utilities.DataBaseManager;

import java.util.List;

public class UserManager {

    private static UserManager instance;
    private User currentUser;

    private UserManager() {}

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    public boolean addUser(User user) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    public List<User> getAllUsers() {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            TypedQuery<User> q = em.createQuery("SELECT u FROM User u", User.class);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public boolean deleteUser(String identification) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            User u = em.find(User.class, identification);
            if (u == null) return false;
            em.getTransaction().begin();
            em.remove(u);
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    public boolean updateUser(User user) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(user);
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            return false;
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

    public User getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        this.currentUser = null;
    }

    public User getUserByIdentification(String identification) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            TypedQuery<User> query = em.createQuery(
                "SELECT u FROM User u WHERE u.identification = :id", User.class);
            query.setParameter("id", identification);
            return query.getResultStream().findFirst().orElse(null);
        } finally {
            em.close();
        }
    }
}