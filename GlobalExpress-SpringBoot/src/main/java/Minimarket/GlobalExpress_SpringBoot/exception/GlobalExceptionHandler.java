package Minimarket.GlobalExpress_SpringBoot.exception;

import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/* Convierte los errores en JSON {"mensaje": ...}, para que react lo pueda mostrar */
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

    // Por ejemplo: borrar un producto o usuario que ya esta en ventas (FK RESTRICT)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> integridad(DataIntegrityViolationException e) {
        return responder(HttpStatus.CONFLICT,
                "No se pudo completar la operación: el registro está relacionado con otros datos o está duplicado.");
    }

    private ResponseEntity<Map<String, String>> responder(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(Map.of("mensaje", mensaje));
    }
}
