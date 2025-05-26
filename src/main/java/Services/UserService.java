package Services;

import Models.User;
import Utilities.DataBaseManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class UserService {

    public void save(User u) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(u);
            em.getTransaction().commit();
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
    }

    public User authenticate(String username, String password) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            TypedQuery<User> q = em.createQuery(
                    "SELECT u FROM User u WHERE u.userName = :usr AND u.password = :pwd",
                    User.class);
            q.setParameter("usr", username);
            q.setParameter("pwd", password);
            return q.getResultList().stream().findFirst().orElse(null);
        } finally {
            em.close();
        }
    }

    public void update(User u) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(u);
            em.getTransaction().commit();
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
    }

    public List<User> findAll() {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            TypedQuery<User> q = em.createQuery(
                    "SELECT u FROM User u", User.class);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public User findByUserName(String userName) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            return em.find(User.class, userName);
        } finally {
            em.close();
        }
    }

    public User findByIdentification(String identification) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            return em.find(User.class, identification);
        } finally {
            em.close();
        }
    }

    public void delete(Long identification) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            em.getTransaction().begin();
            User u = em.find(User.class, identification);
            if (u != null) {
                em.remove(u);
            }
            em.getTransaction().commit();
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
    }
}
