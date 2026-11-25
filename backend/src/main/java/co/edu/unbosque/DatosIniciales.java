package co.edu.unbosque;

import co.edu.unbosque.model.publicaciones.PeriodoDisponible;
import co.edu.unbosque.model.publicaciones.Publicacion;
import co.edu.unbosque.alojamientos.repository.PublicacionRepository;
import co.edu.unbosque.model.usuarios.Usuario;
import co.edu.unbosque.usuarios.repository.UsuarioRepository;
import java.time.LocalDate;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** completa datos iniciales */
@Component
public class DatosIniciales implements ApplicationRunner {
    private final UsuarioRepository usuarios;
    private final PublicacionRepository publicaciones;
    private final PasswordEncoder codificador;

    public DatosIniciales(UsuarioRepository usuarios, PublicacionRepository publicaciones, PasswordEncoder codificador) {
        this.usuarios = usuarios;
        this.publicaciones = publicaciones;
        this.codificador = codificador;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        cifrarContrasenas();
        crearDisponibilidad();
    }

    private void cifrarContrasenas() {
        for (Usuario usuario : usuarios.findByContrasenaHash("PENDIENTE")) {
            String clave = usuario.getCorreo().startsWith("admin") ? "Admin1234" : "Clave1234";
            usuario.setContrasenaHash(codificador.encode(clave));
        }
    }

    private void crearDisponibilidad() {
        LocalDate hoy = LocalDate.now();
        for (Publicacion p : publicaciones.findByEstado("ACTIVA")) {
            if (!p.getPeriodos().isEmpty()) continue;
            p.getPeriodos().add(new PeriodoDisponible(p, hoy.plusDays(1), hoy.plusDays(60)));
            if (p.getId() % 2 == 0) { // segundo periodo disponible
                p.getPeriodos().add(new PeriodoDisponible(p, hoy.plusDays(90), hoy.plusDays(150)));
            }
        }
    }
}
