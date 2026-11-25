package co.edu.unbosque.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record RegistroDTO(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñÜü .'-]*$", message = "El nombre solo puede tener letras, espacios y puntos")
        String nombre,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no es válido")
        @Size(max = 120, message = "El correo no puede superar 120 caracteres")
        String correo,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String contrasena,

        @NotBlank(message = "Debe confirmar la contraseña")
        String confirmacion,

        @NotBlank(message = "El tipo de documento es obligatorio")
        @Pattern(regexp = "CC|CE|TI|PASAPORTE", message = "El tipo de documento no es válido")
        String tipoDocumento,

        @NotBlank(message = "El número de documento es obligatorio")
        @Size(max = 20, message = "El documento no puede superar 20 caracteres")
        String numeroDocumento,

        @NotEmpty(message = "Debe elegir al menos un rol")
        List<String> roles,

        @NotNull(message = "Debe aceptar el tratamiento de datos")
        Boolean aceptaTratamientoDatos) {
}
