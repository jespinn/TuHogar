package co.edu.unbosque.reservas.controller;

import co.edu.unbosque.reservas.dto.*;
import co.edu.unbosque.reservas.service.ReservaService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** controlador de reservas */
@RestController
@RequestMapping("/api/reservas")
public class ReservaController {
    private final ReservaService servicio;

    public ReservaController(ReservaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/alojamientos")
    public List<AlojamientoResumenDTO> buscarAlojamientos() {
        return servicio.buscarAlojamientos();
    }

    @GetMapping("/alojamientos/{id}")
    public AlojamientoDetalleDTO consultarAlojamiento(@PathVariable Long id) {
        return servicio.consultarAlojamiento(id);
    }

    @PostMapping("/cotizar")
    public CotizacionDTO cotizar(@Valid @RequestBody SolicitudReservaDTO dto) {
        return servicio.cotizar(dto);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservaDTO reservar(Authentication auth, @Valid @RequestBody SolicitudReservaDTO dto) {
        return servicio.reservar(auth.getName(), dto);
    }

    @GetMapping("/mias")
    public List<ReservaDTO> consultarMisReservas(Authentication auth) {
        return servicio.consultarMisReservas(auth.getName());
    }

    @GetMapping("/de-mis-publicaciones")
    public List<ReservaDTO> consultarReservasDeMisPublicaciones(Authentication auth) {
        return servicio.consultarReservasDeMisPublicaciones(auth.getName());
    }

    // todas las reservas
    @GetMapping("/todas")
    public List<ReservaAdminDTO> consultarTodasLasReservas(@RequestParam(required = false) String estado,
                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                       @RequestParam(required = false) Long publicacion) {
        return servicio.consultarTodasLasReservas(estado, desde, hasta, publicacion);
    }

    @PutMapping("/{id}/aprobar")
    public ReservaDTO aprobar(Authentication auth, @PathVariable Long id) {
        return servicio.aprobar(auth.getName(), id);
    }

    @PutMapping("/{id}/rechazar")
    public ReservaDTO rechazar(Authentication auth, @PathVariable Long id, @Valid @RequestBody RechazarDTO dto) {
        return servicio.rechazar(auth.getName(), id, dto);
    }

    @PostMapping("/{id}/pagar")
    public ReservaDTO pagar(Authentication auth, @PathVariable Long id, @Valid @RequestBody PagoDTO dto) {
        return servicio.pagar(auth.getName(), id, dto);
    }

    @GetMapping("/{id}/devolucion")
    public DevolucionDTO consultarDevolucion(Authentication auth, @PathVariable Long id) {
        return servicio.consultarDevolucion(auth.getName(), id);
    }

    // comprobante en pdf
    @GetMapping("/{id}/comprobante")
    public ResponseEntity<byte[]> descargarComprobante(Authentication auth, @PathVariable Long id) {
        byte[] pdf = servicio.generarComprobante(auth.getName(), id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"comprobante-reserva-" + id + ".pdf\"")
                .body(pdf);
    }

    @PutMapping("/{id}/cancelar")
    public ReservaDTO cancelar(Authentication auth, @PathVariable Long id, @Valid @RequestBody CancelarDTO dto) {
        return servicio.cancelar(auth.getName(), id, dto);
    }
}
