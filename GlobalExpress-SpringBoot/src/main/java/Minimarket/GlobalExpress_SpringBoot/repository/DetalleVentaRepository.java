package Minimarket.GlobalExpress_SpringBoot.repository;

import Minimarket.GlobalExpress_SpringBoot.model.DetalleVenta;
import Minimarket.GlobalExpress_SpringBoot.dto.DetalleVentaDto;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Integer> {

    List<DetalleVenta> findByVentaIdVenta(Integer idVenta);

    // Borrado fisico de las lineas de una venta 
    @Modifying
    @Query("DELETE FROM DetalleVenta d WHERE d.venta.idVenta = :idVenta")
    int eliminarPorVenta(@Param("idVenta") Integer idVenta);

    /**
     * CONSULTA 2: detalle_venta + producto */
    @Query("""
            SELECT new Minimarket.GlobalExpress_SpringBoot.dto.DetalleVentaDto(
                d.idDetalle, d.cantidad, d.precioUnitario, d.subtotal,
                p.idProducto, p.nombre, p.marca)
            FROM DetalleVenta d JOIN d.producto p
            WHERE d.venta.idVenta = :idVenta
            ORDER BY d.idDetalle
            """)
    List<DetalleVentaDto> consultarDetalle(@Param("idVenta") Integer idVenta);

    // Detalles de todas las ventas de un periodo (fecha de la venta >= inicio y < fin)
    List<DetalleVenta> findByVentaFechaGreaterThanEqualAndVentaFechaLessThan(
            LocalDateTime inicio, LocalDateTime fin);
}
