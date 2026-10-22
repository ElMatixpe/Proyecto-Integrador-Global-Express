package Minimarket.GlobalExpress_SpringBoot.repository;

import Minimarket.GlobalExpress_SpringBoot.model.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// Busca al usuario en la base de datos.
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);
}
