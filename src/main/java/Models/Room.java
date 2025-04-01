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

    public boolean isIsReserved() {
        return isReserved;
    }

    public void setIsReserved(boolean isReserved) {
        this.isReserved = isReserved;
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
