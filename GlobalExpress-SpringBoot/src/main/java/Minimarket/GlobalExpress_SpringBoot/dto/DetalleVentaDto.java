package Minimarket.GlobalExpress_SpringBoot.dto;

import java.math.BigDecimal;

/**
 * CONSULTA 2 (detalle de una venta): une las tablas detalle_venta y producto.
 *  - Campos de la tabla detalle_venta: idDetalle, cantidad, precioUnitario, subtotal
 *  - Campos de la tabla producto:      idProducto, nombre, marca
 */
public record DetalleVentaDto(
        Integer idDetalle,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal,
        Integer idProducto,
        String nombre,
        String marca) {
}
