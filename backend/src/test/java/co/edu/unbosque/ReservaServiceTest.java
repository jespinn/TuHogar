package co.edu.unbosque;

import static org.junit.jupiter.api.Assertions.*;

import co.edu.unbosque.model.publicaciones.Publicacion;
import co.edu.unbosque.alojamientos.repository.PublicacionRepository;
import co.edu.unbosque.comun.ApiException;
import co.edu.unbosque.reservas.dto.*;
import co.edu.unbosque.reservas.repository.ReservaRepository;
import co.edu.unbosque.reservas.service.ReservaService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test") // base alojamientos_test en PostgreSQL
@Transactional
class ReservaServiceTest {
    // datos de laureles
    static final String LAURELES = "Apartamento con vista en Laureles";
    // casa colonial
    static final String COLONIAL = "Casa colonial en San Antonio";
    static final String MARIA = "maria.huesped@correo.co";
    static final String PEDRO = "pedro.huesped@correo.co";
    static final String TARJETA_BUENA = "4242424242424242";
    static final String TARJETA_RECHAZADA = "4111111111110000";

    @Autowired ReservaService servicio;
    @Autowired PublicacionRepository publicaciones;
    @Autowired ReservaRepository reservas;
    @Autowired co.edu.unbosque.reservas.repository.CancelacionRepository cancelaciones;

    private final LocalDate hoy = LocalDate.now();

    private Publicacion publicacion(String titulo) {
        return publicaciones.findAll().stream().filter(p -> p.getTitulo().equals(titulo)).findFirst().orElseThrow();
    }

    // solicitud de prueba
    private SolicitudReservaDTO armarSolicitud(String titulo, int desde, int hasta, String clave) {
        return new SolicitudReservaDTO(publicacion(titulo).getId(), hoy.plusDays(desde), hoy.plusDays(hasta), 2, null, clave);
    }

    private PagoDTO pago(String tarjeta) {
        return new PagoDTO(tarjeta, "Maria Torres", "123");
    }

    private void assertValor(String esperado, BigDecimal real) {
        assertEquals(0, new BigDecimal(esperado).compareTo(real), "esperado " + esperado + " pero fue " + real);
    }

    @Test
    void cotizacionSinDescuento() {
        CotizacionDTO c = servicio.cotizar(armarSolicitud(LAURELES, 5, 7, null)); // dos noches
        assertEquals(2, c.noches());
        assertValor("300000", c.tarifaBase());
        assertValor("25000", c.cargoLimpieza());
        assertValor("30000", c.cargoServicio()); // 10 % de la tarifa base
        assertValor("0", c.descuentos());
        assertValor("355000", c.total());
    }

    @Test
    void cotizacionConDescuentoDesdeSieteNoches() {
        CotizacionDTO c = servicio.cotizar(armarSolicitud(LAURELES, 5, 12, null)); // siete noches
        assertValor("1050000", c.tarifaBase());
        assertValor("105000", c.cargoServicio());
        assertValor("105000", c.descuentos()); // 10 % de la tarifa base
        assertValor("1075000", c.total()); // 1050000 + 25000 + 105000 - 105000
    }

    @Test
    void reservaInmediataQuedaPendientePago() {
        ReservaDTO r = servicio.reservar(MARIA, armarSolicitud(LAURELES, 5, 7, "k-inmediata"));
        assertEquals("PENDIENTE_PAGO", r.estado());
        assertValor("355000", r.total());
        assertTrue(r.puedePagar());
    }

    @Test
    void reservaConAprobacionEsperaAlAnfitrion() {
        ReservaDTO r = servicio.reservar(MARIA, armarSolicitud(COLONIAL, 5, 7, "k-aprobacion"));
        assertEquals("PENDIENTE_APROBACION", r.estado());

        // solo la dueña
        ApiException ajeno = assertThrows(ApiException.class, () -> servicio.aprobar("carlos.anfitrion@correo.co", r.id()));
        assertEquals("SIN_PERMISO", ajeno.getCodigo());
        assertEquals("PENDIENTE_PAGO", servicio.aprobar("laura.anfitriona@correo.co", r.id()).estado());
    }

