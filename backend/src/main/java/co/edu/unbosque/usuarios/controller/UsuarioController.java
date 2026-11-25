package co.edu.unbosque.usuarios.controller;

import co.edu.unbosque.model.usuarios.CodigoRecuperacion;
import co.edu.unbosque.usuarios.dto.*;
import co.edu.unbosque.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** controlador de usuarios */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioService servicio;

    public UsuarioController(UsuarioService servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioDTO registrar(@Valid @RequestBody RegistroDTO dto) {
        return servicio.registrar(dto);
    }

    @PostMapping("/login")
    public SesionDTO iniciarSesion(@Valid @RequestBody LoginDTO dto) {
        return servicio.iniciarSesion(dto);
    }

    @PostMapping("/recuperar")
    public Map<String, String> recuperar(@Valid @RequestBody RecuperarDTO dto) {
        // código simulado
        CodigoRecuperacion codigo = servicio.solicitarRecuperacion(dto);
        if (codigo == null) {
            // no revela si el correo existe
            return Map.of("mensaje", "Si el correo está registrado, se generó un código de recuperación.");
        }
        return Map.of("mensaje", "Código generado. Vence en 30 minutos y sirve una sola vez.",
                "codigo", codigo.getCodigo(), "expiraEn", codigo.getExpiraEn().toString());
    }

    @PostMapping("/reactivar")
    public SesionDTO reactivarCuenta(@Valid @RequestBody LoginDTO dto) {
        return servicio.reactivarCuenta(dto);
    }

    @PostMapping("/restablecer")
    public Map<String, String> restablecer(@Valid @RequestBody RestablecerDTO dto) {
        servicio.restablecer(dto);
        return Map.of("mensaje", "Contraseña actualizada. Ya puede iniciar sesión.");
    }

    @GetMapping("/perfil")
    public UsuarioDTO verPerfil(Authentication auth) {
        return servicio.verPerfil(auth.getName());
    }

    @PutMapping("/perfil")
    public UsuarioDTO actualizarPerfil(Authentication auth, @Valid @RequestBody PerfilDTO dto) {
        return servicio.actualizarPerfil(auth.getName(), dto);
    }

    @PutMapping("/perfil/desactivacion")
    public UsuarioDTO desactivarMiCuenta(Authentication auth) {
        return servicio.desactivarMiCuenta(auth.getName());
    }

    // solo administrador (securityconfig)
    @GetMapping
    public List<UsuarioDTO> listarUsuarios() {
        return servicio.listarUsuarios();
    }

    @PutMapping("/{id}/desactivar")
    public UsuarioDTO desactivarCuenta(Authentication auth, @PathVariable Long id) {
        return servicio.desactivarCuenta(auth.getName(), id);
    }

    @PutMapping("/{id}/activar")
    public UsuarioDTO activarCuenta(@PathVariable Long id) {
        return servicio.activarCuenta(id);
    }
}
