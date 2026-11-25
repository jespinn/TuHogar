package co.edu.unbosque.reservas.repository;

import co.edu.unbosque.model.reservas.Cancelacion;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CancelacionRepository extends JpaRepository<Cancelacion, Long> {
    Optional<Cancelacion> findByReservaId(Long idReserva);
}
