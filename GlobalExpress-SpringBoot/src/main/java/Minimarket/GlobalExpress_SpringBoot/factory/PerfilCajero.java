package Minimarket.GlobalExpress_SpringBoot.factory;

import java.util.List;

public class PerfilCajero extends PerfilUsuario {

    @Override
    public String getRol() {
        return "Cajero";
    }

    @Override
    public List<String> getPermisos() {
        return List.of("registrarVenta", "consultarVenta", "consultarProducto", "actualizarVenta");
    }
}
