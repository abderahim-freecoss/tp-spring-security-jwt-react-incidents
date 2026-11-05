package ma.tp.incidents.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import java.util.Map;

@RestControllerAdvice
public class ApiErrors {
    private ResponseEntity<?> error(int status, String message) {
        return ResponseEntity.status(status).body(Map.of("status", status, "message", message));
    }
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<?> status(ResponseStatusException e) { return error(e.getStatusCode().value(), e.getReason()); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> validation(MethodArgumentNotValidException e) {
        return error(400, e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + " : " + f.getDefaultMessage()).sorted().collect(java.util.stream.Collectors.joining(" ; ")));
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<?> unreadable() { return error(400, "Données invalides"); }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<?> duplicate() { return error(409, "Données déjà utilisées ou incompatibles"); }
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<?> denied() { return error(403, "Accès interdit"); }
}
