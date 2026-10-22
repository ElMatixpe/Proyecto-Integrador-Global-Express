package Minimarket.GlobalExpress_SpringBoot.security;

import Minimarket.GlobalExpress_SpringBoot.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/** Lee el token en cada petición e identifica al usuario.
 * Lee el encabezado "Authorization: Bearer <token>", busca la sesión y deja al usuario
 * identificado con su rol. El usuario se vuelve a leer de la base en cada petición, así que
 * si lo eliminan o le cambian el rol, el cambio se aplica de inmediato*/
public class TokenAuthFilter extends OncePerRequestFilter {

    private final SesionService sesionService;
    private final UsuarioRepository usuarioRepository;

    public TokenAuthFilter(SesionService sesionService, UsuarioRepository usuarioRepository) {
        this.sesionService = sesionService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String encabezado = request.getHeader("Authorization");
        if (encabezado != null && encabezado.startsWith("Bearer ")) {
            String token = encabezado.substring(7).trim();
            sesionService.buscarIdUsuario(token)
                    .flatMap(usuarioRepository::findById)
                    .ifPresent(u -> {
                        UsuarioSesion sesion = new UsuarioSesion(
                                u.getIdUsuario(), u.getUsername(), u.getNombre(), u.getRol());
                        var autenticacion = new UsernamePasswordAuthenticationToken(
                                sesion,
                                token, // se guarda para poder cerrar la sesión (logout)
                                List.of(new SimpleGrantedAuthority("ROLE_" + u.getRol().toUpperCase())));
                        SecurityContextHolder.getContext().setAuthentication(autenticacion);
                    });
        }
        chain.doFilter(request, response);
    }
}
