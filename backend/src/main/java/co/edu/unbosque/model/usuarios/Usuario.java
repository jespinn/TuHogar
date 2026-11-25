package co.edu.unbosque.model.usuarios;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuario")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;
    private String nombre;
    private String correo;
    private String contrasenaHash;
    private String tipoDocumento;
    private String numeroDocumento;
    private Boolean aceptaTratamientoDatos;
    private String estado = "ACTIVA";
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "usuario_rol",
            joinColumns = @JoinColumn(name = "id_usuario"),
            inverseJoinColumns = @JoinColumn(name = "id_rol"))
    private Set<Rol> roles = new HashSet<>();

    /** actualiza el perfil */
    public void actualizarPerfil(String nombre, String tipoDocumento, String numeroDocumento) {
        this.nombre = nombre;
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
    }

    public void desactivar() { estado = "INACTIVA"; }
    public void reactivar() { estado = "ACTIVA"; }
    public void restringir() { estado = "RESTRINGIDA"; } // lo hace el administrador
    public boolean estaActiva() { return "ACTIVA".equals(estado); }
    public boolean estaRestringida() { return "RESTRINGIDA".equals(estado); }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getContrasenaHash() { return contrasenaHash; }
    public void setContrasenaHash(String contrasenaHash) { this.contrasenaHash = contrasenaHash; }
    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }
    public Boolean getAceptaTratamientoDatos() { return aceptaTratamientoDatos; }
    public void setAceptaTratamientoDatos(Boolean acepta) { this.aceptaTratamientoDatos = acepta; }
    public String getEstado() { return estado; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public Set<Rol> getRoles() { return roles; }
}
