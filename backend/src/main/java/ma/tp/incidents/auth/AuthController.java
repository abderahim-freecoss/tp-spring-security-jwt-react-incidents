package ma.tp.incidents.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import ma.tp.incidents.user.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.Locale;

@RestController @RequestMapping("/auth")
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder passwords;
    private final JwtService jwt;
    private final String dummyHash;
    public AuthController(UserRepository users, PasswordEncoder passwords, JwtService jwt) {
        this.users = users; this.passwords = passwords; this.jwt = jwt;
        this.dummyHash = passwords.encode("dummy-password-for-timing");
    }
    public record Register(@NotBlank @Size(max=100) String fullName, @NotBlank @Email @Size(max=254) String email,
                           @NotBlank @Size(min=8, max=72) String password) {}
    public record Login(@NotBlank @Email String email, @NotBlank @Size(max=72) String password) {}
    public static String normalize(String email) { return email.strip().toLowerCase(Locale.ROOT); }
    @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED)
    public UserView register(@Valid @RequestBody Register input) {
        String email = normalize(input.email());
        if (users.existsByEmail(email)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Email déjà utilisé");
        if (input.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mot de passe limité à 72 octets UTF-8");
        return UserView.from(users.save(new User(input.fullName().strip(), email, passwords.encode(input.password()))));
    }
    @PostMapping("/login")
    public JwtService.Session login(@Valid @RequestBody Login input) {
        var user = users.findByEmail(normalize(input.email()));
        boolean matches = passwords.matches(input.password(), user.map(User::getPassword).orElse(dummyHash));
        if (!matches || user.isEmpty() || !user.get().isEnabled())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants incorrects ou compte désactivé");
        return jwt.issue(user.get());
    }
}
