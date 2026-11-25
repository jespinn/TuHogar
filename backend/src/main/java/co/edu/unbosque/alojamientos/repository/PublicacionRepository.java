package co.edu.unbosque.alojamientos.repository;

import co.edu.unbosque.model.publicaciones.Publicacion;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PublicacionRepository extends JpaRepository<Publicacion, Long> {
    List<Publicacion> findByEstado(String estado);

    // publicaciones de un anfitrión
    List<Publicacion> findByAnfitrionIdAndEstado(Long idAnfitrion, String estado);

    // activas por precio
    List<Publicacion> findByEstadoOrderByPrecioNoche(String estado);

    // bloquea la fila
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Publicacion p where p.id = :id")
    Optional<Publicacion> buscarParaReservar(@Param("id") Long id);

    // conteo para reportes
    @Query("select p.estado, count(p) from Publicacion p group by p.estado order by p.estado")
    List<Object[]> contarPorEstado();

    @Query("select p.ciudad, count(p) from Publicacion p group by p.ciudad order by p.ciudad")
    List<Object[]> contarPorCiudad();

    @Query("select p.tipoAlojamiento, count(p) from Publicacion p group by p.tipoAlojamiento order by p.tipoAlojamiento")
    List<Object[]> contarPorTipo();

    // activas sin reservas
    @Query("""
            select p from Publicacion p
            where p.estado = 'ACTIVA'
              and not exists (select r.id from Reserva r where r.publicacion = p and r.estado in :estados)
            order by p.ciudad, p.titulo
            """)
    List<Publicacion> buscarSinReservas(@Param("estados") java.util.Collection<co.edu.unbosque.model.reservas.EstadoReserva> estados);
}
