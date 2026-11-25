package co.edu.unbosque.model.usuarios;

import jakarta.persistence.*;

@Entity
@Table(name = "rol")
public class Rol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Long id;
    private String nombre;

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
}
