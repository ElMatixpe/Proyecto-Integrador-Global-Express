package Minimarket.GlobalExpress_SpringBoot.security;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/** Crea y guarda los tokens en memoria (duran 8 horas)
 * Guarda en memoria las sesiones iniciadas: token -> usuario.
 * Los tokens son aleatorios, duran 8 horas y se pierden si se reinicia el backend
 * en ese caso hay que volver a iniciar sesión*/
@Service
public class SesionService {

    private static final Duration DURACION = Duration.ofHours(8);

    private record Sesion(Integer idUsuario, Instant expira) {
    }

    private final SecureRandom aleatorio = new SecureRandom();
    private final Map<String, Sesion> sesiones = new ConcurrentHashMap<>();

    public String crear(Integer idUsuario) {
        byte[] bytes = new byte[32];
        aleatorio.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sesiones.put(token, new Sesion(idUsuario, Instant.now().plus(DURACION)));
        return token;
    }

    public Optional<Integer> buscarIdUsuario(String token) {
        Sesion sesion = sesiones.get(token);
        if (sesion == null) {
            return Optional.empty();
        }
        if (Instant.now().isAfter(sesion.expira())) {
            sesiones.remove(token);
            return Optional.empty();
        }
        return Optional.of(sesion.idUsuario());
    }

    public void cerrar(String token) {
        sesiones.remove(token);
    }
}
