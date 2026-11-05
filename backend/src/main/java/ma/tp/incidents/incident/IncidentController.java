package ma.tp.incidents.incident;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import ma.tp.incidents.user.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.security.Principal;
import java.time.Instant;
import java.util.*;
@RestController @RequestMapping("/api/incidents") @Transactional
public class IncidentController {
 private final IncidentRepository incidents; private final IncidentEventRepository events; private final UserRepository users;
 public IncidentController(IncidentRepository i,IncidentEventRepository e,UserRepository u){incidents=i;events=e;users=u;}
 record Create(@NotBlank @Size(max=160) String title,@NotBlank @Size(max=5000) String description,@NotNull Incident.Category category,@NotNull Incident.Priority priority){}
 record StatusChange(@NotNull Incident.Status status){}
 record Note(@NotBlank @Size(max=3000) String message){}
 record EventView(Long id,String actor,String message,String kind,Instant createdAt){}
 record View(Long id,String title,String description,Incident.Category category,Incident.Priority priority,Incident.Status status,Instant createdAt,Instant updatedAt,UserView createdBy,UserView assignedTo,List<EventView> history){}
 private User current(Principal p){return users.findByEmail(p.getName()).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED));}
 private Incident get(Long id){return incidents.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Incident introuvable"));}
 private void allow(Incident i,User u){if(u.getRole()!=Role.ADMIN && !(u.getRole()==Role.EMPLOYEE && i.getCreatedBy().getId().equals(u.getId())) && !(u.getRole()==Role.TECHNICIAN && i.getAssignedTo()!=null && i.getAssignedTo().getId().equals(u.getId()))) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Accès interdit");}
 private View view(Incident i,boolean details){var h=details?events.findByIncidentIdOrderByCreatedAtAsc(i.getId()).stream().map(e->new EventView(e.getId(),e.getActor(),e.getMessage(),e.getKind(),e.getCreatedAt())).toList():List.<EventView>of();return new View(i.getId(),i.getTitle(),i.getDescription(),i.getCategory(),i.getPriority(),i.getStatus(),i.getCreatedAt(),i.getUpdatedAt(),UserView.from(i.getCreatedBy()),i.getAssignedTo()==null?null:UserView.from(i.getAssignedTo()),h);}
 private void event(Incident i,User u,String msg,String kind){events.save(new IncidentEvent(i,u.getFullName(),msg,kind));}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasRole('EMPLOYEE')") public View create(@Valid @RequestBody Create x,Principal p){User u=current(p);Incident i=incidents.saveAndFlush(new Incident(x.title().strip(),x.description().strip(),x.category(),x.priority(),u));event(i,u,"Incident créé","HISTORY");return view(i,true);}
 @GetMapping("/my") @PreAuthorize("hasRole('EMPLOYEE')") public List<View> mine(Principal p){return incidents.findByCreatedByIdOrderByCreatedAtDesc(current(p).getId()).stream().map(i->view(i,false)).toList();}
 @GetMapping("/assigned") @PreAuthorize("hasRole('TECHNICIAN')") public List<View> assigned(Principal p){return incidents.findByAssignedToIdOrderByCreatedAtDesc(current(p).getId()).stream().map(i->view(i,false)).toList();}
 @GetMapping @PreAuthorize("hasRole('ADMIN')") public List<View> all(){return incidents.findAll().stream().map(i->view(i,false)).toList();}
 @GetMapping("/{id}") public View detail(@PathVariable Long id,Principal p){Incident i=get(id);allow(i,current(p));return view(i,true);}
 @PutMapping("/{id}/assign/{technicianId}") @PreAuthorize("hasRole('ADMIN')") public View assign(@PathVariable Long id,@PathVariable Long technicianId,Principal p){Incident i=get(id);if(i.getStatus()==Incident.Status.CLOSED)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Incident clôturé");User tech=users.findById(technicianId).filter(u->u.getRole()==Role.TECHNICIAN && u.isEnabled()).orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST,"Technicien actif introuvable"));i.assign(tech);incidents.saveAndFlush(i);event(i,current(p),"Affecté à "+tech.getFullName(),"HISTORY");return view(i,true);}
 @PutMapping("/{id}/status") @PreAuthorize("hasAnyRole('ADMIN','TECHNICIAN')") public View status(@PathVariable Long id,@Valid @RequestBody StatusChange x,Principal p){Incident i=get(id);User u=current(p);allow(i,u);if(i.getStatus()==Incident.Status.CLOSED)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Incident clôturé");if(x.status()==Incident.Status.ASSIGNED && i.getAssignedTo()==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Aucun technicien affecté");var old=i.getStatus();i.setStatus(x.status());incidents.saveAndFlush(i);event(i,u,old+" → "+x.status(),"HISTORY");return view(i,true);}
 @PostMapping("/{id}/comments") @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasRole('TECHNICIAN')") public View note(@PathVariable Long id,@Valid @RequestBody Note x,Principal p){Incident i=get(id);User u=current(p);allow(i,u);if(i.getStatus()==Incident.Status.CLOSED)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Incident clôturé");event(i,u,x.message().strip(),"COMMENT");return view(i,true);}
}
