package co.edu.unbosque.model.publicaciones;

import co.edu.unbosque.model.reservas.Reserva;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "politica_cancelacion")
public class PoliticaCancelacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_politica")
    private Long id;

    private String tipo;
    private int diasAnticipacion;
    private int porcentajeDevolucion;

    /** devolución según HU-24 CA2: 7 días o más 100 %, de 2 a 6 días 50 %, menos de 2 días 0 % */
    public BigDecimal calcularDevolucion(Reserva reserva) {
        long dias = ChronoUnit.DAYS.between(LocalDate.now(), reserva.getFechaLlegada());
        int porcentaje;
        if (dias >= 7) {
            porcentaje = 100;
        } else if (dias >= 2) {
            porcentaje = 50;
        } else {
            porcentaje = 0;
        }
        return reserva.getTotal().multiply(BigDecimal.valueOf(porcentaje))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public Long getId() { return id; }
    public String getTipo() { return tipo; }
    public int getDiasAnticipacion() { return diasAnticipacion; }
    public int getPorcentajeDevolucion() { return porcentajeDevolucion; }
}
