package co.edu.unbosque.usuarios.dto;

import java.time.LocalDateTime;
import java.util.List;

/** datos visibles */
public record UsuarioDTO(Long id, String nombre, String correo, String tipoDocumento, String numeroDocumento,
                         String estado, List<String> roles, LocalDateTime fechaRegistro) {
}