    @Test
    void fechasOcupadasSeRechazan() {
        ReservaDTO deMaria = servicio.reservar(MARIA, armarSolicitud(LAURELES, 5, 7, "k-maria"));
        servicio.pagar(MARIA, deMaria.id(), pago(TARJETA_BUENA)); // confirmada: bloquea noches

        ApiException error = assertThrows(ApiException.class, () -> servicio.reservar(PEDRO, armarSolicitud(LAURELES, 6, 8, "k-pedro")));
        assertEquals("FECHAS_OCUPADAS", error.getCodigo());

        // cotizar incluye cu-08
        ApiException alCotizar = assertThrows(ApiException.class, () -> servicio.cotizar(armarSolicitud(LAURELES, 6, 8, null)));
        assertEquals("FECHAS_OCUPADAS", alCotizar.getCodigo());

        // salida igual llegada
        assertEquals("PENDIENTE_PAGO", servicio.reservar(PEDRO, armarSolicitud(LAURELES, 7, 9, "k-pedro-2")).estado());
    }

    @Test
    void unaReservaPendienteNoBloqueaHastaQueSeConfirma() {
        // RF-09 y RF-14: dos pendientes en las mismas fechas
        ReservaDTO deMaria = servicio.reservar(MARIA, armarSolicitud(LAURELES, 5, 7, "k-pend-maria"));
        ReservaDTO dePedro = servicio.reservar(PEDRO, armarSolicitud(LAURELES, 5, 7, "k-pend-pedro"));

        servicio.pagar(MARIA, deMaria.id(), pago(TARJETA_BUENA)); // la primera que paga gana
        ApiException error = assertThrows(ApiException.class, () -> servicio.pagar(PEDRO, dePedro.id(), pago(TARJETA_BUENA)));
        assertEquals("FECHAS_OCUPADAS", error.getCodigo());
    }

    @Test
    void noSeApruebaSiOtraReservaYaSeConfirmo() {
        // HU-19 CA3
        ReservaDTO pendiente = servicio.reservar(MARIA, armarSolicitud(COLONIAL, 5, 7, "k-colonial-1"));
        ReservaDTO otra = servicio.reservar(PEDRO, armarSolicitud(COLONIAL, 5, 7, "k-colonial-2"));
        servicio.aprobar("laura.anfitriona@correo.co", otra.id());
        servicio.pagar(PEDRO, otra.id(), pago(TARJETA_BUENA));

        ApiException error = assertThrows(ApiException.class,
                () -> servicio.aprobar("laura.anfitriona@correo.co", pendiente.id()));
        assertEquals("FECHAS_OCUPADAS", error.getCodigo());
    }

    @Test
    void mismaClaveCreaUnaSolaReserva() {
        ReservaDTO primera = servicio.reservar(MARIA, armarSolicitud(LAURELES, 5, 7, "k-repetida"));
        ReservaDTO segunda = servicio.reservar(MARIA, armarSolicitud(LAURELES, 5, 7, "k-repetida"));

        assertEquals(primera.id(), segunda.id());
        assertEquals(1, reservas.findAll().stream().filter(r -> "k-repetida".equals(r.getClaveIdempotencia())).count());
    }

    @Test
    void pagoAprobadoConfirmaLaReserva() {
        ReservaDTO r = servicio.reservar(MARIA, armarSolicitud(LAURELES, 5, 7, "k-pago-ok"));

        ReservaDTO pagada = servicio.pagar(MARIA, r.id(), pago(TARJETA_BUENA));
        assertEquals("CONFIRMADA", pagada.estado());
        assertTrue(pagada.referenciaPago().startsWith("SIM-") && pagada.referenciaPago().endsWith("-4242")); // solo ultimos 4
        assertFalse(pagada.puedePagar());
    }

    @Test
    void pagoRechazadoDejaLaReservaPendiente() {
        ReservaDTO r = servicio.reservar(MARIA, armarSolicitud(LAURELES, 5, 7, "k-pago-mal"));

        ApiException error = assertThrows(ApiException.class, () -> servicio.pagar(MARIA, r.id(), pago(TARJETA_RECHAZADA)));
        assertEquals("PAGO_RECHAZADO", error.getCodigo());
        assertEquals("PENDIENTE_PAGO", servicio.consultarMisReservas(MARIA).get(0).estado());
    }

