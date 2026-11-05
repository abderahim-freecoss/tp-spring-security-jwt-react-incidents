package ma.tp.incidents.auth;

import ma.tp.incidents.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import java.time.*;

@Service
public class JwtService {
    private final JwtEncoder encoder;
    private final Duration ttl;
    public JwtService(JwtEncoder encoder, @Value("${app.jwt.ttl}") Duration ttl) { this.encoder = encoder; this.ttl = ttl; }
    public record Session(String token, String role, String fullName) {}
    public Session issue(User user) {
        Instant now = Instant.now();
        var claims = JwtClaimsSet.builder().issuer("incident-desk").subject(user.getEmail())
                .issuedAt(now).expiresAt(now.plus(ttl)).claim("role", user.getRole().name())
                .claim("version", user.getTokenVersion()).build();
        String token = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new Session(token, user.getRole().name(), user.getFullName());
    }
}
