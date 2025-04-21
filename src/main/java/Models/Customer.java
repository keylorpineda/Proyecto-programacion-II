package Models;
import jakarta.persistence.*;
import Services.ReservationManager;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.time.LocalDateTime;

@Entity
@DiscriminatorValue("customer")
public class Customer extends User {

    protected ReservationManager reservationManager;

    public Customer() {
        super();
    }

    public Customer(String userName, String name, String lastName, String identification, String password) {
        super(userName, name, lastName, identification, password);
        reservationManager = new ReservationManager();
    }

    public boolean makeReservation(String reservationId, Space space, LocalDateTime start, LocalDateTime end) {
        if (reservationManager.isSpaceAvailable(String.valueOf(space.getSpaceId()), start, end)) {
            Reservation reservation = new Reservation(this, reservationId, space, start, end);
            return reservationManager.createReservation(reservation);
        }
        return false;
    }

    public boolean cancelReservation(String reservationId) {
        return reservationManager.cancelReservation(reservationId);
    }

    public void viewReservations() {
    }
}
