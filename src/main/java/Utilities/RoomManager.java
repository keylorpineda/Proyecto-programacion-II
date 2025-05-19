package Services;

import Models.Space;
import java.util.ArrayList;
import java.util.List;

public class RoomManager {

    private static RoomManager instance;
    private SpaceManager spaceManager;

    private RoomManager() {
        this.spaceManager = SpaceManager.getInstance();
    }

    public static RoomManager getInstance() {
        if (instance == null) {
            instance = new RoomManager();
        }
        return instance;
    }

    public List<Space> getSpacesByRoom(String roomId) {
        return spaceManager.getSpacesByRoom(roomId);
    }

    public Space[][] buildMatrix(String roomId, int cols) {
        List<Space> spaces = getSpacesByRoom(roomId);
        int total = spaces.size();
        int rows  = (total + cols - 1) / cols;
        Space[][] matrix = new Space[rows][cols];
        for (int i = 0; i < total; i++) {
            int r = i / cols;
            int c = i % cols;
            matrix[r][c] = spaces.get(i);
        }
        return matrix;
    }

    public boolean reserveSpaceAt(Space[][] matrix, int row, int col) {
        if (matrix == null || row < 0 || row >= matrix.length || col < 0 || col >= matrix[0].length) {
            return false;
        }
        Space s = matrix[row][col];
        if (s == null || s.getReserved()) {
            return false;
        }
        boolean ok = spaceManager.reserveSpace(s.getSpaceId());
        if (ok) {
            s.reserve();
        }
        return ok;
    }

    public void reserveAll(Space[][] matrix) {
        if (matrix == null) return;
        for (int r = 0; r < matrix.length; r++) {
            for (int c = 0; c < matrix[r].length; c++) {
                reserveSpaceAt(matrix, r, c);
            }
        }
    }

    public List<Space> flattenMatrix(Space[][] matrix) {
        List<Space> list = new ArrayList<>();
        if (matrix == null) return list;
        for (int r = 0; r < matrix.length; r++) {
            for (int c = 0; c < matrix[r].length; c++) {
                Space s = matrix[r][c];
                if (s != null) {
                    list.add(s);
                }
            }
        }
        return list;
    }
}
