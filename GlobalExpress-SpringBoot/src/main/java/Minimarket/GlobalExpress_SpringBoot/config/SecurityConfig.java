package Minimarket.GlobalExpress_SpringBoot.config;

import Minimarket.GlobalExpress_SpringBoot.repository.UsuarioRepository;
import Minimarket.GlobalExpress_SpringBoot.security.MonitorTokenFilter;
import Minimarket.GlobalExpress_SpringBoot.security.SesionService;
import Minimarket.GlobalExpress_SpringBoot.security.TokenAuthFilter;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/* Seguridad por roles y CORS (el React llama desde las direcciones de app.cors.origins) */
@Configuration
public class SecurityConfig {

        private final SesionService sesionService;
        private final UsuarioRepository usuarioRepository;
        private final List<String> origenesPermitidos;
        private final String monitorToken;

        public SecurityConfig(SesionService sesionService,
                        UsuarioRepository usuarioRepository,
                        @Value("${app.cors.allowed-origins:${app.cors.origins:${CORS_ORIGINS:http://localhost:3000}}}") String origenes,
                        @Value("${app.monitor.token:${MONITOR_TOKEN:}}") String monitorToken) {
                this.monitorToken = monitorToken;
                this.sesionService = sesionService;
                this.usuarioRepository = usuarioRepository;
                // Varias direcciones se separan con coma:
                // https://mi-app.netlify.app,http://localhost:3000
                this.origenesPermitidos = Arrays.stream(origenes.split(","))
                                .map(String::trim)
                                .filter(o -> !o.isEmpty())
                                .toList();
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .cors(Customizer.withDefaults())
                                // Sin cookies ni sesión del servidor: la identidad va en el token, por eso no
                                // hace falta CSRF
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .exceptionHandling(e -> e.authenticationEntryPoint(
                                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                                .addFilterBefore(
                                                new TokenAuthFilter(sesionService, usuarioRepository),
                                                UsernamePasswordAuthenticationFilter.class)
                                // Monitoreo: /actuator/** solo con la clave MONITOR_TOKEN (la usa Grafana)
                                .addFilterBefore(
                                                new MonitorTokenFilter(monitorToken),
                                                UsernamePasswordAuthenticationFilter.class)
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/usuarios/login").permitAll()
                                                .requestMatchers("/api/sistema/**").permitAll()
                                                .requestMatchers("/actuator/**").hasRole("MONITOR")
                                                .requestMatchers(HttpMethod.POST, "/api/usuarios/logout")
                                                .authenticated()
                                                .requestMatchers("/api/usuarios/**").hasRole("ADMINISTRADOR")
                                                .requestMatchers("/api/reportes/**").hasRole("ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.GET, "/api/productos/**").authenticated()
                                                .requestMatchers("/api/productos/**").hasRole("ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.DELETE, "/api/ventas/**")
                                                .hasRole("ADMINISTRADOR")
                                                .requestMatchers("/api/ventas/**").authenticated()
                                                .anyRequest().authenticated());
                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(origenesPermitidos);
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(List.of("*"));

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/api/**", config);
                return source;
        }
}