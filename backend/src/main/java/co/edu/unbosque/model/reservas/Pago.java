package co.edu.unbosque.model.reservas;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pago")
public class Pago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "id_reserva")
    private Reserva reserva;

    private BigDecimal monto;
    private String referenciaPasarela;
    private String estado;
    private LocalDateTime fecha = LocalDateTime.now();

    public Pago() {
    }

    public Pago(Reserva reserva, BigDecimal monto, String referenciaPasarela) {
        this.reserva = reserva;
        this.monto = monto;
        this.referenciaPasarela = referenciaPasarela;
        this.estado = "APROBADO"; // los pagos rechazados no se guardan: la reserva sigue pendiente
    }

    public BigDecimal getMonto() { return monto; }
    public String getReferenciaPasarela() { return referenciaPasarela; }
}
