package Models;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
@Entity
@Table(name = "reservation") 
public class Reservation {
    @Id
    @Column(name = "reservation_id")
    private String reservationId;

    @ManyToOne
    @JoinColumn(name = "user_name", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "space_id", nullable = false)
    private Space space;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    public Reservation() {
    }

    public Reservation(User user, String reservationId, Space space, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        this.user = user;
        this.reservationId = reservationId;
        this.space = space;
        this.startTime= startDateTime;
        this.endTime = endDateTime;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public Space getSpace() {
        return space;
    }

    public void setSpace(Space space) {
        this.space = space;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
}
