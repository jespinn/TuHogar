package co.edu.unbosque.usuarios.service;

import co.edu.unbosque.comun.ApiException;
import co.edu.unbosque.seguridad.JwtUtil;
import co.edu.unbosque.usuarios.dto.*;
import co.edu.unbosque.model.usuarios.CodigoRecuperacion;
import co.edu.unbosque.model.usuarios.Rol;
import co.edu.unbosque.model.usuarios.Usuario;
import co.edu.unbosque.usuarios.repository.CodigoRecuperacionRepository;
import co.edu.unbosque.usuarios.repository.RolRepository;
import co.edu.unbosque.usuarios.repository.UsuarioRepository;
import co.edu.unbosque.alojamientos.repository.PublicacionRepository;
import co.edu.unbosque.model.publicaciones.Publicacion;
import co.edu.unbosque.model.reservas.EstadoReserva;
import co.edu.unbosque.reservas.repository.ReservaRepository;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final CodigoRecuperacionRepository codigos;
    private final PasswordEncoder codificador;
    private final JwtUtil jwtUtil;
    private final int minutosCodigo; // vigencia del código
    private final ReservaRepository reservas;
    private final PublicacionRepository publicaciones;

    public UsuarioService(UsuarioRepository usuarios, RolRepository roles, CodigoRecuperacionRepository codigos,
                          PasswordEncoder codificador, JwtUtil jwtUtil,
                          @Value("${app.codigo.minutos:30}") int minutosCodigo,
                          ReservaRepository reservas, PublicacionRepository publicaciones) {
        this.reservas = reservas;
        this.publicaciones = publicaciones;
        this.usuarios = usuarios;
        this.roles = roles;
        this.codigos = codigos;
        this.codificador = codificador;
        this.jwtUtil = jwtUtil;
        this.minutosCodigo = minutosCodigo;
    }

    // registro

    @Transactional
    public UsuarioDTO registrar(RegistroDTO dto) {
        validarRegistro(dto);
        Usuario usuario = new Usuario();
        usuario.setNombre(dto.nombre().trim());
        usuario.setCorreo(dto.correo().trim().toLowerCase());
        usuario.setContrasenaHash(codificador.encode(dto.contrasena()));
        usuario.setTipoDocumento(dto.tipoDocumento());
        usuario.setNumeroDocumento(dto.numeroDocumento().trim());
        usuario.setAceptaTratamientoDatos(true);
        for (String nombreRol : dto.roles()) {
            usuario.getRoles().add(roles.findByNombre(nombreRol).orElseThrow());
        }
        return convertirADTO(usuarios.save(usuario));
    }

    private void validarRegistro(RegistroDTO dto) {
        if (!dto.contrasena().equals(dto.confirmacion())) {
            throw ApiException.crearInvalida("CONTRASENAS_DIFERENTES", "Las contraseñas no coinciden", "confirmacion");
        }
        if (!dto.aceptaTratamientoDatos()) {
            throw ApiException.crearInvalida("DATOS_NO_ACEPTADOS", "Debe aceptar el tratamiento de datos", "aceptaTratamientoDatos");
        }
        // roles permitidos
        for (String rol : dto.roles()) {
            if (!rol.equals("HUESPED") && !rol.equals("ANFITRION")) {
                throw ApiException.crearInvalida("ROL_INVALIDO", "Solo puede elegir Huésped o Anfitrión", "roles");
            }
        }
        if (usuarios.existsByCorreo(dto.correo().trim().toLowerCase())) {
            throw ApiException.crearConflicto("CORREO_REPETIDO", "Ya existe una cuenta con ese correo", "correo");
        }
        if (usuarios.existsByNumeroDocumento(dto.numeroDocumento().trim())) {
            throw ApiException.crearConflicto("DOCUMENTO_REPETIDO", "Ya existe una cuenta con ese documento", "numeroDocumento");
        }
    }

    // sesión

    public SesionDTO iniciarSesion(LoginDTO dto) {
        Usuario usuario = autenticar(dto);
        rechazarSiRestringida(usuario);
        if (!usuario.estaActiva()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "CUENTA_INACTIVA",
                    "Su cuenta está inactiva. Puede reactivarla.", null);
        }
        return crearSesion(usuario);
    }

    /** reactiva e inicia sesión */
    @Transactional
    public SesionDTO reactivarCuenta(LoginDTO dto) {
        Usuario usuario = autenticar(dto);
        rechazarSiRestringida(usuario); // solo el admin la levanta
        usuario.reactivar();
        return crearSesion(usuario);
    }

    // HU-02 CA4
    private void rechazarSiRestringida(Usuario usuario) {
        if (usuario.estaRestringida()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "CUENTA_RESTRINGIDA",
                    "Su cuenta fue restringida por el administrador", null);
        }
    }

    // mensaje genérico
    private Usuario autenticar(LoginDTO dto) {
        Usuario usuario = usuarios.findByCorreo(dto.correo().trim().toLowerCase()).orElse(null);
        if (usuario == null || !codificador.matches(dto.contrasena(), usuario.getContrasenaHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "CREDENCIALES_INVALIDAS",
                    "Correo o contraseña incorrectos", null);
        }
        return usuario;
    }

    private SesionDTO crearSesion(Usuario usuario) {
        UsuarioDTO dto = convertirADTO(usuario);
        return new SesionDTO(jwtUtil.generar(usuario.getCorreo(), dto.roles()), dto);
    }

    // recuperar contraseña

    /** código simulado en pantalla; null si el correo no existe (CU-04 A1) */
    @Transactional
    public CodigoRecuperacion solicitarRecuperacion(RecuperarDTO dto) {
        Usuario usuario = usuarios.findByCorreo(dto.correo().trim().toLowerCase()).orElse(null);
        if (usuario == null) {
            return null;
        }
        CodigoRecuperacion codigo = new CodigoRecuperacion();
        codigo.setUsuario(usuario);
        codigo.setCodigo(String.format("%06d", new SecureRandom().nextInt(1000000)));
        codigo.setExpiraEn(LocalDateTime.now().plusMinutes(minutosCodigo));
        return codigos.save(codigo);
    }

    @Transactional
    public void restablecer(RestablecerDTO dto) {
        if (!dto.contrasena().equals(dto.confirmacion())) {
            throw ApiException.crearInvalida("CONTRASENAS_DIFERENTES", "Las contraseñas no coinciden", "confirmacion");
        }
        // correo desconocido: mismo error que código malo
        Usuario usuario = usuarios.findByCorreo(dto.correo().trim().toLowerCase())
                .orElseThrow(() -> ApiException.crearInvalida("CODIGO_INVALIDO", "El código no es válido", "codigo"));
        CodigoRecuperacion codigo = codigos.findFirstByUsuarioAndCodigoOrderByIdDesc(usuario, dto.codigo())
                .orElseThrow(() -> ApiException.crearInvalida("CODIGO_INVALIDO", "El código no es válido", "codigo"));
        if (codigo.isUsado()) {
            throw ApiException.crearInvalida("CODIGO_USADO", "Este código ya fue utilizado", "codigo");
        }
        if (!codigo.estaVigente()) {
            throw ApiException.crearInvalida("CODIGO_VENCIDO", "El código venció, solicite uno nuevo", "codigo");
        }
        usuario.setContrasenaHash(codificador.encode(dto.contrasena()));
        codigo.setUsado(true);
    }

    // perfil

    @Transactional(readOnly = true)
    public UsuarioDTO verPerfil(String correo) {
        return convertirADTO(buscarPorCorreo(correo));
    }

    @Transactional
    public UsuarioDTO actualizarPerfil(String correo, PerfilDTO dto) {
        Usuario usuario = buscarPorCorreo(correo);
        String documento = dto.numeroDocumento().trim();
        if (usuarios.existsByNumeroDocumentoAndIdNot(documento, usuario.getId())) {
            throw ApiException.crearConflicto("DOCUMENTO_REPETIDO", "Ya existe una cuenta con ese documento", "numeroDocumento");
        }
        usuario.actualizarPerfil(dto.nombre().trim(), dto.tipoDocumento(), documento);
        return convertirADTO(usuario);
    }

    /** huésped o anfitrión */
    @Transactional
    public UsuarioDTO desactivarMiCuenta(String correo) {
        Usuario usuario = buscarPorCorreo(correo);
        boolean esAdministrador = usuario.getRoles().stream().anyMatch(r -> r.getNombre().equals("ADMINISTRADOR"));
        if (esAdministrador) {
            throw ApiException.crearConflicto("CUENTA_ADMINISTRADOR", "El administrador no puede desactivar su cuenta aquí", null);
        }
        // HU-06 CA2: reservas futuras confirmadas
        LocalDate hoy = LocalDate.now();
        boolean comoHuesped = reservas.existsByHuespedIdAndEstadoAndFechaLlegadaGreaterThanEqual(
                usuario.getId(), EstadoReserva.CONFIRMADA, hoy);
        boolean comoAnfitrion = reservas.existsByPublicacionAnfitrionIdAndEstadoAndFechaLlegadaGreaterThanEqual(
                usuario.getId(), EstadoReserva.CONFIRMADA, hoy);
        if (comoHuesped || comoAnfitrion) {
            throw ApiException.crearConflicto("RESERVAS_FUTURAS",
                    "No puede desactivar su cuenta porque tiene reservas futuras confirmadas", null);
        }
        usuario.desactivar();
        // HU-06 CA3: sus publicaciones activas quedan pausadas
        for (Publicacion p : publicaciones.findByAnfitrionIdAndEstado(usuario.getId(), "ACTIVA")) {
            p.pausar();
        }
        return convertirADTO(usuario);
    }

    // administración de cuentas

    @Transactional(readOnly = true)
    public List<UsuarioDTO> listarUsuarios() {
        return usuarios.findAll().stream().sorted((a, b) -> a.getId().compareTo(b.getId())).map(this::convertirADTO).toList();
    }

    /** solo administrador */
    @Transactional
    public UsuarioDTO desactivarCuenta(String correoAdministrador, Long id) {
        Usuario cuenta = buscarPorId(id);
        if (cuenta.getCorreo().equals(correoAdministrador)) {
            throw ApiException.crearConflicto("CUENTA_PROPIA", "No puede desactivar su propia cuenta", null);
        }
        boolean esAdministrador = cuenta.getRoles().stream().anyMatch(r -> r.getNombre().equals("ADMINISTRADOR"));
        if (esAdministrador && cuenta.estaActiva()
                && usuarios.countByRolesNombreAndEstado("ADMINISTRADOR", "ACTIVA") <= 1) {
            throw ApiException.crearConflicto("ULTIMO_ADMINISTRADOR",
                    "No se puede desactivar al último administrador activo", null);
        }
        cuenta.restringir(); // RF-21
        return convertirADTO(cuenta);
    }

    /** solo administrador */
    @Transactional
    public UsuarioDTO activarCuenta(Long id) {
        Usuario cuenta = buscarPorId(id);
        cuenta.reactivar();
        return convertirADTO(cuenta);
    }

    // apoyo

    private Usuario buscarPorCorreo(String correo) {
        return usuarios.findByCorreo(correo.trim().toLowerCase()).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "USUARIO_NO_ENCONTRADO", "No existe una cuenta con ese correo", "correo"));
    }

    private Usuario buscarPorId(Long id) {
        return usuarios.findById(id).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "USUARIO_NO_ENCONTRADO", "No existe esa cuenta", null));
    }

    private UsuarioDTO convertirADTO(Usuario u) {
        List<String> nombresRoles = u.getRoles().stream().map(Rol::getNombre).sorted().toList();
        return new UsuarioDTO(u.getId(), u.getNombre(), u.getCorreo(), u.getTipoDocumento(), u.getNumeroDocumento(),
                u.getEstado(), nombresRoles, u.getFechaRegistro());
    }
}
