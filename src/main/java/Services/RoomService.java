
package Services;

import Models.Room;
import Utilities.DataBaseManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class RoomService {

   
    public Room findById(Long id) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            return em.find(Room.class, id);
        } finally {
            em.close();
        }
    }

    
    public List<Room> findByFloor(int floor) {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            TypedQuery<Room> q = em.createQuery(
                "SELECT r FROM Room r WHERE r.floor = :f", Room.class);
            q.setParameter("f", floor);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    
    public List<Room> findAll() {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            TypedQuery<Room> q = em.createQuery(
                "SELECT r FROM Room r", Room.class);
            return q.getResultList();
        } finally {
            em.close();
        }
    }
}
