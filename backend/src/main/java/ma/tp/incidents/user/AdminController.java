package ma.tp.incidents.user;
import ma.tp.incidents.incident.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@RestController @RequestMapping("/api/admin") @Transactional
public class AdminController {
 private final UserRepository users; private final IncidentRepository incidents;
 public AdminController(UserRepository u,IncidentRepository i){users=u;incidents=i;}
 @GetMapping("/users") public List<UserView> users(){return users.findAll().stream().map(UserView::from).toList();}
 private UserView change(Long id,boolean enabled){User u=users.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Utilisateur introuvable"));u.setEnabled(enabled);if(!enabled)u.invalidateTokens();return UserView.from(users.save(u));}
 @PutMapping("/users/{id}/enable") public UserView enable(@PathVariable Long id){return change(id,true);}
 @PutMapping("/users/{id}/disable") public UserView disable(@PathVariable Long id){return change(id,false);}
 @GetMapping("/statistics") public Map<String,Long> statistics(){var us=users.findAll();var is=incidents.findAll();Map<String,Long> m=new LinkedHashMap<>();m.put("users",(long)us.size());for(Role r:Role.values())m.put(r.name(),us.stream().filter(u->u.getRole()==r).count());m.put("incidents",(long)is.size());for(Incident.Status s:Incident.Status.values())m.put(s.name(),is.stream().filter(i->i.getStatus()==s).count());return m;}
}