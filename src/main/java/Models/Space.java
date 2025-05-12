package Models;
import jakarta.persistence.*;
import jakarta.persistence.Table;

@Entity
@Table(name = "space")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
public abstract class Space {

    @Id
    @Column(name = "space_id")
    private String spaceId;

    @Column(name = "space_name", nullable = false)
    private String spaceName;

    @Column(name = "capacity")
    private int spaceCapacity;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @Column(name = "reserved")
    private boolean isReserved;

    public Space() {
    }

    public Space(String spaceId, String spaceName, int spaceCapacity, Room room, boolean reserved) {
        this.spaceId = spaceId;
        this.spaceName = spaceName;
        this.spaceCapacity = spaceCapacity;
        this.room = room;
        this.isReserved = reserved;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    public String getSpaceName() {
        return spaceName;
    }

    public void setSpaceName(String spaceName) {
        this.spaceName = spaceName;
    }

    public int getSpaceCapacity() {
        return spaceCapacity;
    }

    public void setSpaceCapacity(int spaceCapacity) {
        this.spaceCapacity = spaceCapacity;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public boolean getReserved() {
        return isReserved;
    }

    public void setReserved(boolean reserved) {
        this.isReserved = reserved;
    }

    public void reserve() {
        this.isReserved = true;
    }

    public void unreserved() {
        this.isReserved = false; 
    }
    
}
