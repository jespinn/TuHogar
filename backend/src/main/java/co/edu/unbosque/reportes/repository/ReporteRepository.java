package co.edu.unbosque.reportes.repository;

import co.edu.unbosque.model.administracion.Reporte;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReporteRepository extends JpaRepository<Reporte, Long> {
    List<Reporte> findByAdministradorCorreoOrderByIdDesc(String correo);
}
