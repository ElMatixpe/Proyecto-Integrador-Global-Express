package Minimarket.GlobalExpress_SpringBoot.dto;

import Minimarket.GlobalExpress_SpringBoot.model.Usuario;
/* Datos que se devuelven: token y usuario */
/* Respuesta del login: el token que el React debe enviar en cada peticion y los datos del usuario */
public record LoginResponse(String token, Usuario usuario) {
}
