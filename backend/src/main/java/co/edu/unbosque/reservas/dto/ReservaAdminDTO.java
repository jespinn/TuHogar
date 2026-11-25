package co.edu.unbosque.reservas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** reserva para administrador */
public class ReservaAdminDTO {
    private Long id;
    private Long alojamientoId;
    private String alojamiento;
    private String huesped;
    private String anfitrion;
    private LocalDate llegada;
    private LocalDate salida;
    private BigDecimal total;
    private String estado;

    public ReservaAdminDTO(Long id, Long alojamientoId, String alojamiento, String huesped, String anfitrion,
                           LocalDate llegada, LocalDate salida, BigDecimal total, String estado) {
        this.id = id;
        this.alojamientoId = alojamientoId;
        this.alojamiento = alojamiento;
        this.huesped = huesped;
        this.anfitrion = anfitrion;
        this.llegada = llegada;
        this.salida = salida;
        this.total = total;
        this.estado = estado;
    }

    public Long getId() { return id; }
    public Long getAlojamientoId() { return alojamientoId; }
    public String getAlojamiento() { return alojamiento; }
    public String getHuesped() { return huesped; }
    public String getAnfitrion() { return anfitrion; }
    public LocalDate getLlegada() { return llegada; }
    public LocalDate getSalida() { return salida; }
    public BigDecimal getTotal() { return total; }
    public String getEstado() { return estado; }
}
