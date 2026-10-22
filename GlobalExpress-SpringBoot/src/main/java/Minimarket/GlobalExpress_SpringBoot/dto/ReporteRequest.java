package Minimarket.GlobalExpress_SpringBoot.dto;

import java.time.LocalDate;

public record ReporteRequest(
        Integer idUsuario,
        Integer idVenta,
        LocalDate periodoInicio,
        LocalDate periodoFin) {
}
