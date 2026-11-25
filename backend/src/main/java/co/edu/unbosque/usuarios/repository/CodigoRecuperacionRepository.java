package co.edu.unbosque.usuarios.repository;

import co.edu.unbosque.model.usuarios.CodigoRecuperacion;
import co.edu.unbosque.model.usuarios.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodigoRecuperacionRepository extends JpaRepository<CodigoRecuperacion, Long> {
    Optional<CodigoRecuperacion> findFirstByUsuarioAndCodigoOrderByIdDesc(Usuario usuario, String codigo);
}
