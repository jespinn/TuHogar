package co.edu.unbosque.reservas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** reserva para mostrar */
public record ReservaDTO(Long id, Long alojamientoId, String alojamiento, String ciudad, String huesped,
                         LocalDate llegada, LocalDate salida, long noches, int huespedes,
                         BigDecimal tarifaBase, BigDecimal cargoLimpieza, BigDecimal serviciosAdicionales,
                         BigDecimal cargoServicio, BigDecimal descuentos, BigDecimal total,
                         String estado, String referenciaPago, BigDecimal valorDevolucion,
                         boolean puedePagar, boolean puedeCancelar, boolean puedeResponder) {
}
