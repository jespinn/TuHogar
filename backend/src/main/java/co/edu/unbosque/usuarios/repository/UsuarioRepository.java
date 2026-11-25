package co.edu.unbosque.usuarios.repository;

import co.edu.unbosque.model.usuarios.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    boolean existsByNumeroDocumento(String numeroDocumento);

    boolean existsByNumeroDocumentoAndIdNot(String numeroDocumento, Long id);

    List<Usuario> findByContrasenaHash(String contrasenaHash);

    // administradores activos
    long countByRolesNombreAndEstado(String rol, String estado);

    // conteo para reportes
    @Query("select r.nombre, count(u) from Usuario u join u.roles r group by r.nombre order by r.nombre")
    List<Object[]> contarPorRol();

    @Query("select u.estado, count(u) from Usuario u group by u.estado order by u.estado")
    List<Object[]> contarPorEstado();
}
