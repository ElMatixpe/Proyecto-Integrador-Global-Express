package Minimarket.GlobalExpress_SpringBoot.config;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * PATRÓN SINGLETON: controla la conexión a la base de datos mediante UNA única
 * instancia compartida por todo el sistema.
 *
 * - Esta clase tiene una sola instancia en toda la aplicación (scope singleton,
 *   que Spring garantiza). Nadie hace "new ConexionBD()": los servicios la reciben
 *   por inyección de dependencias y todos comparten el mismo objeto.
 * - Esa instancia guarda el único DataSource (pool de conexiones HikariCP) que
 *   Spring crea a partir de application.properties. Repositorios y servicios usan
 *   ese mismo pool, así no se abren conexiones sueltas por cada operación.
 *
 * Se usa el singleton del contenedor de Spring y no un "static getInstance()" porque
 * así se integra con la configuración, el pool y las transacciones del framework.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ConexionBD {

    private final DataSource dataSource;

    public ConexionBD(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** El único DataSource (pool) compartido por el sistema. */
    public DataSource getDataSource() {
        return dataSource;
    }

    /** Presta una conexión del pool; quien la use debe cerrarla (try-with-resources). */
    public Connection obtenerConexion() throws SQLException {
        return dataSource.getConnection();
    }

    public boolean estaConectada() {
        try (Connection conexion = obtenerConexion()) {
            return conexion.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    public String nombreBaseDeDatos() {
        try (Connection conexion = obtenerConexion()) {
            return conexion.getCatalog();
        } catch (SQLException e) {
            return null;
        }
    }
}
