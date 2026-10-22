package Minimarket.GlobalExpress_SpringBoot.service;

import Minimarket.GlobalExpress_SpringBoot.dto.DetalleReporteDto;
import Minimarket.GlobalExpress_SpringBoot.dto.ReporteRequest;
import Minimarket.GlobalExpress_SpringBoot.dto.VentaReporteDto;
import Minimarket.GlobalExpress_SpringBoot.exception.RecursoNoEncontradoException;
import Minimarket.GlobalExpress_SpringBoot.model.Reporte;
import Minimarket.GlobalExpress_SpringBoot.model.Usuario;
import Minimarket.GlobalExpress_SpringBoot.model.Venta;
import Minimarket.GlobalExpress_SpringBoot.repository.DetalleVentaRepository;
import Minimarket.GlobalExpress_SpringBoot.repository.ReporteRepository;
import Minimarket.GlobalExpress_SpringBoot.repository.UsuarioRepository;
import Minimarket.GlobalExpress_SpringBoot.repository.VentaRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReporteService {

    private final ReporteRepository reporteRepository;
    private final UsuarioRepository usuarioRepository;
    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;

    public ReporteService(ReporteRepository reporteRepository,
            UsuarioRepository usuarioRepository,
            VentaRepository ventaRepository,
            DetalleVentaRepository detalleVentaRepository) {
        this.reporteRepository = reporteRepository;
        this.usuarioRepository = usuarioRepository;
        this.ventaRepository = ventaRepository;
        this.detalleVentaRepository = detalleVentaRepository;
    }

    @Transactional(readOnly = true)
    public List<Reporte> listar() {
        return reporteRepository.findAllByOrderByIdReporteDesc();
    }

    @Transactional(readOnly = true)
    public Reporte obtener(Integer id) {
        return reporteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reporte", id));
    }

    /**
     * Ventas (con su detalle) entre dos fechas, ambas incluidas. Es la base de los
     * indicadores
     * del reporte: el React calcula con ellas el porcentaje de ventas por mes, la
     * participación
     * por producto, la variación mensual, el ticket promedio y las tarjetas de
     * resumen.
     */
    @Transactional(readOnly = true)
    public List<VentaReporteDto> ventasDelPeriodo(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Debes indicar las fechas desde y hasta.");
        }
        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha desde no puede ser posterior a la fecha hasta.");
        }
        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.plusDays(1).atStartOfDay();

        List<Venta> ventas = ventaRepository.findByFechaGreaterThanEqualAndFechaLessThanOrderByFechaAsc(inicio, fin);

        Map<Integer, List<DetalleReporteDto>> detallesPorVenta = detalleVentaRepository
                .findByVentaFechaGreaterThanEqualAndVentaFechaLessThan(inicio, fin)
                .stream()
                .collect(Collectors.groupingBy(
                        d -> d.getVenta().getIdVenta(),
                        Collectors.mapping(
                                d -> new DetalleReporteDto(
                                        d.getProducto().getIdProducto(),
                                        d.getProducto().getNombre(),
                                        d.getProducto().getMarca(),
                                        d.getCantidad(),
                                        d.getSubtotal()),
                                Collectors.toList())));

        return ventas.stream()
                .map(v -> new VentaReporteDto(
                        v.getIdVenta(),
                        v.getFecha(),
                        v.getEstado(),
                        v.getTotal(),
                        detallesPorVenta.getOrDefault(v.getIdVenta(), List.of())))
                .toList();
    }

    @Transactional
    public Reporte generar(ReporteRequest req) {
        if (req == null || req.idUsuario() == null
                || req.periodoInicio() == null || req.periodoFin() == null) {
            throw new IllegalArgumentException("Usuario y período (inicio y fin) son obligatorios.");
        }
        if (req.periodoInicio().isAfter(req.periodoFin())) {
            throw new IllegalArgumentException("El inicio del período no puede ser posterior al fin.");
        }
        Usuario usuario = usuarioRepository.findById(req.idUsuario())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", req.idUsuario()));
        Venta venta = null; // la venta es opcional
        if (req.idVenta() != null) {
            venta = ventaRepository.findById(req.idVenta())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Venta", req.idVenta()));
        }

        Reporte reporte = new Reporte();
        reporte.setFechaGeneracion(LocalDateTime.now());
        reporte.setPeriodoInicio(req.periodoInicio());
        reporte.setPeriodoFin(req.periodoFin());
        reporte.setUsuario(usuario);
        reporte.setVenta(venta);
        return reporteRepository.save(reporte);
    }

    @Transactional
    public void eliminar(Integer id) {
        reporteRepository.delete(obtener(id));
    }
}
