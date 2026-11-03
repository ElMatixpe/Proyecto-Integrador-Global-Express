package Minimarket.GlobalExpress_SpringBoot.exception;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/* Convierte los errores en respuestas JSON que el React puede mostrar. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> noEncontrado(RecursoNoEncontradoException e) {
        return responder(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<Map<String, String>> credenciales(CredencialesInvalidasException e) {
        return responder(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> datosInvalidos(IllegalArgumentException e) {
        return responder(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    // Falla alguna anotación de validación (@NotBlank, @Pattern, @DecimalMin...) de un DTO con @Valid.
    // Se devuelven los mensajes de las reglas incumplidas, que el React muestra tal cual.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validacion(MethodArgumentNotValidException e) {
        String mensaje = e.getBindingResult().getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.joining(" "));
        return responder(HttpStatus.BAD_REQUEST, mensaje.isBlank() ? "Los datos enviados no son válidos." : mensaje);
    }

    // El JSON no se pudo convertir al tipo esperado: por ejemplo, texto ("abc") en el precio o en una cantidad
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> formatoInvalido(HttpMessageNotReadableException e) {
        return responder(HttpStatus.BAD_REQUEST,
                "Los datos enviados no tienen un formato válido. Revisa que el precio y las cantidades sean números.");
    }

    // Por ejemplo: borrar un producto o usuario que ya está en ventas (FK RESTRICT)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> integridad(DataIntegrityViolationException e) {
        return responder(HttpStatus.CONFLICT,
                "No se pudo completar la operación: el registro está relacionado con otros datos o está duplicado.");
    }

    private ResponseEntity<Map<String, String>> responder(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(Map.of("mensaje", mensaje));
    }
}
