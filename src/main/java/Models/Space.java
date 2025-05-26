
package Models;

import jakarta.persistence.*;

@Entity
@Table(name = "spaces")
public class Space {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String name;

    @Column(nullable = false)
    private int startRow;

    @Column(nullable = false)
    private int startCol;

    @Column(nullable = false)
    private int width;

    @Column(nullable = false)
    private int height;

    private int capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SpaceType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private Boolean available = true;

    public Space() { }

    public Space(String name, int startRow, int startCol, int width, int height, int capacity, SpaceType type, Room room) {
        this.name = name;
        this.startRow = startRow;
        this.startCol = startCol;
        this.width = width;
        this.height = height;
        this.capacity = capacity;
        this.type = type;
        this.room = room;
        this.available = true;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getStartRow() { return startRow; }
    public void setStartRow(int startRow) { this.startRow = startRow; }
    public int getStartCol() { return startCol; }
    public void setStartCol(int startCol) { this.startCol = startCol; }
    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }
    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public SpaceType getType() { return type; }
    public void setType(SpaceType type) { this.type = type; }
    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }
    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }

    @Transient
    public String getSpaceName() { return name; }
}
