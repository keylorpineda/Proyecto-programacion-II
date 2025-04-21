package Services;
import Models.Customer;
import Models.Administrator;
import javax.persistence.Table;
import Models.User;
import com.mycompany.proyectoprogramacionii.DataBaseManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class UserService {
    public User authenticate(String username, String password) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            TypedQuery<User> q = em.createQuery(
                "SELECT u FROM User u WHERE u.userName = :u AND u.password = :p",
                User.class);
            q.setParameter("u", username);
            q.setParameter("p", password);
            return q.getResultStream().findFirst().orElse(null);
        } finally {
            em.close();
        }
    }

    public void registerCustomer(Customer newCustomer) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(newCustomer);
            em.getTransaction().commit();
        } finally {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            em.close();
        }
    }

    public void createAdministrator(User creator, Administrator newAdmin) {
        if (!(creator.isAdmin())) {
            throw new SecurityException("Solo un administrador puede crear otro administrador");
        }
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(newAdmin);
            em.getTransaction().commit();
        } finally {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            em.close();
        }
    }

    public List<User> findAllUsers(User requester) {
        if (!(requester.isAdmin())) {
            throw new SecurityException("Solo un administrador puede listar usuarios");
        }
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            TypedQuery<User> q = em.createQuery("SELECT u FROM User u", User.class);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public void updateUser(User creator, User updatedUser) {
        if (!(creator.isAdmin())) {
            throw new SecurityException("Solo un administrador puede modificar usuarios");
        }
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(updatedUser);
            em.getTransaction().commit();
        } finally {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            em.close();
        }
    }

    public void deleteUser(User creator, String usernameToDelete) {
        if (!(creator.isAdmin())) {
            throw new SecurityException("Solo un administrador puede eliminar usuarios");
        }
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            em.getTransaction().begin();
            User u = em.find(User.class, usernameToDelete);
            if (u != null) {
                em.remove(u);
            }
            em.getTransaction().commit();
        } finally {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            em.close();
        }
    }
}
