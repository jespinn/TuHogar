package co.edu.unbosque.model.reservas;

import co.edu.unbosque.model.usuarios.Usuario;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cancelacion")
public class Cancelacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cancelacion")
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "id_reserva")
    private Reserva reserva;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    private String motivo;
    private BigDecimal valorDevolucion;
    @Enumerated(EnumType.STRING)
    private EstadoReserva estadoFinal;
    private LocalDateTime fecha = LocalDateTime.now();

    public Cancelacion() {
    }

    public Cancelacion(Reserva reserva, Usuario usuario, String motivo, BigDecimal valorDevolucion) {
        this.reserva = reserva;
        this.usuario = usuario;
        this.motivo = motivo;
        this.valorDevolucion = valorDevolucion;
        this.estadoFinal = EstadoReserva.CANCELADA;
    }

    public BigDecimal getValorDevolucion() { return valorDevolucion; }
    public EstadoReserva getEstadoFinal() { return estadoFinal; }
}
