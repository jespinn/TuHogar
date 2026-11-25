package co.edu.unbosque.reservas.repository;

import co.edu.unbosque.model.reservas.Pago;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    Optional<Pago> findByReservaId(Long idReserva);
}
