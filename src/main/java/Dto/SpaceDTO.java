package Dto;
import Models.SpaceType;

public class SpaceDTO {
    private Long id;
    private String name;
    private SpaceType type;
    private int startRow, startCol, width, height;
    private boolean available;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public SpaceType getType() {
        return type;
    }

    public int getStartRow() {
        return startRow;
    }

    public int getStartCol() {
        return startCol;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(SpaceType type) {
        this.type = type;
    }

    public void setStartRow(int startRow) {
        this.startRow = startRow;
    }

    public void setStartCol(int startCol) {
        this.startCol = startCol;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
   
}
