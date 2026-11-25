package co.edu.unbosque.reservas.repository;

import co.edu.unbosque.model.reservas.EstadoReserva;
import co.edu.unbosque.model.reservas.Reserva;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    Optional<Reserva> findByClaveIdempotencia(String clave);

    // más recientes primero
    List<Reserva> findAllByOrderByIdDesc();

    List<Reserva> findByHuespedCorreoOrderByIdDesc(String correo);

    List<Reserva> findByPublicacionAnfitrionCorreoOrderByIdDesc(String correo);

    // reservas futuras (HU-06)
    boolean existsByHuespedIdAndEstadoAndFechaLlegadaGreaterThanEqual(Long idHuesped, EstadoReserva estado, LocalDate desde);

    boolean existsByPublicacionAnfitrionIdAndEstadoAndFechaLlegadaGreaterThanEqual(Long idAnfitrion, EstadoReserva estado, LocalDate desde);

    // cruces de fechas
    @Query("""
            select count(r) from Reserva r
            where r.publicacion.id = :publicacion and r.estado in :estados
              and r.fechaLlegada < :salida and r.fechaSalida > :llegada
            """)
    long contarCruces(@Param("publicacion") Long publicacion, @Param("estados") Collection<EstadoReserva> estados,
                      @Param("llegada") LocalDate llegada, @Param("salida") LocalDate salida);

    // para reportes
    @Query("select r.estado, count(r) from Reserva r group by r.estado order by r.estado")
    List<Object[]> contarPorEstado();

    // reservas desde fecha
    long countByFechaCreacionGreaterThanEqual(LocalDateTime desde);

    // reservas que cuentan
    List<Reserva> findByEstadoInOrderByFechaLlegada(Collection<EstadoReserva> estados);

    @Query("select coalesce(sum(r.total), 0) from Reserva r where r.estado in :estados")
    BigDecimal sumarTotales(@Param("estados") Collection<EstadoReserva> estados);
}
