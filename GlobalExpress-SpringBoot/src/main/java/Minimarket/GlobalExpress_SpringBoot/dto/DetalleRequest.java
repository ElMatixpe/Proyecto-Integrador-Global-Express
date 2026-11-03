package Minimarket.GlobalExpress_SpringBoot.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/* Una línea del carrito: qué producto y cuántas unidades. */
public record DetalleRequest(

        @NotNull(message = "Hay una línea sin producto.")
        @Positive(message = "Hay una línea con un producto no válido.")
        Integer idProducto,

        // Entero de 1 a 999 (el React limita la cantidad a 3 dígitos)
        @NotNull(message = "Todas las cantidades deben ser enteros mayores a 0.")
        @Min(value = 1, message = "Todas las cantidades deben ser enteros mayores a 0.")
        @Max(value = 999, message = "La cantidad máxima por producto es 999.")
        Integer cantidad) {
}
