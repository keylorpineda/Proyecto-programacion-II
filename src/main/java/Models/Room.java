package Models;
import java.util.List;
public class Room {

    protected String roomName;
    protected int roomCapacity;
    protected boolean isReserved;
    protected String idRoom;
    protected String roomType;
    protected List<Space> spaceList;

    public Room(String roomName, int roomCapacity, boolean isReserved, String idRoom, String roomType, List<Space> spaceList) {
        this.roomName = roomName;
        this.roomCapacity = roomCapacity;
        this.isReserved = isReserved;
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
}
