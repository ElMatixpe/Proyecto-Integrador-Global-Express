package Minimarket.GlobalExpress_SpringBoot.dto;

import java.math.BigDecimal;

/** Una línea de una venta, tal como la necesita el reporte (producto, cantidad y subtotal). */
public record DetalleReporteDto(
        Integer idProducto,
        String nombre,
        String marca,
        Integer cantidad,
        BigDecimal subtotal) {
}
