package co.edu.unbosque.model.publicaciones;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "periodo_disponible")
public class PeriodoDisponible {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_periodo")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_publicacion")
    private Publicacion publicacion;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    public PeriodoDisponible() {
    }

    public PeriodoDisponible(Publicacion publicacion, LocalDate fechaInicio, LocalDate fechaFin) {
        this.publicacion = publicacion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    /** rango se cruza */
    public boolean seSuperpone(LocalDate llegada, LocalDate salida) {
        return !llegada.isAfter(fechaFin) && !salida.isBefore(fechaInicio);
    }

    /** estadía cabe completa */
    public boolean cubre(LocalDate llegada, LocalDate salida) {
        return !llegada.isBefore(fechaInicio) && !salida.isAfter(fechaFin);
    }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
}
