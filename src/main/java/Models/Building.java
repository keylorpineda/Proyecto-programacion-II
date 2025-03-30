package Models;

import java.util.List;

public class Building {
    protected String BuildingName;
    protected List<Room> roomList;

    public Building(String buildingName, List<Room> roomList) {
        BuildingName = buildingName;
        this.roomList = roomList;

    }

    public void addRoom(){

    }

    public void deleteRoom(){}

    public void getRoom(){}
}
