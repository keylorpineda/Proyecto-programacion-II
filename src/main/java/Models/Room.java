package Models;
import java.util.List;
import jakarta.persistence.*;
import jakarta.persistence.Table;
@Entity
@Table(name = "room")
public class Room {

    @Id
    @Column(name = "room_id")
    private String idRoom;

    @Column(name = "room_name", nullable = false)
    private String roomName;

    @Column(name = "capacity")
    private int roomCapacity;

    @Column(name = "room_type", length = 50)
    private String roomType;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private List<Space> spaceList;

    public Room() {
    }
    public Room(String roomName, int roomCapacity, String idRoom, String roomType, List<Space> spaceList) {
        this.roomName = roomName;
        this.roomCapacity = roomCapacity;
        this.idRoom = idRoom;
        this.roomType = roomType;
        this.spaceList = spaceList;
    }
    public void addSpace(Space space) {
        spaceList.add(space);
    }
    public void eliminatedSpace(Space space) {
        spaceList.remove(space);
    }
    public Space getSpecificSpace(int index) {
        if (index >= 0 && index < spaceList.size()) {
            return spaceList.get(index);
        }
        return null;
    }

    public List<Space> getAvailableSpaces(){
        return spaceList;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public int getRoomCapacity() {
        return roomCapacity;
    }

    public void setRoomCapacity(int roomCapacity) {
        this.roomCapacity = roomCapacity;
    }

    public String getIdRoom() {
        return idRoom;
    }

    public void setIdRoom(String idRoom) {
        this.idRoom = idRoom;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public List<Space> getSpaceList() {
        return spaceList;
    }

    public void setSpaceList(List<Space> spaceList) {
        this.spaceList = spaceList;
    }
    
}
