package Minimarket.GlobalExpress_SpringBoot.controller;

import Minimarket.GlobalExpress_SpringBoot.dto.VentaRequest;
import Minimarket.GlobalExpress_SpringBoot.dto.DetalleVentaDto;
import Minimarket.GlobalExpress_SpringBoot.dto.VentaHistorialDto;
import Minimarket.GlobalExpress_SpringBoot.model.Venta;
import Minimarket.GlobalExpress_SpringBoot.security.UsuarioSesion;
import Minimarket.GlobalExpress_SpringBoot.service.VentaService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    /** GET /api/ventas  |  GET /api/ventas?estado=Pagado  |  GET /api/ventas?estado=Cancelado */
    @GetMapping
    public List<VentaHistorialDto> listar(@RequestParam(required = false) String estado) {
        return ventaService.listar(estado);
    }

    @GetMapping("/{id}")
    public Venta obtener(@PathVariable Integer id) {
        return ventaService.obtener(id);
    }

    @GetMapping("/{id}/detalles")
    public List<DetalleVentaDto> detalles(@PathVariable Integer id) {
        return ventaService.detalles(id);
    }

    /** Eliminación logica: PUT /api/ventas/{id}/cancelar (Administrador y Cajero) */
    @PutMapping("/{id}/cancelar")
    public VentaHistorialDto cancelar(@PathVariable Integer id) {
        return ventaService.cancelar(id);
    }

    /** Eliminación fisica: DELETE /api/ventas/{id} (solo Administrador, solo ventas canceladas) */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer id) {
        ventaService.eliminarFisicamente(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Venta registrar(@RequestBody VentaRequest solicitud, @AuthenticationPrincipal UsuarioSesion sesion) {
        VentaRequest conVendedor = new VentaRequest(
                sesion.idUsuario(), solicitud.cliente(), solicitud.metodoPago(),
                solicitud.estado(), solicitud.detalles());
        return ventaService.registrar(conVendedor);
    }
}
