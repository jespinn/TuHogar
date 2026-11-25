package co.edu.unbosque.reservas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** tarjeta de prueba */
public record PagoDTO(
        @NotBlank(message = "El número de tarjeta es obligatorio")
        @Pattern(regexp = "\\d{16}", message = "La tarjeta debe tener 16 dígitos") String numeroTarjeta,

        @NotBlank(message = "El nombre del titular es obligatorio") String titular,

        @NotBlank(message = "El código de seguridad es obligatorio")
        @Pattern(regexp = "\\d{3,4}", message = "El código de seguridad debe tener 3 o 4 dígitos") String codigoSeguridad) {
}
