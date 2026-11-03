package Minimarket.GlobalExpress_SpringBoot.dto;

import Minimarket.GlobalExpress_SpringBoot.model.Usuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/* Lo que envía el React al crear o editar un usuario.
 * Las anotaciones se validan solas gracias a @Valid en UsuarioController. */
public record UsuarioRequest(

                @NotBlank(message = "El nombre es obligatorio.") @Size(max = 150, message = "El nombre no debe superar los 150 caracteres.") @Pattern(regexp = ReglasValidacion.NOMBRE_PERSONA, message = "El nombre solo puede contener letras (sin números ni símbolos).") String nombre,

                @NotBlank(message = "El nombre de usuario es obligatorio.") @Size(max = 50, message = "El nombre de usuario no debe superar los 50 caracteres.") @Pattern(regexp = ReglasValidacion.NOMBRE_USUARIO, message = "El nombre de usuario solo admite letras sin tildes, números, punto, guion y guion bajo (sin espacios).") String username,

                // Vacía al editar = conservar la contraseña actual. Al crear, UsuarioService
                // exige que exista.
                @Pattern(regexp = ReglasValidacion.CONTRASENA_O_VACIA, message = "La contraseña debe tener de 8 a 12 caracteres, con mayúscula, minúscula, número y símbolo.") String contrasena,

                @NotNull(message = "El rol es obligatorio.") @Pattern(regexp = "Administrador|Cajero", message = "El rol debe ser Administrador o Cajero.") String rol) {

        /* Convierte la solicitud ya validada en la entidad que guarda UsuarioService.         */
        public Usuario aEntidad() {
                Usuario usuario = new Usuario();
                usuario.setNombre(nombre);
                usuario.setUsername(username);
                usuario.setContrasena(contrasena);
                usuario.setRol(rol);
                return usuario;
        }
}
