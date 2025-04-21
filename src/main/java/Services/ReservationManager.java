package Services;

import Models.Reservation;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationManager {
    private static ReservationManager instance;
    public List<Reservation> reservations;

    public ReservationManager() {
        reservations = new ArrayList<>();
    }

    public static ReservationManager getInstance() {
        if (instance == null) {
            instance = new ReservationManager();
        }
        return instance;
    }

    public boolean createReservation(Reservation reservation) {
        if (isSpaceAvailable(String.valueOf(reservation.getSpace().getSpaceId()), reservation.getStartTime(), reservation.getEndTime())) {
            reservations.add(reservation);
            reservation.getSpace().reserve();
            return true;
        }
        return false;
    }

    public boolean cancelReservation(String reservationId) {
        for (Reservation reservation : reservations) {
            if (reservation.getReservationId().equals(reservationId)) {
                reservation.getSpace().unreserved();
                reservations.remove(reservation);
                return true;
            }
        }
        return false;
    }

    public List<Reservation> getReservationsByUser(String username) {
        List<Reservation> userReservations = new ArrayList<>();
        for (Reservation reservation : reservations) {
            if (reservation.getUser().getUserName().equals(username)) {
                userReservations.add(reservation);
            }
        }
        return userReservations;
    }

    public boolean isSpaceAvailable(String spaceId, LocalDateTime start, LocalDateTime end) {
        for (Reservation reservation : reservations) {
            if (reservation.getSpace().getSpaceId().equals(spaceId)) {
                if (!(reservation.getEndTime().isBefore(start) || reservation.getStartTime().isAfter(end))) {
                    return false;
                }
            }
        }
        return true;
    }
}

