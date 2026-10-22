package Minimarket.GlobalExpress_SpringBoot.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * CONSULTA 1 (historial de ventas): une las tablas venta y usuario.
 *  - Campos de la tabla venta:   idVenta, fecha, cliente, metodoPago, estado, total
 *  - Campo de la tabla usuario:  nombre (se muestra como "vendedor")
 */
public record VentaHistorialDto(
        Integer idVenta,
        LocalDateTime fecha,
        String cliente,
        String metodoPago,
        String estado,
        BigDecimal total,
        String vendedor) {
}
