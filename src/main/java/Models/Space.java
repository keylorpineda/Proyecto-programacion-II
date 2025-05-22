package Models;

import jakarta.persistence.*;

@Entity
@Table(name = "spaces")
public class Space {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int startRow;
    private int startCol;
    private int width;
    private int height;
    private int capacity;

    @Enumerated(EnumType.STRING)
    private SpaceType type;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    public Space() { }

    public Space(int startRow, int startCol, int width, int height, int capacity, SpaceType type, Room room) {
        this.startRow = startRow;
        this.startCol = startCol;
        this.width    = width;
        this.height   = height;
        this.capacity = capacity;
        this.type     = type;
        this.room     = room;
    }

    
    public Long getId() { return id; }
    public int getStartRow() { return startRow; }
    public void setStartRow(int r) { this.startRow = r; }
    public int getStartCol() { return startCol; }
    public void setStartCol(int c) { this.startCol = c; }
    public int getWidth() { return width; }
    public void setWidth(int w) { this.width = w; }
    public int getHeight() { return height; }
    public void setHeight(int h) { this.height = h; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int c) { this.capacity = c; }
    public SpaceType getType() { return type; }
    public void setType(SpaceType t) { this.type = t; }
    public Room getRoom() { return room; }
    public void setRoom(Room r) { this.room = r; }
}
