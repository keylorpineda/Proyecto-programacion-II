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
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
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
}

