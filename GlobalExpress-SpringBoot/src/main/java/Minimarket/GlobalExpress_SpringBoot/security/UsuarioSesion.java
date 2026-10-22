package Minimarket.GlobalExpress_SpringBoot.security;

/** Datos del usuario autenticado (id, username, nombre, rol). */
public record UsuarioSesion(Integer idUsuario, String username, String nombre, String rol) {
}
