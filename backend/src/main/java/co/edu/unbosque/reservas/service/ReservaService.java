package co.edu.unbosque.reservas.service;

import co.edu.unbosque.model.publicaciones.ImagenPublicacion;
import co.edu.unbosque.model.publicaciones.PoliticaCancelacion;
import co.edu.unbosque.model.publicaciones.Publicacion;
import co.edu.unbosque.model.publicaciones.ServicioAdicional;
import co.edu.unbosque.alojamientos.repository.PublicacionRepository;
import co.edu.unbosque.comun.ApiException;
import co.edu.unbosque.reservas.dto.AlojamientoDetalleDTO;
import co.edu.unbosque.reservas.dto.AlojamientoDetalleDTO.PeriodoDTO;
import co.edu.unbosque.reservas.dto.AlojamientoDetalleDTO.PoliticaDTO;
import co.edu.unbosque.reservas.dto.AlojamientoDetalleDTO.ServicioDTO;
import co.edu.unbosque.reservas.dto.AlojamientoResumenDTO;
import co.edu.unbosque.reservas.dto.CancelarDTO;
import co.edu.unbosque.reservas.dto.CotizacionDTO;
import co.edu.unbosque.reservas.dto.DevolucionDTO;
import co.edu.unbosque.reservas.dto.PagoDTO;
import co.edu.unbosque.reservas.dto.RechazarDTO;
import co.edu.unbosque.reservas.dto.ReservaAdminDTO;
import co.edu.unbosque.reservas.dto.ReservaDTO;
import co.edu.unbosque.reservas.dto.SolicitudReservaDTO;
import co.edu.unbosque.model.reservas.Cancelacion;
import co.edu.unbosque.model.reservas.EstadoReserva;
import co.edu.unbosque.model.reservas.Pago;
import co.edu.unbosque.model.reservas.Reserva;
import co.edu.unbosque.model.reservas.ReservaServicio;
import co.edu.unbosque.reservas.repository.CancelacionRepository;
import co.edu.unbosque.reservas.repository.PagoRepository;
import co.edu.unbosque.reservas.repository.ReservaRepository;
import co.edu.unbosque.model.usuarios.Usuario;
import co.edu.unbosque.usuarios.repository.UsuarioRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservaService {
    private static final BigDecimal PORCENTAJE_CARGO_SERVICIO = new BigDecimal("0.10"); // 10 % de la tarifa base
    private static final BigDecimal PORCENTAJE_DESCUENTO = new BigDecimal("0.10");      // 10 % de la tarifa base
    private static final int NOCHES_PARA_DESCUENTO = 7;
    // RF-09: solo la reserva confirmada bloquea noches
    private static final List<EstadoReserva> OCUPAN = List.of(EstadoReserva.CONFIRMADA);

    private final PublicacionRepository publicaciones;
    private final ReservaRepository reservas;
    private final PagoRepository pagos;
    private final CancelacionRepository cancelaciones;
    private final UsuarioRepository usuarios;
    private final PasarelaSimulada pasarela;

    public ReservaService(PublicacionRepository publicaciones, ReservaRepository reservas, PagoRepository pagos,
                          CancelacionRepository cancelaciones, UsuarioRepository usuarios, PasarelaSimulada pasarela) {
        this.publicaciones = publicaciones;
        this.reservas = reservas;
        this.pagos = pagos;
        this.cancelaciones = cancelaciones;
        this.usuarios = usuarios;
        this.pasarela = pasarela;
    }

    // elegir alojamiento

    /** solo publicaciones activas */
    @Transactional(readOnly = true)
    public List<AlojamientoResumenDTO> buscarAlojamientos() {
        return publicaciones.findByEstadoOrderByPrecioNoche("ACTIVA").stream().map(this::convertirAResumen).toList();
    }

    @Transactional(readOnly = true)
    public AlojamientoDetalleDTO consultarAlojamiento(Long id) {
        return convertirADetalle(buscarActiva(id));
    }

    // cotizar y reservar

    @Transactional(readOnly = true)
    public CotizacionDTO cotizar(SolicitudReservaDTO solicitud) {
        Reserva calculada = armarReserva(null, buscarActiva(solicitud.publicacionId()), solicitud);
        return new CotizacionDTO(calculada.calcularNoches(), calculada.getTarifaBase(), calculada.getCargoLimpieza(),
                sumarCostoServicios(calculada), calculada.getCargoServicio(), calculada.getDescuentos(), calculada.getTotal());
    }

    /** recalcula el total */
    @Transactional
    public ReservaDTO reservar(String correo, SolicitudReservaDTO solicitud) {
        String clave = solicitud.claveIdempotencia();
        if (clave == null || clave.isBlank()) {
            throw ApiException.crearInvalida("CLAVE_REQUERIDA", "Falta la clave de la solicitud", "claveIdempotencia");
        }
        Optional<Reserva> repetida = reservas.findByClaveIdempotencia(clave);
        if (repetida.isPresent()) {
            return responderRepetida(correo, repetida.get());
        }
        Usuario huesped = buscarUsuario(correo);
        Publicacion publicacion = publicaciones.buscarParaReservar(solicitud.publicacionId())
                .filter(p -> p.getEstado().equals("ACTIVA")).orElseThrow(this::crearErrorNoEncontrado);
        if (publicacion.getAnfitrion().getId().equals(huesped.getId())) {
            throw ApiException.crearProhibida("No puede reservar su propia publicación");
        }
        Reserva reserva = armarReserva(huesped, publicacion, solicitud);
        reserva.setClaveIdempotencia(clave);
        reserva.setEstado(publicacion.getModoReserva().equals("INMEDIATA")
                ? EstadoReserva.PENDIENTE_PAGO : EstadoReserva.PENDIENTE_APROBACION);
        return convertirADTO(reservas.save(reserva));
    }

    // aprobar, rechazar, pagar

    @Transactional
    public ReservaDTO aprobar(String correo, Long id) {
        Reserva reserva = buscarReservaDelAnfitrion(correo, id);
        exigir(reserva.puedeResponder(), "Solo se puede aprobar una reserva pendiente de aprobación");
        revisarQueSigueLibre(reserva); // HU-19 CA3
        reserva.aprobar();
        return convertirADTO(reserva);
    }

    @Transactional
    public ReservaDTO rechazar(String correo, Long id, RechazarDTO dto) {
        Reserva reserva = buscarReservaDelAnfitrion(correo, id);
        exigir(reserva.puedeResponder(), "Solo se puede rechazar una reserva pendiente de aprobación");
        reserva.rechazar(dto.motivo()); // motivo no guardado
        return convertirADTO(reserva);
    }

    @Transactional
    public ReservaDTO pagar(String correo, Long id, PagoDTO dto) {
        Reserva reserva = buscarReservaDelHuesped(correo, id);
        exigir(reserva.puedePagar(), "Solo se puede pagar una reserva pendiente de pago");
        publicaciones.buscarParaReservar(reserva.getPublicacion().getId()); // bloquea la fila (RNF-04)
        revisarQueSigueLibre(reserva);
        PasarelaSimulada.Resultado resultado = pasarela.cobrar(dto.numeroTarjeta());
        if (!resultado.aprobado()) { // pago rechazado
            throw ApiException.crearInvalida("PAGO_RECHAZADO", resultado.mensaje(), "numeroTarjeta");
        }
        pagos.save(new Pago(reserva, reserva.getTotal(), resultado.referencia()));
        reserva.confirmar();
        return convertirADTO(reserva);
    }

    @Transactional
    public ReservaDTO cancelar(String correo, Long id, CancelarDTO dto) {
        Reserva reserva = buscarReservaDelHuesped(correo, id);
        exigir(reserva.puedeCancelar(), "Esta reserva ya no se puede cancelar");
        BigDecimal devolucion = calcularDevolucion(reserva);
        cancelaciones.save(new Cancelacion(reserva, reserva.getHuesped(), dto.motivo(), devolucion));
        reserva.cancelar(dto.motivo());
        return convertirADTO(reserva);
    }

    /** muestra la devolución */
    @Transactional(readOnly = true)
    public DevolucionDTO consultarDevolucion(String correo, Long id) {
        Reserva reserva = buscarReservaDelHuesped(correo, id);
        exigir(reserva.puedeCancelar(), "Esta reserva ya no se puede cancelar");
        return new DevolucionDTO(calcularDevolucion(reserva));
    }

    /** genera el comprobante */
    @Transactional(readOnly = true)
    public byte[] generarComprobante(String correo, Long id) {
        Reserva reserva = buscarReserva(id);
        boolean esHuesped = reserva.getHuesped().getCorreo().equals(correo);
        boolean esAnfitrion = reserva.getPublicacion().getAnfitrion().getCorreo().equals(correo);
        if (!esHuesped && !esAnfitrion) {
            throw ApiException.crearProhibida("Esta reserva no es suya");
        }

        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document();
        PdfWriter.getInstance(documento, salida);
        documento.open();
        documento.add(new Paragraph("Comprobante de reserva No. " + reserva.getId() + " - TuHogar"));
        documento.add(new Paragraph(" "));

        PdfPTable tabla = new PdfPTable(2);
        agregarFila(tabla, "Alojamiento", reserva.getPublicacion().getTitulo());
        agregarFila(tabla, "Ciudad", reserva.getPublicacion().getCiudad());
        agregarFila(tabla, "Huésped", reserva.getHuesped().getNombre());
        agregarFila(tabla, "Llegada", reserva.getFechaLlegada().toString());
        agregarFila(tabla, "Salida", reserva.getFechaSalida().toString());
        agregarFila(tabla, "Huéspedes", String.valueOf(reserva.getNumeroHuespedes()));
        agregarFila(tabla, "Tarifa base", reserva.getTarifaBase().toString());
        agregarFila(tabla, "Cargo de limpieza", reserva.getCargoLimpieza().toString());
        agregarFila(tabla, "Cargo de servicio", reserva.getCargoServicio().toString());
        agregarFila(tabla, "Descuentos", reserva.getDescuentos().toString());
        agregarFila(tabla, "Total", reserva.getTotal().toString());
        agregarFila(tabla, "Estado", reserva.getEstado().name());
        Optional<Pago> pago = pagos.findByReservaId(reserva.getId());
        if (pago.isPresent()) {
            agregarFila(tabla, "Referencia de pago", pago.get().getReferenciaPasarela());
        } else {
            agregarFila(tabla, "Referencia de pago", "Sin pago");
        }
        documento.add(tabla);
        documento.close();
        return salida.toByteArray();
    }

    private void agregarFila(PdfPTable tabla, String nombre, String valor) {
        tabla.addCell(nombre);
        tabla.addCell(valor);
    }

    // consultar

    @Transactional(readOnly = true)
    public List<ReservaDTO> consultarMisReservas(String correo) {
        return reservas.findByHuespedCorreoOrderByIdDesc(correo).stream().map(this::convertirADTO).toList();
    }

    @Transactional(readOnly = true)
    public List<ReservaDTO> consultarReservasDeMisPublicaciones(String correo) {
        return reservas.findByPublicacionAnfitrionCorreoOrderByIdDesc(correo).stream().map(this::convertirADTO).toList();
    }

    /** filtra todas */
    @Transactional(readOnly = true)
    public List<ReservaAdminDTO> consultarTodasLasReservas(String estado, LocalDate desde, LocalDate hasta, Long publicacion) {
        List<ReservaAdminDTO> filas = new ArrayList<>();
        for (Reserva r : reservas.findAllByOrderByIdDesc()) {
            if (estado != null && !estado.isBlank() && !r.getEstado().name().equals(estado)) continue;
            if (desde != null && r.getFechaLlegada().isBefore(desde)) continue;
            if (hasta != null && r.getFechaLlegada().isAfter(hasta)) continue;
            if (publicacion != null && !r.getPublicacion().getId().equals(publicacion)) continue;
            Publicacion p = r.getPublicacion();
            filas.add(new ReservaAdminDTO(r.getId(), p.getId(), p.getTitulo(), r.getHuesped().getNombre(),
                    p.getAnfitrion().getNombre(), r.getFechaLlegada(), r.getFechaSalida(), r.getTotal(),
                    r.getEstado().name()));
        }
        return filas;
    }

    // cálculo y reglas

    private Reserva armarReserva(Usuario huesped, Publicacion p, SolicitudReservaDTO s) {
        validarSolicitud(p, s);
        verificarDisponibilidad(p, s); // include cu-08
        Reserva reserva = new Reserva();
        reserva.setHuesped(huesped);
        reserva.setPublicacion(p);
        reserva.setFechaLlegada(s.llegada());
        reserva.setFechaSalida(s.salida());
        reserva.setNumeroHuespedes(s.huespedes());
        BigDecimal base = p.getPrecioNoche().multiply(BigDecimal.valueOf(reserva.calcularNoches()));
        reserva.setTarifaBase(base);
        reserva.setCargoLimpieza(p.getTarifaLimpieza());
        reserva.setCargoServicio(base.multiply(PORCENTAJE_CARGO_SERVICIO).setScale(2, RoundingMode.HALF_UP));
        if (reserva.calcularNoches() >= NOCHES_PARA_DESCUENTO) {
            reserva.setDescuentos(base.multiply(PORCENTAJE_DESCUENTO).setScale(2, RoundingMode.HALF_UP));
        }
        agregarServicios(reserva, p, s.serviciosIds());
        reserva.calcularTotal();
        return reserva;
    }

    private void validarSolicitud(Publicacion p, SolicitudReservaDTO s) {
        if (s.llegada().isBefore(LocalDate.now())) {
            throw ApiException.crearInvalida("FECHA_PASADA", "La fecha de llegada no puede ser pasada", "llegada");
        }
        if (!s.salida().isAfter(s.llegada())) {
            throw ApiException.crearInvalida("SALIDA_ANTERIOR", "La salida debe ser posterior a la llegada", "salida");
        }
        if (s.huespedes() > p.getCapacidad()) {
            throw ApiException.crearInvalida("CAPACIDAD_EXCEDIDA",
                    "El alojamiento admite máximo " + p.getCapacidad() + " huéspedes", "huespedes");
        }
    }

    // cu-08 verificar disponibilidad
    private void verificarDisponibilidad(Publicacion p, SolicitudReservaDTO s) {
        if (p.getPeriodos().stream().noneMatch(per -> per.cubre(s.llegada(), s.salida()))) {
            throw ApiException.crearInvalida("FUERA_DE_DISPONIBILIDAD",
                    "Esas fechas no están dentro de un periodo disponible", "llegada");
        }
        long cruces = reservas.contarCruces(p.getId(), OCUPAN, s.llegada(), s.salida());
        if (cruces > 0) {
            throw ApiException.crearConflicto("FECHAS_OCUPADAS", "Esas noches ya están reservadas", "llegada");
        }
    }

    // otra reserva pudo confirmarse antes
    private void revisarQueSigueLibre(Reserva reserva) {
        long cruces = reservas.contarCruces(reserva.getPublicacion().getId(), OCUPAN,
                reserva.getFechaLlegada(), reserva.getFechaSalida());
        if (cruces > 0) {
            throw ApiException.crearConflicto("FECHAS_OCUPADAS",
                    "Esas fechas ya no están disponibles porque otra reserva se confirmó", null);
        }
    }

    private void agregarServicios(Reserva reserva, Publicacion p, List<Long> ids) {
        if (ids == null) return;
        for (Long idServicio : ids.stream().distinct().toList()) {
            ServicioAdicional servicio = p.getServicios().stream().filter(s -> s.getId().equals(idServicio))
                    .findFirst().orElseThrow(() -> ApiException.crearInvalida("SERVICIO_INVALIDO",
                            "Un servicio elegido no pertenece al alojamiento", "serviciosIds"));
            reserva.getServicios().add(new ReservaServicio(reserva, servicio));
        }
    }

    // evita duplicados
    private ReservaDTO responderRepetida(String correo, Reserva existente) {
        if (!existente.getHuesped().getCorreo().equals(correo)) {
            throw ApiException.crearConflicto("CLAVE_REPETIDA", "Esa clave de solicitud ya fue usada", "claveIdempotencia");
        }
        return convertirADTO(existente);
    }

    // calcula la devolución
    private BigDecimal calcularDevolucion(Reserva reserva) {
        Optional<Pago> pago = pagos.findByReservaId(reserva.getId());
        PoliticaCancelacion politica = reserva.getPublicacion().getPolitica();
        if (pago.isEmpty() || politica == null) return BigDecimal.ZERO;
        return politica.calcularDevolucion(reserva);
    }

    // consultas y permisos

    private Publicacion buscarActiva(Long id) {
        return publicaciones.findById(id).filter(p -> p.getEstado().equals("ACTIVA"))
                .orElseThrow(this::crearErrorNoEncontrado);
    }

    private ApiException crearErrorNoEncontrado() {
        return new ApiException(HttpStatus.NOT_FOUND, "ALOJAMIENTO_NO_ENCONTRADO",
                "El alojamiento no existe o no está disponible", null);
    }

    private Usuario buscarUsuario(String correo) {
        return usuarios.findByCorreo(correo).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "USUARIO_NO_ENCONTRADO", "No existe la cuenta", null));
    }

    private Reserva buscarReserva(Long id) {
        return reservas.findById(id).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "RESERVA_NO_ENCONTRADA", "La reserva no existe", null));
    }

    private Reserva buscarReservaDelHuesped(String correo, Long id) {
        Reserva reserva = buscarReserva(id);
        if (!reserva.getHuesped().getCorreo().equals(correo)) {
            throw ApiException.crearProhibida("Esta reserva no es suya");
        }
        return reserva;
    }

    private Reserva buscarReservaDelAnfitrion(String correo, Long id) {
        Reserva reserva = buscarReserva(id);
        if (!reserva.getPublicacion().getAnfitrion().getCorreo().equals(correo)) {
            throw ApiException.crearProhibida("Esta reserva no es de una de sus publicaciones");
        }
        return reserva;
    }

    private void exigir(boolean permitido, String mensaje) {
        if (!permitido) throw ApiException.crearConflicto("ESTADO_INVALIDO", mensaje, null);
    }

    // conversión a dto

    private BigDecimal sumarCostoServicios(Reserva r) {
        return r.getServicios().stream().map(ReservaServicio::getCostoAplicado).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private ReservaDTO convertirADTO(Reserva r) {
        Publicacion p = r.getPublicacion();
        String referencia = pagos.findByReservaId(r.getId()).map(Pago::getReferenciaPasarela).orElse(null);
        BigDecimal devolucion = cancelaciones.findByReservaId(r.getId()).map(Cancelacion::getValorDevolucion).orElse(null);
        return new ReservaDTO(r.getId(), p.getId(), p.getTitulo(), p.getCiudad(), r.getHuesped().getNombre(),
                r.getFechaLlegada(), r.getFechaSalida(), r.calcularNoches(), r.getNumeroHuespedes(), r.getTarifaBase(),
                r.getCargoLimpieza(), sumarCostoServicios(r), r.getCargoServicio(), r.getDescuentos(), r.getTotal(),
                r.getEstado().name(), referencia, devolucion,
                r.puedePagar(), r.puedeCancelar(), r.puedeResponder());
    }

    private AlojamientoResumenDTO convertirAResumen(Publicacion p) {
        String imagen = p.getImagenes().isEmpty() ? null : p.getImagenes().get(0).getUrl();
        return new AlojamientoResumenDTO(p.getId(), p.getTitulo(), p.getCiudad(), p.getTipoAlojamiento(),
                p.getCapacidad(), p.getPrecioNoche(), imagen);
    }

    private AlojamientoDetalleDTO convertirADetalle(Publicacion p) {
        LocalDate hoy = LocalDate.now();
        return new AlojamientoDetalleDTO(p.getId(), p.getTitulo(), p.getDescripcion(), p.getCiudad(),
                p.getTipoAlojamiento(), p.getCapacidad(), p.getPrecioNoche(), p.getTarifaLimpieza(), p.getReglas(),
                p.getModoReserva(), p.getAnfitrion().getNombre(),
                p.getImagenes().stream().map(ImagenPublicacion::getUrl).toList(),
                p.getServicios().stream().map(s -> new ServicioDTO(s.getId(), s.getNombre(), s.getCosto())).toList(),
                p.getPeriodos().stream().filter(per -> per.seSuperpone(hoy, hoy.plusYears(1)))
                        .map(per -> new PeriodoDTO(per.getFechaInicio(), per.getFechaFin())).toList(),
                convertirAPolitica(p.getPolitica()));
    }

    private PoliticaDTO convertirAPolitica(PoliticaCancelacion pol) {
        return pol == null ? null : new PoliticaDTO(pol.getTipo(), pol.getDiasAnticipacion(), pol.getPorcentajeDevolucion());
    }
}
