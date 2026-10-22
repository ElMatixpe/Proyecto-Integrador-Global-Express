package Minimarket.GlobalExpress_SpringBoot.factory;

import java.util.List;

public class PerfilAdministrador extends PerfilUsuario {

    @Override
    public String getRol() {
        return "Administrador";
    }

    @Override
    public List<String> getPermisos() {
        return List.of("registrarProducto", "actualizarProducto", "eliminarProducto", "generarReporteVentas");
    }
}
