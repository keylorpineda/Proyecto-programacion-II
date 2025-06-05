package Services;

import Models.Reservation;
import Models.Space;
import Models.SpaceType;
import Utilities.DataBaseManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ReservationService {

    public Reservation save(Reservation r) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(r);
            em.getTransaction().commit();
            return r;
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
    }

    public List<Reservation> findBySpaceAndDateAndTime(Long spaceId, LocalDate date, LocalTime start, LocalTime end) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            TypedQuery<Reservation> q = em.createQuery(
                    "SELECT r FROM Reservation r "
                    + "WHERE r.space.id = :sid "
                    + "AND r.dateCreated = :date "
                    + "AND r.startTime < :end AND r.endTime > :start",
                    Reservation.class
            );
            q.setParameter("sid", spaceId);
            q.setParameter("date", date.atStartOfDay());
            q.setParameter("start", start.atDate(date));
            q.setParameter("end", end.atDate(date));
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Reservation> findByRoomAndDateAndTime(Long roomId, LocalDate date, String startStr, String endStr) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            TypedQuery<Reservation> q = em.createQuery(
                    "SELECT r FROM Reservation r "
                    + "WHERE r.space.room.id = :roomId "
                    + "AND r.date = :date "
                    + "AND ( "
                    + "   (r.startTime < :end AND r.endTime > :start) "
                    + // Hay cruce de horario
                    ")",
                    Reservation.class
            );
            q.setParameter("roomId", roomId);
            q.setParameter("date", date);
            q.setParameter("start", java.time.LocalTime.parse(startStr));
            q.setParameter("end", java.time.LocalTime.parse(endStr));
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Reservation> findBySpaceAndDate(Long spaceId, LocalDate date) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            TypedQuery<Reservation> q = em.createQuery(
                    "SELECT r FROM Reservation r "
                    + "WHERE r.space.id = :sid "
                    + "AND r.dateCreated = :date",
                    Reservation.class
            );
            q.setParameter("sid", spaceId);
            q.setParameter("date", date.atStartOfDay());
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Reservation> findByRoomAndDateAndTime(Long roomId, LocalDate date, LocalTime start, LocalTime end) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            TypedQuery<Reservation> q = em.createQuery(
                    "SELECT r FROM Reservation r "
                    + "WHERE r.space.room.id = :roomId "
                    + "AND r.dateCreated = :date "
                    + "AND r.startTime < :end AND r.endTime > :start",
                    Reservation.class
            );
            q.setParameter("roomId", roomId);
            q.setParameter("date", date.atStartOfDay());
            q.setParameter("start", start.atDate(date));
            q.setParameter("end", end.atDate(date));
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Reservation> findAll() {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            TypedQuery<Reservation> q = em.createQuery(
                    "SELECT r FROM Reservation r", Reservation.class
            );
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public long countReservationsForSpace(Long spaceId, LocalDate date, LocalTime start, LocalTime end) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            TypedQuery<Long> q = em.createQuery(
                    "SELECT COUNT(r) FROM Reservation r "
                    + "WHERE r.space.id = :sid "
                    + "AND r.dateCreated = :date "
                    + "AND r.startTime < :end AND r.endTime > :start",
                    Long.class
            );
            q.setParameter("sid", spaceId);
            q.setParameter("date", date.atStartOfDay());
            q.setParameter("start", start.atDate(date));
            q.setParameter("end", end.atDate(date));
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

    public Map<String, Long> countReservationsByUser() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public Map<SpaceType, Long> countReservationsBySpaceType() {
        Map<SpaceType, Long> result = new HashMap<>();
        for (Reservation r : findAll()) {
            if (r.getSpace() != null && r.getSpace().getType() != null) {
                result.merge(r.getSpace().getType(), 1L, Long::sum);
            }
        }
        return result;
    }

   public Map<String, Long> getTopUsersByReservations() {
        Map<String, Long> result = new HashMap<>();
        for (Reservation r : findAll()) {
            if (r.getUser() != null && r.getUser().getUserName() != null) {
                result.merge(r.getUser().getUserName(), 1L, Long::sum);
            }
        }
        return result;
    }

    public Map<String, Long> countReservationsByHourSlot() {
        Map<String, Long> result = new TreeMap<>();
        for (Reservation r : findAll()) {
            if (r.getStartTime() != null) {
                String hora = r.getStartTime().toLocalTime().toString();
                result.merge(hora, 1L, Long::sum);
            }
        }
        return result;
    }

}
