package Minimarket.GlobalExpress_SpringBoot.config;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/** PATRÓN SINGLETON: controla la conexion a la base de datos mediante una unica
 * instancia compartida por todo el sistema */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ConexionBD {

    private final DataSource dataSource;

    public ConexionBD(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** El unico DataSource (pool) compartido por el sistema. */
    public DataSource getDataSource() {
        return dataSource;
    }

    /** Presta una conexion del pool; quien la use debe cerrarla */
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
