package co.edu.unbosque.model.reservas;

import co.edu.unbosque.model.publicaciones.Publicacion;
import co.edu.unbosque.model.usuarios.Usuario;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reserva")
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_huesped")
    private Usuario huesped;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_publicacion")
    private Publicacion publicacion;

    private LocalDate fechaLlegada;
    private LocalDate fechaSalida;
    private int numeroHuespedes;
    private BigDecimal tarifaBase = BigDecimal.ZERO;
    private BigDecimal cargoLimpieza = BigDecimal.ZERO;
    private BigDecimal cargoServicio = BigDecimal.ZERO;
    private BigDecimal descuentos = BigDecimal.ZERO;
    private BigDecimal total = BigDecimal.ZERO;
    private String claveIdempotencia;

    @Enumerated(EnumType.STRING)
    private EstadoReserva estado = EstadoReserva.PENDIENTE_PAGO;

    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL)
    private List<ReservaServicio> servicios = new ArrayList<>();

    public long calcularNoches() {
        return ChronoUnit.DAYS.between(fechaLlegada, fechaSalida);
    }

    /** calcula el total */
    public BigDecimal calcularTotal() {
        BigDecimal adicionales = servicios.stream().map(ReservaServicio::getCostoAplicado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        total = tarifaBase.add(cargoLimpieza).add(adicionales).add(cargoServicio).subtract(descuentos);
        return total;
    }

    public void aprobar() { estado = EstadoReserva.PENDIENTE_PAGO; }
    // cambios de estado
    public void rechazar(String motivo) { estado = EstadoReserva.RECHAZADA; }
    public void confirmar() { estado = EstadoReserva.CONFIRMADA; }
    public void cancelar(String motivo) { estado = EstadoReserva.CANCELADA; }

    // acciones por estado
    public boolean puedeResponder() { return estado == EstadoReserva.PENDIENTE_APROBACION; }
    public boolean puedePagar() { return estado == EstadoReserva.PENDIENTE_PAGO; }
    public boolean puedeCancelar() {
        return estado == EstadoReserva.PENDIENTE_APROBACION || estado == EstadoReserva.PENDIENTE_PAGO
                || estado == EstadoReserva.CONFIRMADA;
    }

    public Long getId() { return id; }
    public Usuario getHuesped() { return huesped; }
    public void setHuesped(Usuario huesped) { this.huesped = huesped; }
    public Publicacion getPublicacion() { return publicacion; }
    public void setPublicacion(Publicacion publicacion) { this.publicacion = publicacion; }
    public LocalDate getFechaLlegada() { return fechaLlegada; }
    public void setFechaLlegada(LocalDate fechaLlegada) { this.fechaLlegada = fechaLlegada; }
    public LocalDate getFechaSalida() { return fechaSalida; }
    public void setFechaSalida(LocalDate fechaSalida) { this.fechaSalida = fechaSalida; }
    public int getNumeroHuespedes() { return numeroHuespedes; }
    public void setNumeroHuespedes(int numeroHuespedes) { this.numeroHuespedes = numeroHuespedes; }
    public BigDecimal getTarifaBase() { return tarifaBase; }
    public void setTarifaBase(BigDecimal tarifaBase) { this.tarifaBase = tarifaBase; }
    public BigDecimal getCargoLimpieza() { return cargoLimpieza; }
    public void setCargoLimpieza(BigDecimal cargoLimpieza) { this.cargoLimpieza = cargoLimpieza; }
    public BigDecimal getCargoServicio() { return cargoServicio; }
    public void setCargoServicio(BigDecimal cargoServicio) { this.cargoServicio = cargoServicio; }
    public BigDecimal getDescuentos() { return descuentos; }
    public void setDescuentos(BigDecimal descuentos) { this.descuentos = descuentos; }
    public BigDecimal getTotal() { return total; }
    public String getClaveIdempotencia() { return claveIdempotencia; }
    public void setClaveIdempotencia(String claveIdempotencia) { this.claveIdempotencia = claveIdempotencia; }
    public EstadoReserva getEstado() { return estado; }
    public void setEstado(EstadoReserva estado) { this.estado = estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public List<ReservaServicio> getServicios() { return servicios; }
}
