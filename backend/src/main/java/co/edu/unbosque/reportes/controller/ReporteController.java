package co.edu.unbosque.reportes.controller;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import co.edu.unbosque.reportes.dto.IndicadoresDTO;
import co.edu.unbosque.reportes.service.ReporteService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** solo administrador */
@RestController
@RequestMapping("/api/reportes")
public class ReporteController {
    private final ReporteService servicio;

    public ReporteController(ReporteService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/indicadores")
    public IndicadoresDTO consultarIndicadores(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return servicio.consultarIndicadores(desde, hasta);
    }

    // xlsx o pdf
    @GetMapping("/exportar")
    public ResponseEntity<byte[]> exportar(Authentication auth, @RequestParam(defaultValue = "RESERVAS") String tipo,
                                           @RequestParam(defaultValue = "PDF") String formato,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        byte[] archivo = servicio.exportar(auth.getName(), tipo, formato, desde, hasta); // valida y genera
        MediaType contenido;
        String extension;
        if (formato.equalsIgnoreCase("XLSX")) {
            contenido = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            extension = ".xlsx";
        } else {
            contenido = MediaType.APPLICATION_PDF;
            extension = ".pdf";
        }
        return ResponseEntity.ok()
                .contentType(contenido)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte-" + tipo.toLowerCase() + extension + "\"")
                .body(archivo);
    }
}
