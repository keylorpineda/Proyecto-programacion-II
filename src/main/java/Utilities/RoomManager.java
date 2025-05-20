package Utilities;

import Models.Room;
import Models.Space;
import Services.SpaceManager;
import Utilities.DataBaseManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class RoomManager {

    private static RoomManager instance;
    private final SpaceManager spaceManager;

    private RoomManager() {
        this.spaceManager = SpaceManager.getInstance();
    }

    public static RoomManager getInstance() {
        if (instance == null) {
            instance = new RoomManager();
        }
        return instance;
    }

    public List<Room> getAllRooms() {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            TypedQuery<Room> q = em.createQuery("SELECT r FROM Room r", Room.class);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public boolean addRoom(Room room) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            if (em.find(Room.class, room.getIdRoom()) != null) {
                return false;
            }
            em.getTransaction().begin();
            em.persist(room);
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    public boolean updateRoom(Room room) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(room);
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    public boolean deleteRoom(String roomId) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            Room r = em.find(Room.class, roomId);
            if (r == null) return false;
            em.getTransaction().begin();
            em.remove(r);
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
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
            int r = i / cols, c = i % cols;
            matrix[r][c] = spaces.get(i);
        }
        return matrix;
    }

    public boolean reserveSpaceAt(Space[][] matrix, int row, int col) {
        if (matrix == null 
         || row < 0 || row >= matrix.length 
         || col < 0 || col >= matrix[0].length) {
            return false;
        }
        Space s = matrix[row][col];
        if (s == null || s.getReserved()) {
            return false;
        }
        boolean ok = spaceManager.reserveSpace(s.getSpaceId());
        if (ok) s.reserve();
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
        return spaceManager.flattenMatrix(matrix);
    }
}