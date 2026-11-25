package co.edu.unbosque.reportes.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** ingreso por reserva */
public record IngresoReservaDTO(Long idReserva, String alojamiento, String ciudad, LocalDate llegada,
                                LocalDate salida, BigDecimal total) {
}
