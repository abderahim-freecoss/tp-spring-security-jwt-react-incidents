package ma.tp.incidents.incident;

import jakarta.persistence.*;
import ma.tp.incidents.user.User;
import java.time.Instant;


@Entity
public class Incident {
    public enum Category { NETWORK, MALWARE, PHISHING, ACCESS, SOFTWARE, HARDWARE, DATA_LEAK, OTHER }
    public enum Priority { LOW, MEDIUM, HIGH, CRITICAL }
    public enum Status { OPEN, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED }
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, length=160) private String title;
    @Column(nullable=false, length=5000) private String description;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private Category category;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private Priority priority;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private Status status = Status.OPEN;
    @Column(nullable=false, updatable=false) private Instant createdAt;
    @Column(nullable=false) private Instant updatedAt;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="created_by", nullable=false) private User createdBy;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="assigned_to") private User assignedTo;
    @Version private long version;
    protected Incident() {}
    public Incident(String title, String description, Category category, Priority priority, User creator) { this.title=title; this.description=description; this.category=category; this.priority=priority; this.createdBy=creator; }
    public Long getId(){return id;} public String getTitle(){return title;} public String getDescription(){return description;} public Category getCategory(){return category;} public Priority getPriority(){return priority;} public Status getStatus(){return status;} public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;} public User getCreatedBy(){return createdBy;} public User getAssignedTo(){return assignedTo;}
    public void assign(User user){assignedTo=user; status=Status.ASSIGNED;} public void setStatus(Status value){status=value;}
    @PrePersist void create() { createdAt = updatedAt = Instant.now(); }
    @PreUpdate void update() { updatedAt = Instant.now(); }
}
