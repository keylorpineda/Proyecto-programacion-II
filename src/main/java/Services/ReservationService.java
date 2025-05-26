// Services/ReservationService.java
package Services;

import Models.Reservation;
import Utilities.DataBaseManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ReservationService {

    public Reservation save(Reservation r) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(r);
            em.getTransaction().commit();
            return r;
        } finally {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            em.close();
        }
    }
public long countReservationsForSpace(Long spaceId, LocalDate date,
                                      LocalDateTime start, LocalDateTime end) {
    EntityManager em = DataBaseManager.getEntityManager();
    try {
        TypedQuery<Long> q = em.createQuery(
            "SELECT COUNT(r) FROM Reservation r " +
            "WHERE r.space.id = :sid " +
            "  AND r.dateCreated = :date " +
            "  AND r.startTime = :start " +
            "  AND r.endTime = :end", Long.class);
        q.setParameter("sid",   spaceId);
        q.setParameter("date",  date.atStartOfDay());
        q.setParameter("start", start);
        q.setParameter("end",   end);
        return q.getSingleResult();
    } finally {
        em.close();
    }
}

    public Reservation findById(Long id) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            return em.find(Reservation.class, id);
        } finally {
            em.close();
        }
    }

    public List<Reservation> findAll() {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            TypedQuery<Reservation> q = em.createQuery(
                "SELECT r FROM Reservation r", Reservation.class);
            return q.getResultList();
        } finally {
            em.close();
        }
    }
}
