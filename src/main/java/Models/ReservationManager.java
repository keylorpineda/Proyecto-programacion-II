package Models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationManager {
    private List<Reservation> reservations;

    public boolean createReservation(Reservation reservation){
        if(isSpaceAvailable(String.valueOf(reservation.space.spaceId), reservation.startDate, reservation.endDate)){
            reservations.add(reservation);
            reservation.space.reserve();
            return true;
        }
        return false;
    }

    public boolean cancelReservation(String reservationId){
        for(Reservation reservation : reservations){
            if(reservation.reservationId.equals(reservationId)){
                reservation.space.unReserve();
                reservations.remove(reservation);
                return true;
            }
        }
        return false;
    }

    public List<Reservation> getReservationsByUser(String username){
        List<Reservation> userReservations = new ArrayList<>();

        for (Reservation reservation : reservations) {
            if (reservation.user.userName.equals(username)) {
                userReservations.add(reservation);
            }
        }

        return userReservations;
    }

    public boolean isSpaceAvailable(String spaceId, LocalDateTime start, LocalDateTime end){
        for(Reservation reservation : reservations){
            if(reservation.space.getSpaceId().equals(spaceId)){
                if(!(reservation.endDate.isBefore(start) || reservation.startDate.isAfter(end))){
                    return false;
                }
            }
        }
        return true;
    }
}
