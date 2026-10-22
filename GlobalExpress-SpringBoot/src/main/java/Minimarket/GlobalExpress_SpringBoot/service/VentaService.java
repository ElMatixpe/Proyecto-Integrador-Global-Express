package Minimarket.GlobalExpress_SpringBoot.service;

import Minimarket.GlobalExpress_SpringBoot.dto.DetalleRequest;
import Minimarket.GlobalExpress_SpringBoot.dto.DetalleVentaDto;
import Minimarket.GlobalExpress_SpringBoot.dto.VentaHistorialDto;
import Minimarket.GlobalExpress_SpringBoot.dto.VentaRequest;
import Minimarket.GlobalExpress_SpringBoot.exception.RecursoNoEncontradoException;
import Minimarket.GlobalExpress_SpringBoot.model.DetalleVenta;
import Minimarket.GlobalExpress_SpringBoot.model.Producto;
import Minimarket.GlobalExpress_SpringBoot.model.Usuario;
import Minimarket.GlobalExpress_SpringBoot.model.Venta;
import Minimarket.GlobalExpress_SpringBoot.repository.DetalleVentaRepository;
import Minimarket.GlobalExpress_SpringBoot.repository.ProductoRepository;
import Minimarket.GlobalExpress_SpringBoot.repository.ReporteRepository;
import Minimarket.GlobalExpress_SpringBoot.repository.UsuarioRepository;
import Minimarket.GlobalExpress_SpringBoot.repository.VentaRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VentaService {

    private static final Set<String> METODOS_PAGO = Set.of("Efectivo", "Tarjeta", "Yape", "Plin");
    private static final String PAGADO = "Pagado";
    private static final String CANCELADO = "Cancelado";
    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReporteRepository reporteRepository;

    public VentaService(VentaRepository ventaRepository,
                        DetalleVentaRepository detalleRepository,
                        ProductoRepository productoRepository,
                        UsuarioRepository usuarioRepository,
                        ReporteRepository reporteRepository) {
        this.ventaRepository = ventaRepository;
        this.detalleRepository = detalleRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.reporteRepository = reporteRepository;
    }

    /* Historial. estado = "Pagado", "Cancelado" o null/vacio para todas. */
    @Transactional(readOnly = true)
    public List<VentaHistorialDto> listar(String estado) {
        if (estado == null || estado.isBlank()) {
            return ventaRepository.consultarHistorial();
        }
        return ventaRepository.consultarHistorialPorEstado(estado);
    }

    @Transactional(readOnly = true)
    public Venta obtener(Integer id) {
        return ventaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Venta", id));
    }

    @Transactional(readOnly = true)
    public List<DetalleVentaDto> detalles(Integer idVenta) {
        obtener(idVenta); 
        return detalleRepository.consultarDetalle(idVenta);
    }

    /**Eliminacion logica: cancela una venta ya registrada. El registro NO se borra: cambia su
     * estado de "Pagado" a "Cancelado", sigue en el historial y deja de contar como ingreso */
    @Transactional
    public VentaHistorialDto cancelar(Integer id) {
        Venta venta = obtener(id);
        if (CANCELADO.equals(venta.getEstado())) {
            throw new IllegalArgumentException("La venta ya está cancelada.");
        }
        venta.setEstado(CANCELADO);
        Venta guardada = ventaRepository.save(venta);
        return new VentaHistorialDto(
                guardada.getIdVenta(),
                guardada.getFecha(),
                guardada.getCliente(),
                guardada.getMetodoPago(),
                guardada.getEstado(),
                guardada.getTotal(),
                guardada.getUsuario().getNombre());
    }

    /** eliminacion fisica: borra para siempre una venta CANCELADA y todas sus líneas de detalle.
     * No se puede deshacer. Una venta Pagada nunca se borra
     * Los reportes que mencionaban la venta no se borran, solo quedan sin venta asociada. */
    @Transactional
    public void eliminarFisicamente(Integer id) {
        Venta venta = obtener(id);
        if (!CANCELADO.equals(venta.getEstado())) {
            throw new IllegalArgumentException(
                    "Solo se puede eliminar de forma permanente una venta cancelada. Cancélala primero.");
        }
        reporteRepository.desvincularVenta(id);
        detalleRepository.eliminarPorVenta(id);
        ventaRepository.delete(venta);
    }

    /**Registra una venta Pagada (Confirmar Venta) o Cancelada (Cancelar con confirmacion).
     * Los precios salen de la tabla producto y el total se calcula aqui, no en el cliente.     */
    @Transactional
    public Venta registrar(VentaRequest req) {
        if (req == null || req.idUsuario() == null) {
            throw new IllegalArgumentException("Falta el usuario (vendedor) de la venta.");
        }
        Usuario vendedor = usuarioRepository.findById(req.idUsuario())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", req.idUsuario()));

        String cliente = req.cliente() == null ? "" : req.cliente().trim();
        if (cliente.isEmpty()) {
            cliente = "Cliente general";
        }
        if (cliente.length() > 50) {
            throw new IllegalArgumentException("El nombre del cliente no debe superar los 50 caracteres.");
        }

        if (req.metodoPago() == null || !METODOS_PAGO.contains(req.metodoPago())) {
            throw new IllegalArgumentException("Método de pago no válido (Efectivo, Tarjeta, Yape o Plin).");
        }

        String estado = (req.estado() == null || req.estado().isBlank()) ? PAGADO : req.estado();
        if (!PAGADO.equals(estado) && !CANCELADO.equals(estado)) {
            throw new IllegalArgumentException("El estado debe ser Pagado o Cancelado.");
        }

        if (req.detalles() == null || req.detalles().isEmpty()) {
            throw new IllegalArgumentException("La venta debe tener al menos un producto.");
        }

        List<DetalleVenta> lineas = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (DetalleRequest d : req.detalles()) {
            if (d == null || d.idProducto() == null) {
                throw new IllegalArgumentException("Hay una línea sin producto.");
            }
            if (d.cantidad() == null || d.cantidad() < 1) {
                throw new IllegalArgumentException("Todas las cantidades deben ser enteros mayores a 0.");
            }
            Producto producto = productoRepository.findById(d.idProducto())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Producto", d.idProducto()));
            if (PAGADO.equals(estado) && !"Disponible".equals(producto.getEstado())) {
                throw new IllegalArgumentException("El producto " + producto.getNombre() + " no está disponible.");
            }

            BigDecimal subtotal = producto.getPrecio().multiply(BigDecimal.valueOf(d.cantidad()));
            DetalleVenta linea = new DetalleVenta();
            linea.setProducto(producto);
            linea.setCantidad(d.cantidad());
            linea.setPrecioUnitario(producto.getPrecio());
            linea.setSubtotal(subtotal);
            lineas.add(linea);
            total = total.add(subtotal);
        }

        Venta venta = new Venta();
        venta.setFecha(LocalDateTime.now());
        venta.setUsuario(vendedor);
        venta.setCliente(cliente);
        venta.setMetodoPago(req.metodoPago());
        venta.setEstado(estado);
        venta.setTotal(total);
        Venta guardada = ventaRepository.save(venta);

        for (DetalleVenta linea : lineas) {
            linea.setVenta(guardada);
        }
        detalleRepository.saveAll(lineas);

        return guardada;
    }
}
