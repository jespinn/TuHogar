package co.edu.unbosque.reservas.dto;

import java.math.BigDecimal;

/** resumen de alojamiento */
public record AlojamientoResumenDTO(Long id, String titulo, String ciudad, String tipoAlojamiento,
                                    int capacidad, BigDecimal precioNoche, String imagenPrincipal) {
}
