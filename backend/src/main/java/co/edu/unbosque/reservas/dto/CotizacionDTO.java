package co.edu.unbosque.reservas.dto;

import java.math.BigDecimal;

/** desglose del precio */
public record CotizacionDTO(long noches, BigDecimal tarifaBase, BigDecimal cargoLimpieza,
                            BigDecimal serviciosAdicionales, BigDecimal cargoServicio,
                            BigDecimal descuentos, BigDecimal total) {
}
