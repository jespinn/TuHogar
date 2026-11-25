package co.edu.unbosque;

import static org.junit.jupiter.api.Assertions.*;

import co.edu.unbosque.comun.ApiException;
import co.edu.unbosque.reportes.dto.IndicadoresDTO;
import co.edu.unbosque.reportes.repository.ReporteRepository;
import co.edu.unbosque.reportes.service.ReporteService;
import co.edu.unbosque.model.administracion.Reporte;
import co.edu.unbosque.model.reservas.Reserva;
import co.edu.unbosque.reservas.repository.ReservaRepository;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test") // base alojamientos_test en PostgreSQL
@Transactional
class ReporteServiceTest {
    @Autowired ReporteService servicio;
    @Autowired ReporteRepository reportes;
    @Autowired ReservaRepository reservas;

    @Test
    void losTotalesCoincidenConLosDatosDeEjemplo() {
        IndicadoresDTO datos = servicio.consultarIndicadores();

        assertEquals(1L, datos.usuariosPorRol().get("ADMINISTRADOR"));
        assertEquals(2L, datos.usuariosPorRol().get("ANFITRION"));
        assertEquals(2L, datos.usuariosPorRol().get("HUESPED"));
        assertEquals(5L, datos.usuariosPorEstado().get("ACTIVA"));
        assertEquals(12L, datos.publicacionesPorEstado().get("ACTIVA"));
        assertEquals(2L, datos.publicacionesPorEstado().get("BORRADOR"));
        assertEquals(1L, datos.publicacionesPorEstado().get("BLOQUEADA"));

        // totales de ejemplo
        assertEquals(4L, datos.reservasPorEstado().get("CONFIRMADA"));
        assertEquals(6L, datos.reservasPorEstado().get("COMPLETADA"));
        assertEquals(2L, datos.reservasPorEstado().get("CANCELADA"));
        assertEquals(0, new java.math.BigDecimal("8212000").compareTo(datos.ingresosEstimados()));

        // reservas por periodo
        assertEquals(3L, datos.reservasRecientes().get("Hoy"));
        assertEquals(9L, datos.reservasRecientes().get("Últimos 7 días"));
        assertEquals(16L, datos.reservasRecientes().get("Últimos 30 días"));

    }

    @Test
    void losReportesDeReservasUsanSoloConfirmadasYCompletadas() {
        IndicadoresDTO datos = servicio.consultarIndicadores();

        // reservas válidas
        assertEquals(10, datos.ingresosPorReserva().size());
        assertEquals(4L, datos.reservasPorTipo().get("Apartamento"));
        assertEquals(1L, datos.reservasPorTipo().get("Cabaña"));
        assertEquals(2L, datos.reservasPorTipo().get("Hotel"));
        assertEquals(2L, datos.reservasPorCiudad().get("Bogotá"));
        assertEquals(2L, datos.reservasPorCiudad().get("Santa Marta"));
        assertEquals(10L, datos.reservasPorMes().values().stream().mapToLong(Long::longValue).sum());

        // total de noches
        long noches = datos.nochesFinDeSemana().values().stream().mapToLong(Long::longValue).sum();
        assertEquals(26L, noches);
        assertTrue(datos.fechasMasFrecuentes().size() <= 5);

        // alojamientos sin reservas
        assertEquals(3, datos.alojamientosSinReservas().size());
    }

    @Test
    void exportarExcelTieneContenidoYRegistraQuienYCuando() throws Exception {
        long antes = reportes.count();
        byte[] archivo = servicio.exportar("admin@alojamientos.co", "publicaciones", "xlsx");

        // revisa el excel
        try (XSSFWorkbook libro = new XSSFWorkbook(new ByteArrayInputStream(archivo))) {
            assertEquals("Publicaciones por estado", libro.getSheetAt(0).getSheetName());
            assertEquals("ACTIVA", libro.getSheetAt(0).getRow(1).getCell(0).getStringCellValue());
            assertEquals(12.0, libro.getSheetAt(0).getRow(1).getCell(1).getNumericCellValue());
        }

        assertEquals(antes + 1, reportes.count());
        Reporte ultimo = reportes.findByAdministradorCorreoOrderByIdDesc("admin@alojamientos.co").get(0);
        assertEquals("PUBLICACIONES", ultimo.getTipo());
        assertEquals("XLSX", ultimo.getFormato());
        assertEquals("admin@alojamientos.co", ultimo.getAdministrador().getCorreo());
        assertNotNull(ultimo.getFechaSolicitud());
    }

    @Test
    void exportarPdfTieneContenidoYRegistraQuienYCuando() {
        byte[] archivo = servicio.exportar("admin@alojamientos.co", "demanda", "pdf");

        // es un pdf
        assertTrue(archivo.length > 100);
        assertEquals("%PDF", new String(archivo, 0, 4));

        Reporte ultimo = reportes.findByAdministradorCorreoOrderByIdDesc("admin@alojamientos.co").get(0);
        assertEquals("DEMANDA", ultimo.getTipo());
        assertEquals("PDF", ultimo.getFormato());
        assertNotNull(ultimo.getFechaSolicitud());
    }

    @Test
    void sinInformacionNoSeGeneraElReporte() {
        // sin ingresos
        for (Reserva r : reservas.findAll()) {
            r.cancelar("Prueba sin información");
        }
        long antes = reportes.count();

        ApiException error = assertThrows(ApiException.class, () -> servicio.exportar("admin@alojamientos.co", "INGRESOS", "PDF"));
        assertEquals("SIN_INFORMACION", error.getCodigo());
        assertEquals("No hay información para el reporte", error.getMessage());
        assertEquals(antes, reportes.count()); // nada registrado
    }

    @Test
    void unTipoOFormatoInvalidoSeRechaza() {
        assertThrows(ApiException.class, () -> servicio.exportar("admin@alojamientos.co", "NOEXISTE", "PDF"));
        ApiException error = assertThrows(ApiException.class, () -> servicio.exportar("admin@alojamientos.co", "RESERVAS", "CSV"));
        assertEquals("FORMATO_INVALIDO", error.getCodigo());
    }

    @Test
    void elFiltroDeFechasRecalculaYAvisaSiNoHayDatos() {
        // HU-32 CA2 y CA3: un rango lejano no tiene reservas ni usuarios
        LocalDate lejos = LocalDate.now().plusYears(5);
        IndicadoresDTO vacio = servicio.consultarIndicadores(lejos, lejos.plusDays(10));
        assertTrue(vacio.reservasPorEstado().isEmpty());
        assertEquals(0, vacio.ingresosEstimados().signum());
        assertNotNull(vacio.aviso());

        // sin filtro no hay aviso
        assertNull(servicio.consultarIndicadores().aviso());

        // rango al revés
        ApiException error = assertThrows(ApiException.class, () -> servicio.consultarIndicadores(lejos, lejos.minusDays(1)));
        assertEquals("RANGO_INVALIDO", error.getCodigo());
    }
}
