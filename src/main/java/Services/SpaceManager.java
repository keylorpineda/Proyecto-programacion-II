package Services;

import Models.Space;
import Utilities.DataBaseManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class SpaceManager {

    private static SpaceManager instance;

    private SpaceManager() { }

    public static SpaceManager getInstance() {
        if (instance == null) {
            instance = new SpaceManager();
        }
        return instance;
    }

    public List<Space> getAllSpaces() {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            TypedQuery<Space> query = em.createQuery(
                "SELECT s FROM Space s", Space.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Space> getSpacesByRoom(String roomId) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            TypedQuery<Space> query = em.createQuery(
                "SELECT s FROM Space s WHERE s.room.idRoom = :rid",
                Space.class);
            query.setParameter("rid", roomId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Space getSpaceById(String spaceId) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            return em.find(Space.class, spaceId);
        } finally {
            em.close();
        }
    }

    public boolean reserveSpace(String spaceId) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            Space space = em.find(Space.class, spaceId);
            if (space != null && !space.getReserved()) {
                em.getTransaction().begin();
                space.setReserved(true);
                em.merge(space);
                em.getTransaction().commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    public List<Space> getAvailableSpaces() {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            TypedQuery<Space> query = em.createQuery(
                "SELECT s FROM Space s WHERE s.isReserved = false",
                Space.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public boolean addSpace(Space space) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            if (em.find(Space.class, space.getSpaceId()) != null) {
                return false;
            }
            em.getTransaction().begin();
            em.persist(space);
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    public boolean deleteSpace(String spaceId) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            Space space = em.find(Space.class, spaceId);
            if (space != null) {
                em.getTransaction().begin();
                em.remove(space);
                em.getTransaction().commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }
}
