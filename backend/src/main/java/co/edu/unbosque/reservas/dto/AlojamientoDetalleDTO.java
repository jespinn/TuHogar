package co.edu.unbosque.reservas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AlojamientoDetalleDTO(Long id, String titulo, String descripcion, String ciudad,
                                    String tipoAlojamiento, int capacidad, BigDecimal precioNoche,
                                    BigDecimal tarifaLimpieza, String reglas, String modoReserva,
                                    String anfitrion, List<String> imagenes, List<ServicioDTO> servicios,
                                    List<PeriodoDTO> disponibilidad, PoliticaDTO politicaCancelacion) {

    public record ServicioDTO(Long id, String nombre, BigDecimal costo) {
    }

    public record PeriodoDTO(LocalDate desde, LocalDate hasta) {
    }

    public record PoliticaDTO(String tipo, int diasAnticipacion, int porcentajeDevolucion) {
    }
}
