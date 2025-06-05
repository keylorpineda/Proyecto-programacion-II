package Dto;

import java.util.List;

public class RoomLayoutDTO {
    private final SpaceDTO[][] matrix;
    public RoomLayoutDTO(List<SpaceDTO> spaces, int rows, int cols) {
        matrix = new SpaceDTO[rows][cols];
        for (SpaceDTO s: spaces) {
            for (int r = s.getStartRow(); r < s.getStartRow()+s.getHeight(); r++) {
                for (int c = s.getStartCol(); c < s.getStartCol()+s.getWidth(); c++) {
                    matrix[r][c] = s;
                }
            }
        }
    }
    public SpaceDTO getCell(int r, int c) { return matrix[r][c]; }
    public int getRows() { return matrix.length; }
    public int getCols() { return matrix[0].length; }
}
