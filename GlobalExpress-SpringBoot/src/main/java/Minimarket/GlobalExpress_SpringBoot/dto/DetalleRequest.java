package Minimarket.GlobalExpress_SpringBoot.dto;

/** Una línea del carrito: qué producto y cuántas unidades. */
public record DetalleRequest(Integer idProducto, Integer cantidad) {
}
