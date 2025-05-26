package Dto;

public class RoomTableDTO {
    private Long id;
    private String roomName;
    private int floor, rows, cols;

    public Long getId() {
        return id;
    }

    public String getRoomName() {
        return roomName;
    }

    public int getFloor() {
        return floor;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }
    
}

