package co.edu.unbosque.usuarios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** datos del perfil */
public record PerfilDTO(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñÜü .'-]*$", message = "El nombre solo puede tener letras, espacios y puntos")
        String nombre,

        @NotBlank(message = "El tipo de documento es obligatorio")
        @Pattern(regexp = "CC|CE|TI|PASAPORTE", message = "El tipo de documento no es válido")
        String tipoDocumento,

        @NotBlank(message = "El número de documento es obligatorio")
        @Size(max = 20, message = "El documento no puede superar 20 caracteres")
        String numeroDocumento) {
}
