package Minimarket.GlobalExpress_SpringBoot.dto;

import java.util.List;

/**
 * Lo que envía el React al confirmar (o cancelar) una venta.
 * El precio, subtotales, total y fecha los calcula el servidor.
 * estado: "Pagado" (por defecto) o "Cancelado".
 */
public record VentaRequest(
        Integer idUsuario,
        String cliente,
        String metodoPago,
        String estado,
        List<DetalleRequest> detalles) {
}
