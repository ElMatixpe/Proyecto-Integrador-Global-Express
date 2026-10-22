package Minimarket.GlobalExpress_SpringBoot.dto;

import java.math.BigDecimal;

/* CONSULTA 2 (detalle de una venta): une las tablas detalle_venta y producto. Datos de cada producto en el detalle*/
public record DetalleVentaDto(
        Integer idDetalle,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal,
        Integer idProducto,
        String nombre,
        String marca) {
}
