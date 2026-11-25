package co.edu.unbosque.model.publicaciones;

import co.edu.unbosque.model.usuarios.Usuario;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "publicacion")
public class Publicacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_publicacion")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_anfitrion")
    private Usuario anfitrion;

    @ManyToOne
    @JoinColumn(name = "id_politica")
    private PoliticaCancelacion politica;

    private String titulo;
    private String descripcion;
    private String ciudad;
    private String direccion;
    private String tipoAlojamiento;
    private int capacidad;
    private BigDecimal precioNoche;
    private BigDecimal tarifaLimpieza;
    private String reglas;
    private String modoReserva;
    private String estado;

    @OneToMany(mappedBy = "publicacion", cascade = CascadeType.ALL)
    @OrderBy("orden")
    private List<ImagenPublicacion> imagenes = new ArrayList<>();

    @OneToMany(mappedBy = "publicacion", cascade = CascadeType.ALL)
    private List<ServicioAdicional> servicios = new ArrayList<>();

    @OneToMany(mappedBy = "publicacion", cascade = CascadeType.ALL)
    @OrderBy("fechaInicio")
    private List<PeriodoDisponible> periodos = new ArrayList<>();

    public Long getId() { return id; }
    public Usuario getAnfitrion() { return anfitrion; }
    public PoliticaCancelacion getPolitica() { return politica; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public String getCiudad() { return ciudad; }
    public String getDireccion() { return direccion; }
    public String getTipoAlojamiento() { return tipoAlojamiento; }
    public int getCapacidad() { return capacidad; }
    public BigDecimal getPrecioNoche() { return precioNoche; }
    public BigDecimal getTarifaLimpieza() { return tarifaLimpieza; }
    public String getReglas() { return reglas; }
    public String getModoReserva() { return modoReserva; }
    public String getEstado() { return estado; }
    public void pausar() { estado = "PAUSADA"; }
    public List<ImagenPublicacion> getImagenes() { return imagenes; }
    public List<ServicioAdicional> getServicios() { return servicios; }
    public List<PeriodoDisponible> getPeriodos() { return periodos; }
}
