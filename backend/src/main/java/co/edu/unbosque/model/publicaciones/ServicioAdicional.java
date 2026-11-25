package co.edu.unbosque.model.publicaciones;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "servicio_adicional")
public class ServicioAdicional {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_servicio")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_publicacion")
    private Publicacion publicacion;

    private String nombre;
    private BigDecimal costo;

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public BigDecimal getCosto() { return costo; }
}
