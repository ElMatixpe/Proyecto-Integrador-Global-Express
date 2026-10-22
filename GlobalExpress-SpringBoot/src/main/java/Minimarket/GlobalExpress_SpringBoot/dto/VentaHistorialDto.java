package Minimarket.GlobalExpress_SpringBoot.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/* CONSULTA 1 (historial de ventas): une las tablas venta y usuario. Datos que se devuelven al listar.*/
public record VentaHistorialDto(
        Integer idVenta,
        LocalDateTime fecha,
        String cliente,
        String metodoPago,
        String estado,
        BigDecimal total,
        String vendedor) {
}
