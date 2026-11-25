package co.edu.unbosque.reservas.service;

import java.util.UUID;
import org.springframework.stereotype.Component;

/** pasarela simulada */
@Component
public class PasarelaSimulada {

    public record Resultado(boolean aprobado, String referencia, String mensaje) {
    }

    public Resultado cobrar(String numeroTarjeta) {
        String ultimos4 = numeroTarjeta.substring(numeroTarjeta.length() - 4); // últimos cuatro dígitos
        if (ultimos4.equals("0000")) {
            return new Resultado(false, null, "La tarjeta fue rechazada por el banco");
        }
        String referencia = "SIM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() + "-" + ultimos4;
        return new Resultado(true, referencia, "Pago aprobado");
    }
}
