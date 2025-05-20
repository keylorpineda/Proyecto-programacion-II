package Services;

import Models.Space;
import Models.User;
import java.util.List;

public class ReportService {

    private static ReportService instance;

    private ReportService() {}

    public static ReportService getInstance() {
        if (instance == null) {
            instance = new ReportService();
        }
        return instance;
    }

    /*public List<Space> mostReservedSpaces() {
        return 
    }

    public List<User> mostActiveUsers() {
       
    }

    public List<String> peakHours() {
       
    }

    public void generateReport() {
        
    }*/
}