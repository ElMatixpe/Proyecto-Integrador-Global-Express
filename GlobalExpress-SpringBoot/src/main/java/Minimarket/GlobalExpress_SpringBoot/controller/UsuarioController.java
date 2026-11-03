package Minimarket.GlobalExpress_SpringBoot.controller;

import Minimarket.GlobalExpress_SpringBoot.dto.LoginRequest;
import Minimarket.GlobalExpress_SpringBoot.dto.LoginResponse;
import Minimarket.GlobalExpress_SpringBoot.dto.UsuarioRequest;
import Minimarket.GlobalExpress_SpringBoot.model.Usuario;
import Minimarket.GlobalExpress_SpringBoot.security.SesionService;
import Minimarket.GlobalExpress_SpringBoot.service.UsuarioService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final SesionService sesionService;

    public UsuarioController(UsuarioService usuarioService, SesionService sesionService) {
        this.usuarioService = usuarioService;
        this.sesionService = sesionService;
    }

    @GetMapping
    public List<Usuario> listar() {
        return usuarioService.listar();
    }

    @GetMapping("/{id}")
    public Usuario obtener(@PathVariable Integer id) {
        return usuarioService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Usuario crear(@Valid @RequestBody UsuarioRequest usuario) {
        return usuarioService.crear(usuario.aEntidad());
    }

    @PutMapping("/{id}")
    public Usuario actualizar(@PathVariable Integer id, @Valid @RequestBody UsuarioRequest usuario) {
        return usuarioService.actualizar(id, usuario.aEntidad());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer id) {
        usuarioService.eliminar(id);
    }

    /* Login del React: devuelve { token, usuario } */
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest credenciales) {
        Usuario usuario = usuarioService.autenticar(credenciales.username(), credenciales.contrasena());
        return new LoginResponse(sesionService.crear(usuario.getIdUsuario()), usuario);
    }

    /* Cierra la sesión: el token deja de servir. */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(Authentication autenticacion) {
        sesionService.cerrar((String) autenticacion.getCredentials());
    }
}
