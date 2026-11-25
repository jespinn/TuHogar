package co.edu.unbosque.model.publicaciones;

import jakarta.persistence.*;

@Entity
@Table(name = "imagen_publicacion")
public class ImagenPublicacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_imagen")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_publicacion")
    private Publicacion publicacion;

    private String url;
    private int orden;

    public String getUrl() { return url; }
    public int getOrden() { return orden; }
}
