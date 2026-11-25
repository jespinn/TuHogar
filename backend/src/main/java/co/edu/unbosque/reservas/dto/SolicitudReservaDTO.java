package co.edu.unbosque.reservas.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/** solicitud de reserva */
public record SolicitudReservaDTO(
        @NotNull(message = "Debe elegir un alojamiento") Long publicacionId,
        @NotNull(message = "La fecha de llegada es obligatoria") LocalDate llegada,
        @NotNull(message = "La fecha de salida es obligatoria") LocalDate salida,
        @NotNull(message = "Indique el número de huéspedes")
        @Min(value = 1, message = "Debe haber al menos 1 huésped") Integer huespedes,
        List<Long> serviciosIds,
        String claveIdempotencia) {
}
