package Services;

import Models.Space;
import Utilities.DataBaseManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class SpaceService {

    public Space update(Space s) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            em.getTransaction().begin();
            Space managed;
            if (s.getId() == null) {
                em.persist(s);
                managed = s;
            } else {
                managed = em.merge(s);
            }
            em.getTransaction().commit();
            return managed;
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
    }

    public Space findById(Long id) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            return em.find(Space.class, id);
        } finally {
            em.close();
        }
    }

    public List<Space> findAll() {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            TypedQuery<Space> q = em.createQuery(
                    "SELECT s FROM Space s", Space.class);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public void delete(Space s) {
        EntityManager em = Utilities.DataBaseManager.getEntityManager();
        try {
            em.getTransaction().begin();
            Space attached = em.find(Space.class, s.getId());
            if (attached != null) {
                em.remove(attached);
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
