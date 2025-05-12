package Models;

import jakarta.persistence.*;
import Services.ReservationManager;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.time.LocalDateTime;

@Entity
@DiscriminatorValue("customer")
public class Customer extends User {

    public Customer() {
        super();
    }

    public Customer(String userName, String name, String lastName, String identification, String password) {
        super(userName, name, lastName, identification, password);
    }

    public boolean makeReservation(String reservationId, Space space, LocalDateTime start, LocalDateTime end) {
        ReservationManager reservationManager = ReservationManager.getInstance();
        if (reservationManager.isSpaceAvailable(space.getSpaceId(), start, end)) {
            Reservation reservation = new Reservation(this, reservationId, space, start, end);
            return reservationManager.createReservation(reservation);
        }
        return false;
    }

    public boolean cancelReservation(String reservationId) {
        return ReservationManager.getInstance().cancelReservation(reservationId);
    }

    public void viewReservations() {
    }
}