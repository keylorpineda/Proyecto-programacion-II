package Models;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int floor;

    @Column(name = "rowsNumber", nullable = false)
    private int rows;

    @Column(nullable = false)
    private int cols;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Space> spaces = new ArrayList<>();

    public Room() {
    }

    public Room(String name, int floor, int rows, int cols) {
        this.name = name;
        this.floor = floor;
        this.rows = rows;
        this.cols = cols;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getCols() {
        return cols;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public List<Space> getSpaces() {
        return spaces;
    }

    public void setSpaces(List<Space> spaces) {
        this.spaces = spaces;
    }

    public List<Space> getAvailableSpaces() {
        return spaces.stream()
                .filter(Space::getAvailable)
                .collect(Collectors.toList());
    }

    @Transient
    public String getRoomName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
