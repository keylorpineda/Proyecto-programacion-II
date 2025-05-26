
package Models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="description", nullable=false)
    private String description;

    @Column(nullable = false)
    private LocalDateTime dateCreated;

    public Report() { }

    public Report(String description, LocalDateTime dateCreated) {
        this.description = description;
        this.dateCreated = dateCreated;
    }

    public Long getId() { return id; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getDateCreated() { return dateCreated; }
    public void setDateCreated(LocalDateTime dateCreated) { this.dateCreated = dateCreated; }

    @Transient
    public LocalDateTime getDate() { return dateCreated; }
}
