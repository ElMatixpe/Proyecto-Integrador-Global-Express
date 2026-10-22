package Minimarket.GlobalExpress_SpringBoot.controller;

import Minimarket.GlobalExpress_SpringBoot.config.ConexionBD;
import Minimarket.GlobalExpress_SpringBoot.factory.PerfilUsuario;
import Minimarket.GlobalExpress_SpringBoot.factory.UsuarioFactory;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint de comprobación del patrón Singleton sobre la conexión a la base de datos. */
@RestController
@RequestMapping("/api/sistema")
public class SistemaController {

    private final ConexionBD conexionBD;
    private final DataSource dataSource; // el mismo que usan JPA y los repositorios
    private final UsuarioFactory usuarioFactory;

    public SistemaController(ConexionBD conexionBD, DataSource dataSource, UsuarioFactory usuarioFactory) {
        this.conexionBD = conexionBD;
        this.dataSource = dataSource;
        this.usuarioFactory = usuarioFactory;
    }

    /**
     * GET /api/sistema/conexion
     * Si llamas varias veces, "instanciaConexionBD" y "instanciaDataSource" no cambian:
     * es siempre el mismo objeto compartido. "mismoDataSource" = true confirma que
     * ConexionBD y el resto del sistema usan el mismo pool.
     */
    @GetMapping("/conexion")
    public Map<String, Object> conexion() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("conectado", conexionBD.estaConectada());
        info.put("baseDeDatos", conexionBD.nombreBaseDeDatos());
        info.put("pool", dataSource.getClass().getSimpleName());
        info.put("instanciaConexionBD", System.identityHashCode(conexionBD));
        info.put("instanciaDataSource", System.identityHashCode(dataSource));
        info.put("mismoDataSource", conexionBD.getDataSource() == dataSource);
        return info;
    }

    /**
     * GET /api/sistema/perfil/Administrador  |  /api/sistema/perfil/Cajero
     * Comprueba el patrón Factory: la fábrica devuelve una clase distinta según el rol.
     * Con un rol inexistente responde 400.
     */
    @GetMapping("/perfil/{rol}")
    public Map<String, Object> perfil(@PathVariable String rol) {
        PerfilUsuario perfil = usuarioFactory.crearPerfil(rol);
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("rol", perfil.getRol());
        info.put("claseCreada", perfil.getClass().getSimpleName());
        info.put("permisos", perfil.getPermisos());
        return info;
    }
}
