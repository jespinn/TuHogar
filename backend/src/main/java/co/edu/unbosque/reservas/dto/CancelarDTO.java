package co.edu.unbosque.reservas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelarDTO(
        @NotBlank(message = "Indique el motivo")
        @Size(max = 200, message = "El motivo no puede superar 200 caracteres") String motivo) {
}
