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
        Long spaceId = r.getSpace().getId();
        LocalDateTime start = r.getStartTime(), end = r.getEndTime();
        int requested = r.getSeatCount(); 
        int capacity = r.getSpace().getCapacity();

       
        int already = getReservedSeats(spaceId, start, end);

        if (already + requested > capacity) {
            throw new IllegalArgumentException(
                    String.format("No hay suficientes asientos libres: %d ya reservados + %d solicitados > %d capacidad",
                            already, requested, capacity)
            );
        }

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

    public boolean cancelReservation(Long reservationId) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            em.getTransaction().begin();
            Reservation reservation = em.find(Reservation.class, reservationId);
            if (reservation != null) {
                em.remove(reservation);
                em.getTransaction().commit();
                return true;
            }
            em.getTransaction().rollback();
            return false;
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

    public boolean isSpaceAvailableForReservation(Long spaceId, LocalDateTime startTime, LocalDateTime endTime) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            LocalDate date = startTime.toLocalDate();
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

            TypedQuery<Long> q = em.createQuery(
                    "SELECT COUNT(r) FROM Reservation r "
                    + "WHERE r.space.id = :spaceId "
                    + "AND r.startTime >= :dayStart AND r.startTime < :dayEnd "
                    + "AND r.startTime < :endTime AND r.endTime > :startTime",
                    Long.class
            );
            q.setParameter("spaceId", spaceId);
            q.setParameter("dayStart", dayStart);
            q.setParameter("dayEnd", dayEnd);
            q.setParameter("startTime", startTime);
            q.setParameter("endTime", endTime);

            return q.getSingleResult() == 0;
        } finally {
            em.close();
        }
    }

    public List<Reservation> findBySpaceAndDateAndTime(Long spaceId, LocalDate date, LocalTime start, LocalTime end) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
            LocalDateTime startDateTime = LocalDateTime.of(date, start);
            LocalDateTime endDateTime = LocalDateTime.of(date, end);

            TypedQuery<Reservation> q = em.createQuery(
                    "SELECT r FROM Reservation r "
                    + "WHERE r.space.id = :sid "
                    + "AND r.startTime >= :dayStart AND r.startTime < :dayEnd "
                    + "AND r.startTime < :end AND r.endTime > :start",
                    Reservation.class
            );
            q.setParameter("sid", spaceId);
            q.setParameter("dayStart", dayStart);
            q.setParameter("dayEnd", dayEnd);
            q.setParameter("start", startDateTime);
            q.setParameter("end", endDateTime);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Reservation> findByRoomAndDateAndTime(Long roomId, LocalDate date, LocalTime start, LocalTime end) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
            LocalDateTime startDateTime = LocalDateTime.of(date, start);
            LocalDateTime endDateTime = LocalDateTime.of(date, end);

            TypedQuery<Reservation> q = em.createQuery(
                    "SELECT r FROM Reservation r "
                    + "WHERE r.space.room.id = :roomId "
                    + "AND r.startTime >= :dayStart AND r.startTime < :dayEnd "
                    + "AND r.startTime < :end AND r.endTime > :start",
                    Reservation.class
            );
            q.setParameter("roomId", roomId);
            q.setParameter("dayStart", dayStart);
            q.setParameter("dayEnd", dayEnd);
            q.setParameter("start", startDateTime);
            q.setParameter("end", endDateTime);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Reservation> findBySpaceAndDate(Long spaceId, LocalDate date) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

            TypedQuery<Reservation> q = em.createQuery(
                    "SELECT r FROM Reservation r "
                    + "WHERE r.space.id = :sid "
                    + "AND r.startTime >= :dayStart AND r.startTime < :dayEnd",
                    Reservation.class
            );
            q.setParameter("sid", spaceId);
            q.setParameter("dayStart", dayStart);
            q.setParameter("dayEnd", dayEnd);
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
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
            LocalDateTime startDateTime = LocalDateTime.of(date, start);
            LocalDateTime endDateTime = LocalDateTime.of(date, end);

            TypedQuery<Long> q = em.createQuery(
                    "SELECT COUNT(r) FROM Reservation r "
                    + "WHERE r.space.id = :sid "
                    + "AND r.startTime >= :dayStart AND r.startTime < :dayEnd "
                    + "AND r.startTime < :end AND r.endTime > :start",
                    Long.class
            );
            q.setParameter("sid", spaceId);
            q.setParameter("dayStart", dayStart);
            q.setParameter("dayEnd", dayEnd);
            q.setParameter("start", startDateTime);
            q.setParameter("end", endDateTime);
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
        throw new UnsupportedOperationException("Not supported yet.");
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

    public List<Reservation> findByUserName(String userName) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            TypedQuery<Reservation> q = em.createQuery(
                    "SELECT r FROM Reservation r "
                    + "WHERE r.user.userName = :userName "
                    + "ORDER BY r.startTime DESC",
                    Reservation.class
            );
            q.setParameter("userName", userName);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Reservation> findByUserId(Long userId) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            List<Reservation> reservations = em.createQuery(
                    "SELECT r FROM Reservation r WHERE r.user.id = :userId ORDER BY r.dateCreated DESC",
                    Reservation.class)
                    .setParameter("userId", userId)
                    .getResultList();

            System.out.println("Query ejecutada para userId: " + userId + ", resultados: " + reservations.size());
            return reservations;
        } finally {
            em.close();
        }
    }

    public int getReservedSeats(Long spaceId, LocalDateTime startTime, LocalDateTime endTime) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            TypedQuery<Long> q = em.createQuery(
                    "SELECT COALESCE(SUM(r.seatCount),0) "
                    + "  FROM Reservation r "
                    + " WHERE r.space.id = :spaceId "
                    + "   AND r.startTime < :endTime "
                    + "   AND r.endTime > :startTime",
                    Long.class);
            q.setParameter("spaceId", spaceId);
            q.setParameter("startTime", startTime);
            q.setParameter("endTime", endTime);
            return q.getSingleResult().intValue();
        } finally {
            em.close();
        }
    }
    
    public void delete(Reservation reservation) {
    EntityManager em = DataBaseManager.getEntityManager();
    try {
        em.getTransaction().begin();
        Reservation attached = em.find(Reservation.class, reservation.getId());
        if (attached != null) {
            em.remove(attached);
        }
        em.getTransaction().commit();
    } catch (Exception e) {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        e.printStackTrace();
    } finally {
        em.close();
    }
}
}
