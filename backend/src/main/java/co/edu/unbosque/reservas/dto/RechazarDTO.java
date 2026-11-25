package co.edu.unbosque.reservas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** motivo del rechazo */
public record RechazarDTO(
        @NotBlank(message = "Indique el motivo")
        @Size(max = 200, message = "El motivo no puede superar 200 caracteres") String motivo) {
}
