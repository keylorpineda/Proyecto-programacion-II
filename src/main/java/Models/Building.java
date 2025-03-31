package Models;

import java.util.List;

public class Building {
    protected String BuildingName;
    protected List<Room> roomList;
    public Building(String buildingName, List<Room> roomList) {
        BuildingName = buildingName;
        this.roomList = roomList;

    }
    public boolean addRoom(Room room) {
        for(Room r : roomList) {
            if(r.idRoom.equals(room.idRoom)) {
                return false;
            }
        }
        roomList.add(room);
        return true;
    }

    public boolean deleteRoom(String idRoom) {
        for (Room room : roomList) {
            if (room.idRoom.equals(idRoom)) {
                roomList.remove(room);
                return true;
            }
        }
        return false;
    }

    public void getRoom(){}

    public List<Room> listAllRooms(){
        return roomList;
    }
}
