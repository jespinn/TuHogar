package co.edu.unbosque.model.reservas;

/** estados de reserva */
public enum EstadoReserva {
    PENDIENTE_APROBACION,
    PENDIENTE_PAGO,
    CONFIRMADA,
    RECHAZADA,
    CANCELADA,
    COMPLETADA,
    EN_REVISION_MANUAL
}
