package co.edu.unbosque.comun;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class ManejadorErrores {
    private static final Logger log = LoggerFactory.getLogger(ManejadorErrores.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorApi> manejarNegocio(ApiException e) {
        return construirRespuesta(e.getEstado(), e.getCodigo(), e.getMessage(), e.getCampo());
    }

    // errores de validación
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorApi> manejarValidacion(BindException e) {
        FieldError error = e.getBindingResult().getFieldError();
        if (error == null) {
            return construirRespuesta(HttpStatus.BAD_REQUEST, "VALIDACION", "Datos inválidos", null);
        }
        String mensaje = error.isBindingFailure() ? "Valor inválido" : error.getDefaultMessage();
        return construirRespuesta(HttpStatus.BAD_REQUEST, "VALIDACION", mensaje, error.getField());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorApi> manejarJsonInvalido(HttpMessageNotReadableException e) {
        return construirRespuesta(HttpStatus.BAD_REQUEST, "JSON_INVALIDO", "El cuerpo de la petición no es válido", null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorApi> manejarSinPermiso(AccessDeniedException e) {
        return construirRespuesta(HttpStatus.FORBIDDEN, "SIN_PERMISO", "No tiene permiso para esta acción", null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorApi> manejarNoEncontrado(NoResourceFoundException e) {
        return construirRespuesta(HttpStatus.NOT_FOUND, "NO_ENCONTRADO", "Recurso no encontrado", null);
    }

    // registra errores inesperados
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorApi> manejarInesperado(Exception e, HttpServletRequest peticion) {
        log.error("[modulo={}] Error inesperado en {} {}", obtenerModulo(peticion), peticion.getMethod(), peticion.getRequestURI(), e);
        return construirRespuesta(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO", "Ocurrió un error inesperado", null);
    }

    private String obtenerModulo(HttpServletRequest peticion) {
        String ruta = peticion.getRequestURI();
        if (ruta.startsWith("/api/reportes")) return "reportes";
        if (ruta.startsWith("/api/reservas")) return "reservas";
        return "usuarios";
    }

    private ResponseEntity<ErrorApi> construirRespuesta(HttpStatus estado, String codigo, String mensaje, String campo) {
        return ResponseEntity.status(estado).body(new ErrorApi(codigo, mensaje, campo));
    }
}
