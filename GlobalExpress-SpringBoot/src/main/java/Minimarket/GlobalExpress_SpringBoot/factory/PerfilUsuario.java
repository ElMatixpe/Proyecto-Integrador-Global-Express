package Minimarket.GlobalExpress_SpringBoot.factory;

import java.util.List;

/** Producto abstracto del patron Factory
 * Cada tipo de usuario (Administrador, Cajero) hereda de aquí y define sus operaciones */
public abstract class PerfilUsuario {

    public abstract String getRol();

    public abstract List<String> getPermisos();

    public boolean puede(String operacion) {
        return getPermisos().contains(operacion);
    }
}
