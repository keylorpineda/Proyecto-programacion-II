package Models;

import java.time.LocalDateTime;

public class Customer extends User {

    protected ReservationManager reservationManager;

    public Customer(String userName, String name, String lastName, String identification, String password, String userRol) {
        super(userName, name, lastName, identification, password, userRol);
        reservationManager = new ReservationManager();
    }

    public boolean makeReservation(String reservationId, Space space, LocalDateTime start, LocalDateTime end) {
        if (reservationManager.isSpaceAvailable(space.spaceId, start, end)) {
            Reservation reservation = new Reservation(this, reservationId, space, start, end);
            return reservationManager.createReservation(reservation);
        }
        return false;
    }

    public boolean cancelReservation(String reservationId) {
        return reservationManager.cancelReservation(reservationId);
    }

    public void viewReservations(){}
}
