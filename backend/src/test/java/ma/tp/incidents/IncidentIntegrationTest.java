package ma.tp.incidents;
import ma.tp.incidents.auth.JwtService;
import ma.tp.incidents.user.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:incidenttest;DB_CLOSE_DELAY=-1","spring.jpa.hibernate.ddl-auto=create-drop"}) @AutoConfigureMockMvc
class IncidentIntegrationTest {
 @Autowired MockMvc mvc; @Autowired UserRepository users; @Autowired JwtService jwt; @Autowired PasswordEncoder passwords; @Autowired ObjectMapper json;
 User user(String email,Role role){User u=new User(email,email,passwords.encode("Password123!"));u.setRole(role);return users.saveAndFlush(u);}
 String token(User u){return "Bearer "+jwt.issue(u).token();}
 @Test void scenarioAndOwnership() throws Exception {
  User a=user("a@example.com",Role.EMPLOYEE),b=user("b@example.com",Role.EMPLOYEE),t=user("t@example.com",Role.TECHNICIAN),other=user("t2@example.com",Role.TECHNICIAN),admin=user("admin@example.com",Role.ADMIN);
  var result=mvc.perform(post("/api/incidents").header("Authorization",token(a)).contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"Panne\",\"description\":\"Réseau\",\"category\":\"NETWORK\",\"priority\":\"HIGH\"}")).andExpect(status().isCreated()).andReturn();
  long id=json.readTree(result.getResponse().getContentAsString()).get("id").asLong();
  mvc.perform(get("/api/incidents/"+id).header("Authorization",token(b))).andExpect(status().isForbidden());
  mvc.perform(put("/api/incidents/"+id+"/assign/"+t.getId()).header("Authorization",token(a))).andExpect(status().isForbidden());
  mvc.perform(put("/api/incidents/"+id+"/assign/"+t.getId()).header("Authorization",token(admin))).andExpect(status().isOk()).andExpect(jsonPath("status").value("ASSIGNED"));
  mvc.perform(put("/api/incidents/"+id+"/status").header("Authorization",token(other)).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"IN_PROGRESS\"}")).andExpect(status().isForbidden());
  mvc.perform(put("/api/incidents/"+id+"/status").header("Authorization",token(t)).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"IN_PROGRESS\"}")).andExpect(status().isOk());
  mvc.perform(post("/api/incidents/"+id+"/comments").header("Authorization",token(t)).contentType(MediaType.APPLICATION_JSON).content("{\"message\":\"Vérification effectuée\"}")).andExpect(status().isCreated());
  mvc.perform(get("/api/incidents/"+id).header("Authorization",token(a))).andExpect(status().isOk()).andExpect(jsonPath("history.length()").value(4));
  mvc.perform(put("/api/incidents/"+id+"/status").header("Authorization",token(admin)).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CLOSED\"}")).andExpect(status().isOk());
  mvc.perform(put("/api/incidents/"+id+"/status").header("Authorization",token(admin)).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"OPEN\"}")).andExpect(status().isBadRequest());
  mvc.perform(post("/api/incidents").header("Authorization",token(a)).contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"\"}")).andExpect(status().isBadRequest());
 }
}