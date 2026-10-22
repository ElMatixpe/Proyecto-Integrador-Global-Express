package Minimarket.GlobalExpress_SpringBoot.factory;

import Minimarket.GlobalExpress_SpringBoot.model.Usuario;
import org.springframework.stereotype.Component;

/**
 * PATRÓN FACTORY: centraliza la creación de los distintos tipos de usuario.
 * El resto del sistema (servicios, controladores) no hace "new PerfilAdministrador()"
 * ni "new PerfilCajero()": le pide a la fábrica el tipo según el rol. Si mañana se
 * agrega un rol nuevo (p. ej. Supervisor), solo se crea su clase y se añade un caso aquí.
 */
@Component
public class UsuarioFactory {

    /** Crea el tipo de usuario (con su comportamiento) que corresponde al rol. */
    public PerfilUsuario crearPerfil(String rol) {
        if (rol == null) {
            throw new IllegalArgumentException("El rol debe ser Administrador o Cajero.");
        }
        return switch (rol) {
            case "Administrador" -> new PerfilAdministrador();
            case "Cajero" -> new PerfilCajero();
            default -> throw new IllegalArgumentException("El rol debe ser Administrador o Cajero.");
        };
    }

    /** Crea la entidad Usuario lista para guardar, con el rol validado por la fábrica. */
    public Usuario crearUsuario(String rol, String nombre, String username, String contrasena) {
        PerfilUsuario perfil = crearPerfil(rol);
        Usuario usuario = new Usuario();
        usuario.setRol(perfil.getRol());
        usuario.setNombre(nombre);
        usuario.setUsername(username);
        usuario.setContrasena(contrasena);
        return usuario;
    }
}
