package Models;
import jakarta.persistence.*;
@Entity
@DiscriminatorValue("desk")
public class Desk extends Space {

    public Desk() {
        super();
    }
    
    public Desk(String spaceId, String spaceName, int spaceCapacity, boolean isReserved) {
        super();
    }
}
