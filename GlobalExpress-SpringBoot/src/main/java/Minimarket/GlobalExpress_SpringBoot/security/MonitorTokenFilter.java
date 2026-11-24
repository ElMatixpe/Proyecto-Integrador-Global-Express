package Minimarket.GlobalExpress_SpringBoot.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/** Protege /actuator/**: solo entra quien envía "Authorization: Bearer <MONITOR_TOKEN>".
 * Es la clave que se configura en Grafana Cloud (Metrics Endpoint). No es la sesión de un usuario
 * de la aplicación. Si MONITOR_TOKEN está vacía, nadie puede entrar.
 * No es un @Component a propósito: lo crea SecurityConfig, igual que TokenAuthFilter */
public class MonitorTokenFilter extends OncePerRequestFilter {

    private final byte[] tokenEsperado;

    public MonitorTokenFilter(String token) {
        this.tokenEsperado = token == null ? new byte[0] : token.trim().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String encabezado = request.getHeader("Authorization");
        if (tokenEsperado.length > 0 && encabezado != null && encabezado.startsWith("Bearer ")) {
            byte[] recibido = encabezado.substring(7).trim().getBytes(StandardCharsets.UTF_8);
            // Comparación en tiempo constante (evita adivinar la clave midiendo tiempos)
            if (MessageDigest.isEqual(tokenEsperado, recibido)) {
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "monitor", null, List.of(new SimpleGrantedAuthority("ROLE_MONITOR"))));
            }
        }
        chain.doFilter(request, response);
    }
}
