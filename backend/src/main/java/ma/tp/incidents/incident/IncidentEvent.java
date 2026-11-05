package ma.tp.incidents.incident;
import jakarta.persistence.*;
import java.time.Instant;
@Entity
public class IncidentEvent {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) private Incident incident;
 @Column(nullable=false) private String actor;
 @Column(nullable=false,length=3000) private String message;
 @Column(nullable=false) private String kind;
 @Column(nullable=false) private Instant createdAt=Instant.now();
 protected IncidentEvent(){}
 public IncidentEvent(Incident i,String actor,String message,String kind){this.incident=i;this.actor=actor;this.message=message;this.kind=kind;}
 public Long getId(){return id;} public String getActor(){return actor;} public String getMessage(){return message;} public String getKind(){return kind;} public Instant getCreatedAt(){return createdAt;}
}
