package Models;
import jakarta.persistence.*;
@Entity
@DiscriminatorValue("table")
public class Table extends Space {

    public Table() {
        super(); 
    }
    
    public Table(String spaceId, String spaceName, int spaceCapacity, boolean isReserved) {
        super();
    }
}
