package co.edu.unbosque;

import static org.junit.jupiter.api.Assertions.*;

import co.edu.unbosque.alojamientos.repository.PublicacionRepository;
import co.edu.unbosque.comun.ApiException;
import co.edu.unbosque.usuarios.dto.*;
import co.edu.unbosque.model.usuarios.CodigoRecuperacion;
import co.edu.unbosque.model.usuarios.Usuario;
import co.edu.unbosque.usuarios.repository.CodigoRecuperacionRepository;
import co.edu.unbosque.usuarios.repository.UsuarioRepository;
import co.edu.unbosque.usuarios.service.UsuarioService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("demo")
@Transactional // deshace los cambios
class UsuarioServiceTest {
    @Autowired UsuarioService servicio;
    @Autowired UsuarioRepository usuarios;
    @Autowired CodigoRecuperacionRepository codigos;
    @Autowired PublicacionRepository publicaciones;

    private RegistroDTO registrar(String correo, String documento, List<String> roles) {
        return new RegistroDTO("Nuevo Usuario", correo, "Clave1234", "Clave1234", "CC", documento, roles, true);
    }

    // el código llega al correo; la prueba lo lee de la base
    private String codigoDe(String correo) {
        servicio.solicitarRecuperacion(new RecuperarDTO(correo));
        List<CodigoRecuperacion> todos = codigos.findAll();
        return todos.get(todos.size() - 1).getCodigo();
    }

    @Test
    void registrarGuardaElHashYNoLaContrasena() {
        UsuarioDTO creado = servicio.registrar(registrar("nuevo@correo.co", "555", List.of("HUESPED", "ANFITRION")));

        assertEquals(List.of("ANFITRION", "HUESPED"), creado.roles());
        Usuario guardado = usuarios.findByCorreo("nuevo@correo.co").orElseThrow();
        assertTrue(guardado.getContrasenaHash().startsWith("$2")); // BCrypt
        assertNotEquals("Clave1234", guardado.getContrasenaHash());
    }

    @Test
    void registrarConCorreoRepetidoFalla() {
        ApiException error = assertThrows(ApiException.class,
                () -> servicio.registrar(registrar("maria.huesped@correo.co", "556", List.of("HUESPED"))));
        assertEquals("CORREO_REPETIDO", error.getCodigo());
    }

    @Test
    void noSePuedeRegistrarComoAdministrador() {
        ApiException error = assertThrows(ApiException.class,
                () -> servicio.registrar(registrar("falso@correo.co", "557", List.of("ADMINISTRADOR"))));
        assertEquals("ROL_INVALIDO", error.getCodigo());
    }

    @Test
    void recuperarConCorreoNoRegistradoNoRevelaNada() {
        long antes = codigos.count();

        assertNull(servicio.solicitarRecuperacion(new RecuperarDTO("nadie@correo.co"))); // CU-04 A1
        assertEquals(antes, codigos.count()); // no se guarda
    }

    @Test
    void noSeDesactivaConReservasFuturasConfirmadas() {
        // maría tiene una reserva confirmada en 10 días (data.sql)
        ApiException error = assertThrows(ApiException.class,
                () -> servicio.desactivarMiCuenta("maria.huesped@correo.co"));
        assertEquals("RESERVAS_FUTURAS", error.getCodigo());
        assertEquals("ACTIVA", usuarios.findByCorreo("maria.huesped@correo.co").orElseThrow().getEstado());
    }

    @Test
    void alDesactivarElAnfitrionSusPublicacionesQuedanPausadas() {
        Long laura = usuarios.findByCorreo("laura.anfitriona@correo.co").orElseThrow().getId();
        assertFalse(publicaciones.findByAnfitrionIdAndEstado(laura, "ACTIVA").isEmpty());

        servicio.desactivarMiCuenta("laura.anfitriona@correo.co");

        assertTrue(publicaciones.findByAnfitrionIdAndEstado(laura, "ACTIVA").isEmpty());
        assertFalse(publicaciones.findByAnfitrionIdAndEstado(laura, "PAUSADA").isEmpty());
    }

