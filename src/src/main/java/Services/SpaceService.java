package Services;

import Models.Space;
import Models.SpaceBlock;
import Utilities.DataBaseManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class SpaceService {

    public void update(Space s) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            em.getTransaction().begin();
            if (s.getId() == null) {
                em.persist(s);
            } else {
                em.merge(s);
            }
            em.getTransaction().commit();
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
    }

    public void delete(Space s) {
        EntityManager em = DataBaseManager.getEntityManager();
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

    public void blockSpace(Space space, LocalDate date, LocalTime startTime, LocalTime endTime) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            em.getTransaction().begin();
            SpaceBlock block = new SpaceBlock(space, date, startTime, endTime);
            em.persist(block);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public List<Space> findBlockedSpaces(Long roomId, LocalDate date, LocalTime start, LocalTime end) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            TypedQuery<SpaceBlock> q = em.createQuery(
                    "SELECT b FROM SpaceBlock b WHERE b.room.id = :roomId AND b.date = :date AND b.start = :start AND b.end = :end",
                    SpaceBlock.class);
            q.setParameter("roomId", roomId);
            q.setParameter("date", date);
            q.setParameter("start", start);
            q.setParameter("end", end);

            List<SpaceBlock> bloques = q.getResultList();
            List<Space> espacios = new ArrayList<>();
            for (int i = 0; i < bloques.size(); i++) {
                espacios.add(bloques.get(i).getSpace());
            }
            return espacios;
        } finally {
            em.close();
        }
    }
}
