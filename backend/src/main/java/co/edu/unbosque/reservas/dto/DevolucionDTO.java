package co.edu.unbosque.reservas.dto;

import java.math.BigDecimal;

/** valor a devolver */
public class DevolucionDTO {
    private BigDecimal valorDevolucion;

    public DevolucionDTO(BigDecimal valorDevolucion) {
        this.valorDevolucion = valorDevolucion;
    }

    public BigDecimal getValorDevolucion() { return valorDevolucion; }
}
