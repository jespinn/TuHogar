package co.edu.unbosque.reportes.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** indicadores del panel */
public record IndicadoresDTO(
        Map<String, Long> usuariosPorRol,
        Map<String, Long> usuariosPorEstado,
        Map<String, Long> publicacionesPorEstado,
        Map<String, Long> publicacionesPorCiudad,
        Map<String, Long> publicacionesPorTipo,
        Map<String, Long> reservasPorEstado,
        BigDecimal ingresosEstimados,
        Map<String, Long> reservasPorMes,
        Map<String, Long> reservasPorTipo,
        Map<String, Long> reservasPorCiudad,
        Map<String, Long> fechasMasFrecuentes,
        Map<String, Long> nochesFinDeSemana,
        List<IngresoReservaDTO> ingresosPorReserva,
        List<AlojamientoSinReservaDTO> alojamientosSinReservas,
        // reservas por periodo
        Map<String, Long> reservasRecientes,
        // aviso cuando el rango no tiene datos
        String aviso) {
}
