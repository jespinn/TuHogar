package co.edu.unbosque.reportes.service;

import co.edu.unbosque.model.publicaciones.Publicacion;
import co.edu.unbosque.alojamientos.repository.PublicacionRepository;
import co.edu.unbosque.comun.ApiException;
import co.edu.unbosque.reportes.dto.AlojamientoSinReservaDTO;
import co.edu.unbosque.reportes.dto.IndicadoresDTO;
import co.edu.unbosque.reportes.dto.IngresoReservaDTO;
import co.edu.unbosque.model.administracion.Reporte;
import co.edu.unbosque.reportes.repository.ReporteRepository;
import co.edu.unbosque.model.reservas.EstadoReserva;
import co.edu.unbosque.model.reservas.Reserva;
import co.edu.unbosque.reservas.repository.ReservaRepository;
import co.edu.unbosque.model.usuarios.Rol;
import co.edu.unbosque.model.usuarios.Usuario;
import java.math.BigDecimal;
import co.edu.unbosque.usuarios.repository.UsuarioRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReporteService {
    // reservas que cuentan
    private static final List<EstadoReserva> CUENTAN = List.of(EstadoReserva.CONFIRMADA, EstadoReserva.COMPLETADA);
    private static final List<String> TIPOS = List.of("USUARIOS", "PUBLICACIONES", "RESERVAS", "INGRESOS", "DEMANDA");
    // formatos permitidos
    private static final List<String> FORMATOS = List.of("XLSX", "PDF");

    private final UsuarioRepository usuarios;
    private final PublicacionRepository publicaciones;
    private final ReservaRepository reservas;
    private final ReporteRepository reportes;

    public ReporteService(UsuarioRepository usuarios, PublicacionRepository publicaciones,
                          ReservaRepository reservas, ReporteRepository reportes) {
        this.usuarios = usuarios;
        this.publicaciones = publicaciones;
        this.reservas = reservas;
        this.reportes = reportes;
    }

    @Transactional(readOnly = true)
    public IndicadoresDTO consultarIndicadores() {
        return consultarIndicadores(null, null);
    }

    /** HU-32 CA2: indicadores en un rango de fechas (llegada de la reserva, registro del usuario) */
    @Transactional(readOnly = true)
    public IndicadoresDTO consultarIndicadores(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && hasta.isBefore(desde)) {
            throw ApiException.crearInvalida("RANGO_INVALIDO", "La fecha final no puede ser anterior a la inicial", "hasta");
        }
        boolean hayFiltro = desde != null || hasta != null;

        // reservas del rango
        List<Reserva> delRango = new ArrayList<>();
        for (Reserva r : reservas.findAll()) {
            if (estaEnRango(r.getFechaLlegada(), desde, hasta)) delRango.add(r);
        }
        Map<String, Long> porEstado = new TreeMap<>();
        List<Reserva> validas = new ArrayList<>();
        BigDecimal ingresosEstimados = BigDecimal.ZERO;
        for (Reserva r : delRango) {
            sumar(porEstado, r.getEstado().name());
            if (CUENTAN.contains(r.getEstado())) {
                validas.add(r);
                ingresosEstimados = ingresosEstimados.add(r.getTotal());
            }
        }
        validas.sort((a, b) -> a.getFechaLlegada().compareTo(b.getFechaLlegada()));

        // usuarios del rango
        Map<String, Long> porRol = new TreeMap<>();
        Map<String, Long> porEstadoUsuario = new TreeMap<>();
        int usuariosDelRango = 0;
        for (Usuario u : usuarios.findAll()) {
            if (!estaEnRango(u.getFechaRegistro().toLocalDate(), desde, hasta)) continue;
            usuariosDelRango++;
            sumar(porEstadoUsuario, u.getEstado());
            for (Rol rol : u.getRoles()) sumar(porRol, rol.getNombre());
        }

        // HU-32 CA3
        String aviso = null;
        if (hayFiltro && delRango.isEmpty() && usuariosDelRango == 0) {
            aviso = "No hay datos en el rango de fechas elegido";
        }

        Map<String, Long> porMes = new TreeMap<>();
        Map<String, Long> porTipo = new TreeMap<>();
        Map<String, Long> porCiudad = new TreeMap<>();
        Map<String, Long> nochesPorFecha = new TreeMap<>();
        Map<String, Long> finDeSemana = new LinkedHashMap<>();
        finDeSemana.put("Fin de semana (sábado y domingo)", 0L);
        finDeSemana.put("Entre semana (lunes a viernes)", 0L);
        List<IngresoReservaDTO> ingresos = new ArrayList<>();

        for (Reserva r : validas) {
            Publicacion p = r.getPublicacion();
            sumar(porMes, r.getFechaLlegada().toString().substring(0, 7)); // año y mes
            sumar(porTipo, p.getTipoAlojamiento());
            sumar(porCiudad, p.getCiudad());
            ingresos.add(new IngresoReservaDTO(r.getId(), p.getTitulo(), p.getCiudad(),
                    r.getFechaLlegada(), r.getFechaSalida(), r.getTotal()));
            // noches de estadía
            for (LocalDate noche = r.getFechaLlegada(); noche.isBefore(r.getFechaSalida()); noche = noche.plusDays(1)) {
                sumar(nochesPorFecha, noche.toString());
                DayOfWeek dia = noche.getDayOfWeek();
                boolean esFin = dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY;
                sumar(finDeSemana, esFin ? "Fin de semana (sábado y domingo)" : "Entre semana (lunes a viernes)");
            }
        }

        List<AlojamientoSinReservaDTO> sinReservas = new ArrayList<>();
        for (Publicacion p : publicaciones.buscarSinReservas(CUENTAN)) {
            sinReservas.add(new AlojamientoSinReservaDTO(p.getId(), p.getTitulo(), p.getCiudad(), p.getTipoAlojamiento()));
        }

        return new IndicadoresDTO(
                porRol,
                porEstadoUsuario,
                // las publicaciones no tienen fecha en el modelo
                convertirAMapa(publicaciones.contarPorEstado()),
                convertirAMapa(publicaciones.contarPorCiudad()),
                convertirAMapa(publicaciones.contarPorTipo()),
                porEstado,
                ingresosEstimados,
                porMes,
                porTipo,
                ordenarMayorAMenor(porCiudad, porCiudad.size()),
                ordenarMayorAMenor(nochesPorFecha, 5),
                finDeSemana,
                ingresos,
                sinReservas,
                contarReservasRecientes(),
                aviso);
    }

    private boolean estaEnRango(LocalDate fecha, LocalDate desde, LocalDate hasta) {
        if (desde != null && fecha.isBefore(desde)) return false;
        if (hasta != null && fecha.isAfter(hasta)) return false;
        return true;
    }

    // reservas por periodo
    private Map<String, Long> contarReservasRecientes() {
        LocalDate hoy = LocalDate.now();
        Map<String, Long> recientes = new LinkedHashMap<>();
        recientes.put("Hoy", reservas.countByFechaCreacionGreaterThanEqual(hoy.atStartOfDay()));
        recientes.put("Últimos 7 días", reservas.countByFechaCreacionGreaterThanEqual(hoy.minusDays(6).atStartOfDay()));
        recientes.put("Últimos 30 días", reservas.countByFechaCreacionGreaterThanEqual(hoy.minusDays(29).atStartOfDay()));
        return recientes;
    }

    @Transactional
    public byte[] exportar(String correoAdministrador, String tipo, String formato) {
        return exportar(correoAdministrador, tipo, formato, null, null);
    }

    /** exporta el reporte con el mismo rango del dashboard */
    @Transactional
    public byte[] exportar(String correoAdministrador, String tipo, String formato, LocalDate desde, LocalDate hasta) {
        String tipoNormal = tipo == null ? "" : tipo.toUpperCase();
        if (!TIPOS.contains(tipoNormal)) {
            throw ApiException.crearInvalida("TIPO_INVALIDO", "Tipo de reporte no válido: " + TIPOS, "tipo");
        }
        String formatoNormal = formato == null ? "" : formato.toUpperCase();
        if (!FORMATOS.contains(formatoNormal)) {
            throw ApiException.crearInvalida("FORMATO_INVALIDO", "Formato no válido: " + FORMATOS, "formato");
        }
        Optional<Usuario> buscado = usuarios.findByCorreo(correoAdministrador);
        if (buscado.isEmpty()) {
            throw ApiException.crearProhibida("Usuario no encontrado");
        }

        List<String[]> filas = filasDelReporte(tipoNormal, desde, hasta);
        // sin información
        if (filas.isEmpty()) {
            throw ApiException.crearConflicto("SIN_INFORMACION", "No hay información para el reporte", null);
        }

        Reporte reporte = new Reporte(buscado.get(), tipoNormal, formatoNormal);
        byte[] archivo = reporte.exportar(filas);
        reportes.save(reporte);
        return archivo;
    }

    // filas del reporte
    private List<String[]> filasDelReporte(String tipo, LocalDate desde, LocalDate hasta) {
        IndicadoresDTO datos = consultarIndicadores(desde, hasta);
        List<String[]> filas = new ArrayList<>();
        switch (tipo) {
            case "USUARIOS":
                agregarFilas(filas, "Usuarios por rol", datos.usuariosPorRol());
                agregarFilas(filas, "Usuarios por estado", datos.usuariosPorEstado());
                break;
            case "PUBLICACIONES":
                agregarFilas(filas, "Publicaciones por estado", datos.publicacionesPorEstado());
                agregarFilas(filas, "Publicaciones por ciudad", datos.publicacionesPorCiudad());
                agregarFilas(filas, "Publicaciones por tipo", datos.publicacionesPorTipo());
                break;
            case "RESERVAS":
                agregarFilas(filas, "Reservas hechas por periodo", datos.reservasRecientes());
                agregarFilas(filas, "Reservas por estado", datos.reservasPorEstado());
                agregarFilas(filas, "Reservas por mes", datos.reservasPorMes());
                agregarFilas(filas, "Reservas por tipo de alojamiento", datos.reservasPorTipo());
                agregarFilas(filas, "Fechas más frecuentes (noches)", datos.fechasMasFrecuentes());
                agregarFilas(filas, "Noches reservadas", datos.nochesFinDeSemana());
                break;
            case "INGRESOS":
                for (IngresoReservaDTO fila : datos.ingresosPorReserva()) {
                    String categoria = "Reserva " + fila.idReserva() + ": " + fila.alojamiento() + " (" + fila.llegada() + " a " + fila.salida() + ")";
                    filas.add(new String[] {"Ingresos por reserva", categoria, fila.total().toString()});
                }
                // total de ingresos
                if (!filas.isEmpty()) {
                    filas.add(new String[] {"Ingresos estimados", "CONFIRMADAS y COMPLETADAS", datos.ingresosEstimados().toString()});
                }
                break;
            default: // demanda
                agregarFilas(filas, "Demanda por ciudad (reservas)", datos.reservasPorCiudad());
                for (AlojamientoSinReservaDTO a : datos.alojamientosSinReservas()) {
                    filas.add(new String[] {"Alojamientos sin reservas", a.titulo() + " (" + a.ciudad() + ")", "0"});
                }
                break;
        }
        return filas;
    }

    private void agregarFilas(List<String[]> filas, String seccion, Map<String, Long> datos) {
        for (Map.Entry<String, Long> dato : datos.entrySet()) {
            filas.add(new String[] {seccion, dato.getKey(), dato.getValue().toString()});
        }
    }

    private void sumar(Map<String, Long> mapa, String clave) {
        mapa.merge(clave, 1L, Long::sum);
    }

    // mayor a menor
    private Map<String, Long> ordenarMayorAMenor(Map<String, Long> mapa, int limite) {
        List<Map.Entry<String, Long>> filas = new ArrayList<>(mapa.entrySet());
        filas.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        Map<String, Long> resultado = new LinkedHashMap<>();
        for (int i = 0; i < filas.size() && i < limite; i++) {
            resultado.put(filas.get(i).getKey(), filas.get(i).getValue());
        }
        return resultado;
    }

    // filas a mapa
    private Map<String, Long> convertirAMapa(List<Object[]> filas) {
        Map<String, Long> mapa = new LinkedHashMap<>();
        for (Object[] fila : filas) {
            mapa.put(fila[0].toString(), (Long) fila[1]);
        }
        return mapa;
    }
}
