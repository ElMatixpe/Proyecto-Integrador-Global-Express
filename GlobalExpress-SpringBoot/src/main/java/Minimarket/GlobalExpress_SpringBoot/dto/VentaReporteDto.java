package Minimarket.GlobalExpress_SpringBoot.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Una venta con su detalle, para calcular los indicadores del módulo de Reportes. */
public record VentaReporteDto(
        Integer idVenta,
        LocalDateTime fecha,
        String estado,
        BigDecimal total,
        List<DetalleReporteDto> detalles) {
}
