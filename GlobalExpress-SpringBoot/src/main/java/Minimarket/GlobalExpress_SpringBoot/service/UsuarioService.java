package Minimarket.GlobalExpress_SpringBoot.service;

import Minimarket.GlobalExpress_SpringBoot.exception.CredencialesInvalidasException;
import Minimarket.GlobalExpress_SpringBoot.exception.RecursoNoEncontradoException;
import Minimarket.GlobalExpress_SpringBoot.factory.UsuarioFactory;
import Minimarket.GlobalExpress_SpringBoot.model.Usuario;
import Minimarket.GlobalExpress_SpringBoot.repository.UsuarioRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioFactory usuarioFactory;

    public UsuarioService(UsuarioRepository usuarioRepository, UsuarioFactory usuarioFactory) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioFactory = usuarioFactory;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Usuario obtener(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
    }

    @Transactional
    public Usuario crear(Usuario datos) {
        validarDatos(datos);
        validarContrasena(datos.getContrasena());
        if (usuarioRepository.existsByUsername(datos.getUsername().trim())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese nombre de usuario.");
        }
        // patron factory: la fabrica crea el tipo de usuario segun el rol
        Usuario nuevo = usuarioFactory.crearUsuario(
                datos.getRol(), datos.getNombre().trim(), datos.getUsername().trim(), datos.getContrasena());
        return usuarioRepository.save(nuevo);
    }

    /** Si la contraseña llega vacia se conserva la actual. */
    @Transactional
    public Usuario actualizar(Integer id, Usuario datos) {
        validarDatos(datos);
        Usuario existente = obtener(id);

        String username = datos.getUsername().trim();
        usuarioRepository.findByUsername(username).ifPresent(otro -> {
            if (!otro.getIdUsuario().equals(id)) {
                throw new IllegalArgumentException("Ya existe un usuario con ese nombre de usuario.");
            }
        });

        existente.setNombre(datos.getNombre().trim());
        existente.setUsername(username);
        existente.setRol(datos.getRol());
        if (datos.getContrasena() != null && !datos.getContrasena().isEmpty()) {
            validarContrasena(datos.getContrasena());
            existente.setContrasena(datos.getContrasena());
        }
        return usuarioRepository.save(existente);
    }

    @Transactional
    public void eliminar(Integer id) {
        usuarioRepository.delete(obtener(id));
    }

    /* Valida las credenciales del Login. Devuelve el usuario. */
    @Transactional(readOnly = true)
    public Usuario autenticar(String username, String contrasena) {
        if (username == null || contrasena == null) {
            throw new CredencialesInvalidasException();
        }
        Usuario usuario = usuarioRepository.findByUsername(username.trim())
                .orElseThrow(CredencialesInvalidasException::new);
        if (!usuario.getContrasena().equals(contrasena)) {
            throw new CredencialesInvalidasException();
        }
        return usuario;
    }

    private void validarDatos(Usuario u) {
        if (u == null || vacio(u.getNombre()) || vacio(u.getUsername())) {
            throw new IllegalArgumentException("El nombre y el usuario son obligatorios.");
        }
        if (u.getNombre().trim().length() > 150 || u.getUsername().trim().length() > 50) {
            throw new IllegalArgumentException("El nombre o el usuario superan la longitud permitida.");
        }
        usuarioFactory.crearPerfil(u.getRol()); // valida el rol: error si no es Administrador o Cajero
    }

    // validacion: 8 a 12 caracteres, mayúscula, minúscula, número y símbolo.
    private void validarContrasena(String c) {
        if (c == null || c.length() < 8 || c.length() > 12) {
            throw new IllegalArgumentException("La contraseña debe tener entre 8 y 12 caracteres.");
        }
        if (!c.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("La contraseña debe incluir al menos una letra mayúscula.");
        }
        if (!c.matches(".*[a-z].*")) {
            throw new IllegalArgumentException("La contraseña debe incluir al menos una letra minúscula.");
        }
        if (!c.matches(".*\\d.*")) {
            throw new IllegalArgumentException("La contraseña debe incluir al menos un dígito.");
        }
        if (!c.matches(".*[^A-Za-z0-9].*")) {
            throw new IllegalArgumentException("La contraseña debe incluir al menos un símbolo especial.");
        }
    }

    private boolean vacio(String s) {
        return s == null || s.trim().isEmpty();
    }
}
