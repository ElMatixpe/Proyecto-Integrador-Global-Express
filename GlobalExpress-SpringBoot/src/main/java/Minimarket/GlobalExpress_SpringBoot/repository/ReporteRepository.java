package Minimarket.GlobalExpress_SpringBoot.repository;

import Minimarket.GlobalExpress_SpringBoot.model.Reporte;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReporteRepository extends JpaRepository<Reporte, Integer> {

    List<Reporte> findAllByOrderByIdReporteDesc();

    @Modifying
    @Query("UPDATE Reporte r SET r.venta = null WHERE r.venta.idVenta = :idVenta")
    int desvincularVenta(@Param("idVenta") Integer idVenta);
}
