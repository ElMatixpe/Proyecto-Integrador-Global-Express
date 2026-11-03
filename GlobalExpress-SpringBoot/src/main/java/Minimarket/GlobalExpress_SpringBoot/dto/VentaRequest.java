package Minimarket.GlobalExpress_SpringBoot.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

/* Lo que envía el React al confirmar (o cancelar) una venta.
 * El precio, subtotales, total y fecha los calcula el servidor.
 * estado: "Pagado" (por defecto) o "Cancelado".
 * Las anotaciones se validan solas gracias a @Valid en VentaController. */
public record VentaRequest(

        Integer idUsuario,

        // solo letras, sin números
        @Size(max = 50, message = "El nombre del cliente no debe superar los 50 caracteres.")
        @Pattern(regexp = ReglasValidacion.NOMBRE_PERSONA_OPCIONAL,
                message = "El nombre del cliente solo puede contener letras (sin números ni símbolos).")
        String cliente,

        // Solo se acepta Efectivo
        @NotNull(message = "Debe indicar el método de pago.")
        @Pattern(regexp = "Efectivo", message = "Método de pago no válido. Por ahora solo se acepta Efectivo.")
        String metodoPago,

        @Pattern(regexp = "Pagado|Cancelado", message = "El estado debe ser Pagado o Cancelado.")
        String estado,

        // @Valid hace que también se validen los datos de cada línea (DetalleRequest)
        @NotEmpty(message = "La venta debe tener al menos un producto.")
        @Valid
        List<DetalleRequest> detalles) {
}

