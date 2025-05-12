package Models;
import jakarta.persistence.*;
@Entity
@DiscriminatorValue("chair")
public class Chair extends Space{

    public Chair() {
        super();
    }
    
    public Chair(String spaceId, String spaceName, int spaceCapacity, boolean isReserved) {
        super();
    }
}
