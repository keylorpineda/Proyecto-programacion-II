package Models;

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
        if (isSpaceAvailable(String.valueOf(reservation.space.spaceId), reservation.startTime, reservation.endTime)) {
            reservations.add(reservation);
            reservation.space.reserve();
            return true;
        }
        return false;
    }

    public boolean cancelReservation(String reservationId) {
        for (Reservation reservation : reservations) {
            if (reservation.reservationId.equals(reservationId)) {
                reservation.space.unReserve();
                reservations.remove(reservation);
                return true;
            }
        }
        return false;
    }

    public List<Reservation> getReservationsByUser(String username) {
        List<Reservation> userReservations = new ArrayList<>();
        for (Reservation reservation : reservations) {
            if (reservation.user.userName.equals(username)) {
                userReservations.add(reservation);
            }
        }
        return userReservations;
    }

    public boolean isSpaceAvailable(String spaceId, LocalDateTime start, LocalDateTime end) {
        for (Reservation reservation : reservations) {
            if (reservation.space.getSpaceId().equals(spaceId)) {
                if (!(reservation.endTime.isBefore(start) || reservation.startTime.isAfter(end))) {
                    return false;
                }
            }
        }
        return true;
    }
}

