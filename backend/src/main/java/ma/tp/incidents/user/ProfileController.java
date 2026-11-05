package ma.tp.incidents.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import ma.tp.incidents.auth.AuthController;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.security.Principal;

@RestController @RequestMapping("/api/profile")
public class ProfileController {
    private final UserRepository users;
    private final PasswordEncoder passwords;
    public ProfileController(UserRepository users, PasswordEncoder passwords) { this.users = users; this.passwords = passwords; }
    private User current(Principal principal) { return users.findByEmail(principal.getName()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session invalide")); }
    public record Profile(@NotBlank @Size(max=100) String fullName, @NotBlank @Email @Size(max=254) String email) {}
    public record Password(@NotBlank @Size(max=72) String oldPassword, @NotBlank @Size(min=8,max=72) String newPassword, @NotBlank String confirmation) {}
    @GetMapping public UserView get(Principal principal) { return UserView.from(current(principal)); }
    @PutMapping @Transactional
    public UserView update(Principal principal, @Valid @RequestBody Profile input) {
        var user = current(principal);
        String email = AuthController.normalize(input.email());
        if (!email.equals(user.getEmail())) {
            if (users.existsByEmail(email)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Email déjà utilisé");
            user.invalidateTokens(); user.setEmail(email);
        }
        user.setFullName(input.fullName().strip());
        return UserView.from(users.save(user));
    }
    @PutMapping("/password") @ResponseStatus(HttpStatus.NO_CONTENT) @Transactional
    public void password(Principal principal, @Valid @RequestBody Password input) {
        var user = current(principal);
        if (!passwords.matches(input.oldPassword(), user.getPassword())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ancien mot de passe incorrect");
        if (!input.newPassword().equals(input.confirmation())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La confirmation ne correspond pas");
        if (input.newPassword().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mot de passe limité à 72 octets UTF-8");
        user.setPassword(passwords.encode(input.newPassword())); user.invalidateTokens(); users.save(user);
    }
}
