package ma.tp.incidents.auth;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import ma.tp.incidents.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.*;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.List;

@Configuration @EnableMethodSecurity
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean SecretKey jwtKey(@Value("${app.jwt.secret}") String secret) {
        byte[] bytes;
        if (secret.isBlank()) { bytes = new byte[32]; new SecureRandom().nextBytes(bytes); }
        else { bytes = secret.getBytes(StandardCharsets.UTF_8); }
        if (bytes.length < 32) throw new IllegalArgumentException("JWT_SECRET doit contenir au moins 32 octets");
        return new SecretKeySpec(bytes, "HmacSHA256");
    }
    @Bean JwtEncoder jwtEncoder(SecretKey key) { return new NimbusJwtEncoder(new ImmutableSecret<>(key)); }
    @Bean JwtDecoder jwtDecoder(SecretKey key) {
        var decoder = NimbusJwtDecoder.withSecretKey(key).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(new JwtTimestampValidator(Duration.ZERO), new JwtIssuerValidator("incident-desk")));
        return decoder;
    }
    @Bean SecurityFilterChain security(HttpSecurity http, UserRepository users,
            @Value("${spring.h2.console.enabled:false}") boolean console) throws Exception {
        http.csrf(csrf -> csrf.disable()).cors(cors -> {})
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> {
                a.requestMatchers("/auth/register", "/auth/login").permitAll();
                if (console) a.requestMatchers("/h2-console/**").permitAll();
                a.requestMatchers("/api/admin/**").hasRole("ADMIN").requestMatchers("/api/**").authenticated().anyRequest().denyAll();
            })
            .exceptionHandling(e -> e.authenticationEntryPoint((req, res, ex) -> {
                res.setStatus(401); res.setContentType("application/json;charset=UTF-8");
                res.getWriter().write("{\"status\":401,\"message\":\"Authentification requise ou session expirée\"}");
            }).accessDeniedHandler((req, res, ex) -> {
                res.setStatus(403); res.setContentType("application/json;charset=UTF-8");
                res.getWriter().write("{\"status\":403,\"message\":\"Accès interdit\"}");
            }))
            .oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(jwt -> {
                var user = users.findByEmail(jwt.getSubject()).filter(u -> u.isEnabled()
                        && jwt.getClaim("version") instanceof Number n && n.longValue() == u.getTokenVersion())
                        .orElseThrow(() -> new OAuth2AuthenticationException("invalid_token"));
                return new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())), user.getEmail());
            })).authenticationEntryPoint((req, res, ex) -> {
                res.setStatus(401); res.setContentType("application/json;charset=UTF-8");
                res.getWriter().write("{\"status\":401,\"message\":\"JWT invalide ou expiré\"}");
            }));
        if (console) http.headers(h -> h.frameOptions(f -> f.sameOrigin()));
        return http.build();
    }
    @Bean CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.origin}") String origin) {
        var config = new CorsConfiguration();
        // Vite is opened as localhost on some machines and 127.0.0.1 on others.
        config.setAllowedOrigins(List.of(origin, "http://localhost:5173", "http://127.0.0.1:5173").stream().distinct().toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        var source = new UrlBasedCorsConfigurationSource(); source.registerCorsConfiguration("/**", config); return source;
    }
}
