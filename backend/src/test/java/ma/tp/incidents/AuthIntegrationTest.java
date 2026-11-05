package ma.tp.incidents;

import com.fasterxml.jackson.databind.ObjectMapper;
import ma.tp.incidents.user.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import java.time.Instant;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "spring.jpa.hibernate.ddl-auto=create-drop"})
@AutoConfigureMockMvc
class AuthIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder passwords;
    @Autowired JwtEncoder encoder;
    @BeforeEach void reset() { users.deleteAll(); }
    void register() throws Exception {
        mvc.perform(post("/auth/register").contentType("application/json").content(json.writeValueAsString(Map.of(
                "fullName", "Ahmed Benali", "email", "ahmed@example.com", "password", "Password123!", "role", "ADMIN"))))
                .andExpect(status().isCreated()).andExpect(jsonPath("role").value("EMPLOYEE")).andExpect(jsonPath("password").doesNotExist());
    }
    String login() throws Exception {
        var result = mvc.perform(post("/auth/login").contentType("application/json").content("{\"email\":\"ahmed@example.com\",\"password\":\"Password123!\"}"))
                .andExpect(status().isOk()).andReturn();
        return json.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }
    @Test void registrationForcesEmployeeAndHashesPassword() throws Exception {
        register(); var user = users.findByEmail("ahmed@example.com").orElseThrow();
        assertThat(passwords.matches("Password123!", user.getPassword())).isTrue();
        assertThat(user.getPassword()).startsWith("$2");
    }
    @Test void loginAndProtectedProfile() throws Exception {
        register(); String token = login();
        mvc.perform(get("/api/profile").header("Authorization", "Bearer " + token)).andExpect(status().isOk())
                .andExpect(jsonPath("email").value("ahmed@example.com")).andExpect(jsonPath("password").doesNotExist());
    }
    @Test void missingInvalidAndExpiredTokenAreRejected() throws Exception {
        mvc.perform(get("/api/profile")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/profile").header("Authorization", "Bearer invalid")).andExpect(status().isUnauthorized());
        register();
        var claims = JwtClaimsSet.builder().issuer("incident-desk").subject("ahmed@example.com").issuedAt(Instant.now().minusSeconds(600))
                .expiresAt(Instant.now().minusSeconds(300)).claim("role", "EMPLOYEE").claim("version", 0).build();
        String expired = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        mvc.perform(get("/api/profile").header("Authorization", "Bearer " + expired)).andExpect(status().isUnauthorized());
    }
    @Test void employeeCannotAccessAdmin() throws Exception {
        register(); mvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + login())).andExpect(status().isForbidden());
    }
    @Test void duplicateAndInvalidRegistration() throws Exception {
        register();
        mvc.perform(post("/auth/register").contentType("application/json").content("{\"fullName\":\"Other\",\"email\":\"AHMED@example.com\",\"password\":\"Password123!\"}"))
                .andExpect(status().isConflict());
        mvc.perform(post("/auth/register").contentType("application/json").content("{\"fullName\":\"\",\"email\":\"invalid\",\"password\":\"x\"}"))
                .andExpect(status().isBadRequest());
    }
    @Test void disabledAccountCannotLoginOrReuseToken() throws Exception {
        register(); String token = login(); var user = users.findByEmail("ahmed@example.com").orElseThrow();
        user.setEnabled(false); users.save(user);
        mvc.perform(post("/auth/login").contentType("application/json").content("{\"email\":\"ahmed@example.com\",\"password\":\"Password123!\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/profile").header("Authorization", "Bearer " + token)).andExpect(status().isUnauthorized());
    }
    @Test void passwordChangeChecksOldPasswordAndRevokesSession() throws Exception {
        register(); String token = login();
        mvc.perform(put("/api/profile/password").header("Authorization", "Bearer " + token).contentType("application/json")
                .content("{\"oldPassword\":\"wrong\",\"newPassword\":\"NewPassword123!\",\"confirmation\":\"NewPassword123!\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/profile/password").header("Authorization", "Bearer " + token).contentType("application/json")
                .content("{\"oldPassword\":\"Password123!\",\"newPassword\":\"NewPassword123!\",\"confirmation\":\"NewPassword123!\"}"))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/profile").header("Authorization", "Bearer " + token)).andExpect(status().isUnauthorized());
    }
    @Test void changingEmailInvalidatesOldToken() throws Exception {
        register(); String token = login();
        mvc.perform(put("/api/profile").header("Authorization", "Bearer " + token).contentType("application/json")
                .content("{\"fullName\":\"Ahmed\",\"email\":\"new@example.com\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("email").value("new@example.com"));
        mvc.perform(get("/api/profile").header("Authorization", "Bearer " + token)).andExpect(status().isUnauthorized());
    }
}
