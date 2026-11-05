package ma.tp.incidents.user;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "app_users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String fullName;
    @Column(nullable = false, unique = true, length = 254)
    private String email;
    @Column(nullable = false)
    private String password;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Role role = Role.EMPLOYEE;
    @Column(nullable = false)
    private boolean enabled = true;
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    @Column(nullable = false)
    private long tokenVersion;

    protected User() {}
    public User(String fullName, String email, String password) {
        this.fullName = fullName; this.email = email; this.password = password;
    }
    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
    public boolean isEnabled() { return enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public long getTokenVersion() { return tokenVersion; }
    public void setFullName(String value) { fullName = value; }
    public void setEmail(String value) { email = value; }
    public void setPassword(String value) { password = value; }
    public void setRole(Role value) { role = value; }
    public void setEnabled(boolean value) { enabled = value; }
    public void invalidateTokens() { tokenVersion++; }
}
