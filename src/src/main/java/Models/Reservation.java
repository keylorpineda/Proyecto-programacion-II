
package Models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "space_id", nullable = false)
    private Space space;

    @Column(nullable = false)
    private LocalDateTime dateCreated;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    public Reservation() { }

    public Reservation(User user, Space space, LocalDateTime dateCreated, LocalDateTime startTime, LocalDateTime endTime) {
        this.user = user;
        this.space = space;
        this.dateCreated = dateCreated;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Space getSpace() { return space; }
    public void setSpace(Space space) { this.space = space; }
    public LocalDateTime getDateCreated() { return dateCreated; }
    public void setDateCreated(LocalDateTime dateCreated) { this.dateCreated = dateCreated; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public void setDate(LocalDate d) {
    this.dateCreated = d.atStartOfDay();
}

    @Transient
    public String getPlace() { return space.getRoom().getRoomName(); }
}
