package co.edu.unbosque.model.reservas;

import co.edu.unbosque.model.publicaciones.ServicioAdicional;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/** servicio de reserva */
@Entity
@Table(name = "reserva_servicio")
public class ReservaServicio {

    @Embeddable
    public static class Clave implements Serializable {
        @Column(name = "id_reserva")
        private Long idReserva;
        @Column(name = "id_servicio")
        private Long idServicio;

        @Override
        public boolean equals(Object otro) {
            return otro instanceof Clave c && Objects.equals(idReserva, c.idReserva) && Objects.equals(idServicio, c.idServicio);
        }

        @Override
        public int hashCode() {
            return Objects.hash(idReserva, idServicio);
        }
    }

    @EmbeddedId
    private Clave id = new Clave();

    @ManyToOne(optional = false)
    @MapsId("idReserva")
    @JoinColumn(name = "id_reserva")
    private Reserva reserva;

    @ManyToOne(optional = false)
    @MapsId("idServicio")
    @JoinColumn(name = "id_servicio")
    private ServicioAdicional servicio;

    private BigDecimal costoAplicado;

    public ReservaServicio() {
    }

    public ReservaServicio(Reserva reserva, ServicioAdicional servicio) {
        this.reserva = reserva;
        this.servicio = servicio;
        this.costoAplicado = servicio.getCosto();
    }

    public BigDecimal getCostoAplicado() { return costoAplicado; }
}
