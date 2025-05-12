package Services;

import Models.Reservation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import com.mycompany.proyectoprogramacionii.DataBaseManager;
import java.time.LocalDateTime;
import java.util.List;

public class ReservationManager {
    private static ReservationManager instance;

    private ReservationManager() {}

    public static ReservationManager getInstance() {
        if (instance == null) {
            instance = new ReservationManager();
        }
        return instance;
    }

    public boolean createReservation(Reservation reservation) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            em.getTransaction().begin();
            reservation.getSpace().setReserved(true);
            em.merge(reservation.getSpace());
            em.persist(reservation);
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

    public boolean cancelReservation(String reservationId) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            em.getTransaction().begin();
            Reservation reservation = em.find(Reservation.class, reservationId);
            if (reservation != null) {
                reservation.getSpace().unreserved();
                em.merge(reservation.getSpace());
                em.remove(reservation);
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

    public List<Reservation> getReservationsByUser(String username) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            TypedQuery<Reservation> query = em.createQuery(
                "SELECT r FROM Reservation r WHERE r.user.userName = :username",
                Reservation.class);
            query.setParameter("username", username);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public boolean isSpaceAvailable(String spaceId, LocalDateTime start, LocalDateTime end) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            TypedQuery<Reservation> query = em.createQuery(
                "SELECT r FROM Reservation r WHERE r.space.spaceId = :spaceId AND " +
                "(:end > r.startTime AND :start < r.endTime)", Reservation.class);
            query.setParameter("spaceId", spaceId);
            query.setParameter("start", start);
            query.setParameter("end", end);
            return query.getResultList().isEmpty();
        } finally {
            em.close();
        }
    }
}
