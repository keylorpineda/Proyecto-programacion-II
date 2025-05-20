package Models;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "report")
public class Report {

    @Id
    @Column(name = "report_id", length = 50)
    private String reportId;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "date_created", nullable = false)
    private LocalDateTime dateCreated;

    public Report() {
    }

    public Report(String reportId, String description, LocalDateTime dateCreated) {
        this.reportId = reportId;
        this.description = description;
        this.dateCreated = dateCreated;
    }

    public String getReportId() {
        return reportId;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }
}
