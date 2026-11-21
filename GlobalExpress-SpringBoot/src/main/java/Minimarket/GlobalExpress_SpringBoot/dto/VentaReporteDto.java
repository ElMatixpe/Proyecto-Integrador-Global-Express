package Minimarket.GlobalExpress_SpringBoot.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Una venta con su detalle, para el módulo de Reportes: sirve para calcular los indicadores y para
 * el reporte de "Registro de ventas por rango de fechas" (cliente, metodo de pago y vendedor) */
public record VentaReporteDto(
        Integer idVenta,
        LocalDateTime fecha,
        String cliente,
        String metodoPago,
        String estado,
        BigDecimal total,
        String vendedor,
        List<DetalleReporteDto> detalles) {
}