    @Test
    void cancelacionDevuelveSegunLaPolitica() {
        // HU-24 CA2: 7 días o más, 100 %
        ReservaDTO conTiempo = servicio.reservar(MARIA, armarSolicitud(LAURELES, 20, 22, "k-cancela-1"));
        servicio.pagar(MARIA, conTiempo.id(), pago(TARJETA_BUENA));
        ReservaDTO cancelada = servicio.cancelar(MARIA, conTiempo.id(), new CancelarDTO("Cambio de planes"));
        assertEquals("CANCELADA", cancelada.estado());
        assertValor("355000", cancelada.valorDevolucion()); // 100 % de 355000
        // estado final
        assertEquals(co.edu.unbosque.model.reservas.EstadoReserva.CANCELADA,
                cancelaciones.findByReservaId(conTiempo.id()).get().getEstadoFinal());

        // de 2 a 6 días, 50 %
        ReservaDTO medio = servicio.reservar(MARIA, armarSolicitud(LAURELES, 3, 5, "k-cancela-2"));
        servicio.pagar(MARIA, medio.id(), pago(TARJETA_BUENA));
        assertValor("177500", servicio.cancelar(MARIA, medio.id(), new CancelarDTO("Ya no viajo")).valorDevolucion());

        // menos de 2 días, 0 %
        ReservaDTO sinTiempo = servicio.reservar(MARIA, armarSolicitud(LAURELES, 1, 2, "k-cancela-3"));
        servicio.pagar(MARIA, sinTiempo.id(), pago(TARJETA_BUENA));
        assertValor("0", servicio.cancelar(MARIA, sinTiempo.id(), new CancelarDTO("Ya no viajo")).valorDevolucion());
    }

    @Test
    void laDevolucionSeConsultaAntesDeCancelarSinGuardarNada() {
        ReservaDTO r = servicio.reservar(MARIA, armarSolicitud(LAURELES, 20, 22, "k-devolucion"));
        servicio.pagar(MARIA, r.id(), pago(TARJETA_BUENA));

        assertValor("355000", servicio.consultarDevolucion(MARIA, r.id()).getValorDevolucion()); // 100 %, faltan 20 días
        // solo consulta
        assertTrue(cancelaciones.findByReservaId(r.id()).isEmpty());
        assertEquals("CONFIRMADA", servicio.consultarMisReservas(MARIA).get(0).estado());

        ApiException ajeno = assertThrows(ApiException.class, () -> servicio.consultarDevolucion(PEDRO, r.id()));
        assertEquals("SIN_PERMISO", ajeno.getCodigo());
    }

    @Test
    void elComprobanteLoDescarganElHuespedYElAnfitrion() {
        ReservaDTO r = servicio.reservar(MARIA, armarSolicitud(LAURELES, 5, 7, "k-comprobante"));
        servicio.pagar(MARIA, r.id(), pago(TARJETA_BUENA));

        // es un pdf
        byte[] delHuesped = servicio.generarComprobante(MARIA, r.id());
        assertEquals("%PDF", new String(delHuesped, 0, 4));
        byte[] delAnfitrion = servicio.generarComprobante("carlos.anfitrion@correo.co", r.id()); // Carlos es el anfitrion de Laureles
        assertEquals("%PDF", new String(delAnfitrion, 0, 4));

        // otros no pueden
        ApiException ajeno = assertThrows(ApiException.class, () -> servicio.generarComprobante(PEDRO, r.id()));
        assertEquals("SIN_PERMISO", ajeno.getCodigo());
        ApiException otroAnfitrion = assertThrows(ApiException.class, () -> servicio.generarComprobante("laura.anfitriona@correo.co", r.id()));
        assertEquals("SIN_PERMISO", otroAnfitrion.getCodigo());
    }

    @Test
    void unHuespedAjenoNoPuedePagarNiCancelar() {
        ReservaDTO deMaria = servicio.reservar(MARIA, armarSolicitud(LAURELES, 5, 7, "k-ajena"));

        ApiException alPagar = assertThrows(ApiException.class, () -> servicio.pagar(PEDRO, deMaria.id(), pago(TARJETA_BUENA)));
        assertEquals("SIN_PERMISO", alPagar.getCodigo());
        ApiException alCancelar = assertThrows(ApiException.class, () -> servicio.cancelar(PEDRO, deMaria.id(), new CancelarDTO("x")));
        assertEquals("SIN_PERMISO", alCancelar.getCodigo());
    }
}
