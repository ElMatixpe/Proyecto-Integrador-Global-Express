package Minimarket.GlobalExpress_SpringBoot.dto;

import Minimarket.GlobalExpress_SpringBoot.model.Producto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/* Lo que envía el React al registrar o editar un producto.
 * Las anotaciones se validan solas gracias a @Valid en ProductoController. */
public record ProductoRequest(

                @NotBlank(message = "El nombre del producto es obligatorio.") @Size(max = 150, message = "El nombre del producto no debe superar los 150 caracteres.") @Pattern(regexp = ReglasValidacion.TEXTO_PRODUCTO, message = "El nombre del producto debe ser texto: necesita al menos una letra (puede llevar números, ej. 1L) y no admite símbolos especiales.") String nombre,

                @NotBlank(message = "La marca es obligatoria.") @Size(max = 100, message = "La marca no debe superar los 100 caracteres.") @Pattern(regexp = ReglasValidacion.TEXTO_PRODUCTO, message = "La marca debe ser texto: necesita al menos una letra y no admite símbolos especiales.") String marca,

                // BigDecimal = tipo numérico decimal: solo acepta números (no texto)
                @NotNull(message = "El precio es obligatorio.") @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0.") @Digits(integer = 8, fraction = 2, message = "El precio admite hasta 8 enteros y 2 decimales (ej. 5.50).") BigDecimal precio,

                @NotNull(message = "El estado es obligatorio.") @Pattern(regexp = "Disponible|No Disponible", message = "El estado debe ser Disponible o No Disponible.") String estado) {

        /* Convierte la solicitud ya validada en la entidad que guarda ProductoService.*/
        public Producto aEntidad() {
                Producto producto = new Producto();
                producto.setNombre(nombre);
                producto.setMarca(marca);
                producto.setPrecio(precio);
                producto.setEstado(estado);
                return producto;
        }
}
