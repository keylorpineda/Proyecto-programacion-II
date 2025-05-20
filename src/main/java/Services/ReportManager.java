package Services;

import Models.Report;
import Utilities.DataBaseManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class ReportManager {

    private static ReportManager instance;

    private ReportManager() {}

    public static ReportManager getInstance() {
        if (instance == null) {
            instance = new ReportManager();
        }
        return instance;
    }

    public List<Report> getAllReports() {
        EntityManager em = DataBaseManager.getEntityManager();
        try {
            TypedQuery<Report> q = em.createQuery(
                "SELECT r FROM Report r", Report.class);
            return q.getResultList();
        } finally {
            em.close();
        }
    }
}