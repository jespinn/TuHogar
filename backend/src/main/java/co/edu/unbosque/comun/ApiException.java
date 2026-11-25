package co.edu.unbosque.comun;

import org.springframework.http.HttpStatus;

/** error de negocio */
public class ApiException extends RuntimeException {
    private final HttpStatus estado;
    private final String codigo;
    private final String campo;

    public ApiException(HttpStatus estado, String codigo, String mensaje, String campo) {
        super(mensaje);
        this.estado = estado;
        this.codigo = codigo;
        this.campo = campo;
    }

    public static ApiException crearInvalida(String codigo, String mensaje, String campo) {
        return new ApiException(HttpStatus.BAD_REQUEST, codigo, mensaje, campo);
    }

    public static ApiException crearConflicto(String codigo, String mensaje, String campo) {
        return new ApiException(HttpStatus.CONFLICT, codigo, mensaje, campo);
    }

    public static ApiException crearProhibida(String mensaje) {
        return new ApiException(HttpStatus.FORBIDDEN, "SIN_PERMISO", mensaje, null);
    }

    public HttpStatus getEstado() { return estado; }
    public String getCodigo() { return codigo; }
    public String getCampo() { return campo; }
}