    @Test
    void elHuespedDesactivaYReactivaSuCuenta() {
        servicio.registrar(registrar("sinreservas@correo.co", "558", List.of("HUESPED"))); // sin reservas
        LoginDTO credenciales = new LoginDTO("sinreservas@correo.co", "Clave1234");

        assertEquals("INACTIVA", servicio.desactivarMiCuenta("sinreservas@correo.co").estado());
        ApiException error = assertThrows(ApiException.class, () -> servicio.iniciarSesion(credenciales));
        assertEquals("CUENTA_INACTIVA", error.getCodigo());

        assertEquals("ACTIVA", servicio.reactivarCuenta(credenciales).usuario().estado());
        assertNotNull(servicio.iniciarSesion(credenciales).token()); // ahora entra
    }

    @Test
    void elAdministradorDesactivaYActivaUnaCuenta() {
        Long pedro = usuarios.findByCorreo("pedro.huesped@correo.co").orElseThrow().getId();
        LoginDTO credenciales = new LoginDTO("pedro.huesped@correo.co", "Clave1234");

        assertEquals("RESTRINGIDA", servicio.desactivarCuenta("admin@alojamientos.co", pedro).estado());
        ApiException error = assertThrows(ApiException.class, () -> servicio.iniciarSesion(credenciales));
        assertEquals("CUENTA_RESTRINGIDA", error.getCodigo()); // HU-02 CA4
        error = assertThrows(ApiException.class, () -> servicio.reactivarCuenta(credenciales));
        assertEquals("CUENTA_RESTRINGIDA", error.getCodigo()); // no se reactiva solo

        assertEquals("ACTIVA", servicio.activarCuenta(pedro).estado());
        assertNotNull(servicio.iniciarSesion(credenciales).token()); // ahora entra
    }

    @Test
    void noSeDesactivaLaPropiaCuentaNiElUltimoAdministrador() {
        Long admin = usuarios.findByCorreo("admin@alojamientos.co").orElseThrow().getId();
        ApiException propia = assertThrows(ApiException.class,
                () -> servicio.desactivarCuenta("admin@alojamientos.co", admin));
        assertEquals("CUENTA_PROPIA", propia.getCodigo());

        // otro admin intenta desactivar al único admin activo
        ApiException ultimo = assertThrows(ApiException.class,
                () -> servicio.desactivarCuenta("otro@correo.co", admin));
        assertEquals("ULTIMO_ADMINISTRADOR", ultimo.getCodigo());
    }

    @Test
    void codigoVencidoSeRechaza() {
        Usuario maria = usuarios.findByCorreo("maria.huesped@correo.co").orElseThrow();
        CodigoRecuperacion vencido = new CodigoRecuperacion();
        vencido.setUsuario(maria);
        vencido.setCodigo("111111");
        vencido.setExpiraEn(LocalDateTime.now().minusMinutes(1));
        codigos.save(vencido);

        ApiException error = assertThrows(ApiException.class, () -> servicio.restablecer(
                new RestablecerDTO("maria.huesped@correo.co", "111111", "OtraClave99", "OtraClave99")));
        assertEquals("CODIGO_VENCIDO", error.getCodigo());
    }

    @Test
    void codigoUsadoNoSePuedeReutilizar() {
        String codigo = codigoDe("maria.huesped@correo.co");
        LocalDateTime vence = codigos.findAll().get(0).getExpiraEn();
        assertTrue(vence.isAfter(LocalDateTime.now().plusMinutes(29))); // dura 30 minutos
        assertTrue(vence.isBefore(LocalDateTime.now().plusMinutes(31)));
        RestablecerDTO cambio = new RestablecerDTO("maria.huesped@correo.co", codigo, "OtraClave99", "OtraClave99");

        servicio.restablecer(cambio); // primera vez funciona
        ApiException error = assertThrows(ApiException.class, () -> servicio.restablecer(cambio));
        assertEquals("CODIGO_USADO", error.getCodigo());
    }
}
