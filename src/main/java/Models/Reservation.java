package Models;

import java.time.LocalDateTime;

public class Reservation {
    protected User user;
    protected String reservationId;
    protected Space space;
    protected LocalDateTime startTime;
    protected LocalDateTime endTime;

    public Reservation(User user, String reservationId, Space space, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        this.user = user;
        this.reservationId = reservationId;
        this.space = space;
        this.startTime= startDateTime;
        this.endTime = endDateTime;
    }
}
