package Minimarket.GlobalExpress_SpringBoot.factory;

import java.util.List;

/**
 * Producto abstracto del patrón Factory (la clase base "Usuario" del diagrama UML).
 * Cada tipo de usuario (Administrador, Cajero) hereda de aquí y define sus operaciones.
 */
public abstract class PerfilUsuario {

    public abstract String getRol();

    /** Operaciones que este tipo de usuario puede realizar (según el diagrama UML). */
    public abstract List<String> getPermisos();

    public boolean puede(String operacion) {
        return getPermisos().contains(operacion);
    }
}
