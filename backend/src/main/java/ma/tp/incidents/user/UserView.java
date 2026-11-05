package ma.tp.incidents.user;
import java.time.Instant;
public record UserView(Long id, String fullName, String email, Role role, boolean enabled, Instant createdAt) {
    public static UserView from(User user) {
        return new UserView(user.getId(), user.getFullName(), user.getEmail(), user.getRole(), user.isEnabled(), user.getCreatedAt());
    }
}
