package Minimarket.GlobalExpress_SpringBoot.repository;

import Minimarket.GlobalExpress_SpringBoot.model.Venta;
import Minimarket.GlobalExpress_SpringBoot.dto.VentaHistorialDto;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VentaRepository extends JpaRepository<Venta, Integer> {

    @Query("""
            SELECT new Minimarket.GlobalExpress_SpringBoot.dto.VentaHistorialDto(
                v.idVenta, v.fecha, v.cliente, v.metodoPago, v.estado, v.total, u.nombre)
            FROM Venta v JOIN v.usuario u
            ORDER BY v.idVenta DESC
            """)
    List<VentaHistorialDto> consultarHistorial();

    @Query("""
            SELECT new Minimarket.GlobalExpress_SpringBoot.dto.VentaHistorialDto(
                v.idVenta, v.fecha, v.cliente, v.metodoPago, v.estado, v.total, u.nombre)
            FROM Venta v JOIN v.usuario u
            WHERE v.estado = :estado
            ORDER BY v.idVenta DESC
            """)
    List<VentaHistorialDto> consultarHistorialPorEstado(@Param("estado") String estado);

    // Historial: las más recientes primero
    List<Venta> findAllByOrderByIdVentaDesc();

    // Historial filtrado por estado ("Pagado" o "Cancelado")
    List<Venta> findByEstadoOrderByIdVentaDesc(String estado);

    // Ventas de un período: fecha >= inicio y fecha < fin
    List<Venta> findByFechaGreaterThanEqualAndFechaLessThanOrderByFechaAsc(
            LocalDateTime inicio, LocalDateTime fin);
}
